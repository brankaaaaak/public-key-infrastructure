package com.example.publicKeyInfrastructure.controller;

import com.example.publicKeyInfrastructure.dto.*;
import com.example.publicKeyInfrastructure.model.User;
import com.example.publicKeyInfrastructure.model.UserWithRoleDTO;
import com.example.publicKeyInfrastructure.model.VerificationToken;
import com.example.publicKeyInfrastructure.repository.IUserRepository;
import com.example.publicKeyInfrastructure.repository.IVerificationTokenRepository;
import com.example.publicKeyInfrastructure.service.implementation.UserService;
import com.example.publicKeyInfrastructure.service.interfaces.IUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "https://localhost:4200")
public class UserController {
    @Autowired
    IUserService service;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IVerificationTokenRepository verificationTokenRepository;

    @PostMapping(value = "/register",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> registerUser(@RequestBody RegisterDTO dto){
        UserDTO newUser = service.register(dto);
        return ResponseEntity.ok(newUser);
    }
    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token) {
        Optional<VerificationToken> optionalToken = verificationTokenRepository.findByToken(token);
        if (optionalToken.isEmpty()) {
            return "Invalid verification token.";
        }

        VerificationToken verificationToken = optionalToken.get();

        if (verificationToken.isExpired()) {
            return "Verification link has expired.";
        }

        if (verificationToken.isUsed()) {
            return "This verification link has already been used.";
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationToken.markAsUsed();
        verificationTokenRepository.save(verificationToken);

        return "You are successfully verified!";
    }
    @PostMapping("/login")
    public ResponseEntity<TokenPair> login(@Valid @RequestBody LoginDTO request) {
        TokenPair tokenPair = service.login(request);
        return ResponseEntity.ok(tokenPair);
    }
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenDTO request) {
        TokenPair tokenPair = service.refreshToken(request);
        return ResponseEntity.ok(tokenPair);
    }

    @GetMapping("/all-users")
    public List<UserWithRoleDTO> getAllUsers() {
        System.out.println(service.getAllUsers());
        return service.getAllUsers();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        return service.getUserById(id);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}/upgrade")
    public ResponseEntity<UserDTO> upgradeUserToCA(@PathVariable Long id) {
        UserDTO upgraded = ((UserService) service).upgradeToCA(id);
        return ResponseEntity.ok(upgraded);
    }

    @GetMapping("/all-org/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserTableViewDTO>> getAllCAUsersForOrg(@PathVariable("id") Long orgId) {
        List<UserTableViewDTO> users =service.getUsersForOrganization(orgId);
        return ResponseEntity.ok(users);
    }
    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserTableViewDTO>> getAllCAUsers() {
        List<UserTableViewDTO> users =service.getAllCAUsers();
        return ResponseEntity.ok(users);
    }
    @GetMapping("/all-org/role-user/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserTableViewDTO>> getAllUsersForOrg(@PathVariable("id") Long orgId) {
        List<UserTableViewDTO> users =service.getAllUsersForOrganization(orgId);
        return ResponseEntity.ok(users);
    }
}

