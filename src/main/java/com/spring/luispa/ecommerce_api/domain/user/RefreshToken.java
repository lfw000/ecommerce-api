package com.spring.luispa.ecommerce_api.domain.user;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean revoked = false;

    private Instant revokedAt;

    private String revokedByIp;

    private String createdByIp;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public RefreshToken() {
        // No-args constructor
    }

    public RefreshToken(String token, User user, Instant expiryDate, String createdByIp) {
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
        this.createdByIp = createdByIp;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getRevokedByIp() {
        return revokedByIp;
    }

    public void setRevokedByIp(String revokedByIp) {
        this.revokedByIp = revokedByIp;
    }

    public String getCreatedByIp() {
        return createdByIp;
    }

    public void setCreatedByIp(String createdByIp) {
        this.createdByIp = createdByIp;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods

    public void revoke(String ip) {
        this.revoked = true;
        this.revokedAt = Instant.now();
        this.revokedByIp = ip;
        this.updatedAt = Instant.now();
    }

    public boolean isValid() {
        return !revoked && Instant.now().isBefore(expiryDate);
    }
}
