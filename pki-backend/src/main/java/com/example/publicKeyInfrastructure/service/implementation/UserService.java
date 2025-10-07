package com.example.publicKeyInfrastructure.service.implementation;

import com.example.publicKeyInfrastructure.dto.*;
import com.example.publicKeyInfrastructure.model.*;
import com.example.publicKeyInfrastructure.repository.IOrganizationRepository;
import com.example.publicKeyInfrastructure.repository.IRoleRepository;
import com.example.publicKeyInfrastructure.repository.IUserRepository;
import com.example.publicKeyInfrastructure.repository.IVerificationTokenRepository;
import com.example.publicKeyInfrastructure.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class UserService implements IUserService{

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private IRoleRepository roleRepository;

    @Autowired
    private IVerificationTokenRepository verificationTokenRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private IOrganizationRepository organizationRepository;

    @Override
    public UserDTO register(RegisterDTO request) {
        if (this.userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with provided email already exists!");
        }
        Organization organization = organizationRepository.findByName(request.getOrganization())
                .orElseThrow(() -> new RuntimeException("Organization not found: " + request.getOrganization()));
        User newUser = new User(request);
        System.out.println(organization.toString());
        newUser.setOrganization(organization);
        newUser.setEnabled(false);
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        Set<Role> roles = new HashSet<>();
        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found!"));
        roles.add(defaultRole);
        newUser.setRoles(roles);
        User user = userRepository.save(newUser);
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(user, token, Duration.ofHours(1));
        verificationTokenRepository.save(verificationToken);

        String confirmationUrl = "https://localhost:8443/api/users/verify-email?token=" + token;
        emailService.sendEmail(user.getEmail(), "Email Verification", "Click to verify: " + confirmationUrl);
        return new UserDTO(user);
    }

    @Override
    public TokenPair login(LoginDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return jwtService.generateTokenPair(authentication);
    }

    @Override
    public TokenPair refreshToken(RefreshTokenDTO request) {
        String refreshToken = request.getRefreshToken();
        if(!jwtService.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String user = jwtService.extractUsernameFromToken(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(user);

        if (userDetails == null) {
            throw new IllegalArgumentException("User not found");
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        String accessToken = jwtService.generateAccessToken(authentication);
        return new TokenPair(accessToken, refreshToken);
    }

    @Override
    public List<UserTableViewDTO> getAllCAUsers() {
        return userRepository.findAllEnabledCAUsers().stream()
                .map(u -> {
                    UserTableViewDTO dto = new UserTableViewDTO(u);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<UserWithRoleDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRoles()
                        .stream()
                        .noneMatch(role -> role.getName().equals("ROLE_ADMIN")))
                .map(UserWithRoleDTO::new)
                .collect(Collectors.toList());
    }



    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return new UserDTO(user);
    }

    public UserDTO upgradeToCA(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isUser = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_USER"));

        if (!isUser) {
            throw new RuntimeException("User does not have ROLE_USER");
        }

        Role caRole = roleRepository.findByName("ROLE_CA")
                .orElseThrow(() -> new RuntimeException("ROLE_CA not found"));

        user.getRoles().clear();
        user.getRoles().add(caRole);
        userRepository.save(user);

        return new UserDTO(user);
    }
    @Override
    public List<UserTableViewDTO> getUsersForOrganization(Long id) {
        return userRepository.findAllEnabledCAUsersByOrganization(id).stream()
                .map(u -> {
                    UserTableViewDTO dto = new UserTableViewDTO(u);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    @Override
    public List<UserTableViewDTO> getAllUsersForOrganization(Long id) {
        return userRepository.findAllEnabledUsersByOrganization(id).stream()
                .map(u -> {
                    UserTableViewDTO dto = new UserTableViewDTO(u);
                    return dto;
                })
                .collect(Collectors.toList());
    }

}
