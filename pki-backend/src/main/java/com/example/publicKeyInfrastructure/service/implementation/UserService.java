package com.example.publicKeyInfrastructure.service.implementation;

import com.example.publicKeyInfrastructure.dto.*;
import com.example.publicKeyInfrastructure.model.Role;
import com.example.publicKeyInfrastructure.model.User;
import com.example.publicKeyInfrastructure.model.VerificationToken;
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
import java.util.Set;

import java.util.UUID;


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

    @Override
    public UserDTO register(RegisterDTO request) {
        if (this.userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with provided email already exists!");
        }
        User newUser = new User(request);
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

        String confirmationUrl = "http://localhost:8080/api/users/verify-email?token=" + token;
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

}
