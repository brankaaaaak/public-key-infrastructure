package com.example.publicKeyInfrastructure.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Getter
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne
    private User user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Getter
    private boolean used = false;

    public VerificationToken() {}

    public VerificationToken(User user, String token, Duration duration) {
        this.token = token;
        this.user = user;
        this.expiryDate = LocalDateTime.now().plus(duration);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }

    public void markAsUsed() {
        this.used = true;
    }
}
