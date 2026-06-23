package com.spring.luispa.ecommerce_api.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "JWT authentication response")
public class JwtResponse {

    @Schema(description = "JWT token",
        example = "eyJhbGciOiJIUzUxMiJ9. eyJzdWIiOiJ1c3VhcmlvQGV4YW1wbGUuY29tIiwiaWF0IjoxNzMzMjY0MjAwLCJleHAiOjE3MzMzNTA2MDB9.abc123...")
    private String token;

    @Schema(description = "Token type",
        example = "Bearer")
    private String type = "Bearer";

    @Schema(description = "Refresh token")
    private String refreshToken;

    @Schema(description = "User ID",
            example = "1")
    private Long id;

    @Schema(description = "User email",
            example = "usuario@example.com")
    private String email;

    @Schema(description = "First name",
            example = "Juan")
    private String firstName;

    @Schema(description = "Last name",
            example = "Pérez")
    private String lastName;

    @Schema(description = "User roles", example = "[\"ROLE_USER\"]")
    private Set<String> roles;

    @Schema(description = "Token expiration date", example = "2024-12-05T10:00:00")
    private LocalDateTime expiresAt;

    public JwtResponse(String token, String refreshToken, UserResponse user) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.roles = user.getRoles();
    }

    public JwtResponse(String token, UserResponse user) {
        this(token, null, user);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
