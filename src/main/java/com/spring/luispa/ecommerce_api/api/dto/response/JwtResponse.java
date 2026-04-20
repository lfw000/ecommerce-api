package com.spring.luispa.ecommerce_api.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "Authentication response with JWT token")
public class JwtResponse {

    @Schema(description = "JWT token",
        example = "eyJhbGciOiJIUzUxMiJ9. eyJzdWIiOiJ1c3VhcmlvQGV4YW1wbGUuY29tIiwiaWF0IjoxNzMzMjY0MjAwLCJleHAiOjE3MzMzNTA2MDB9.abc123...")
    private String token;

    @Schema(description = "Token type",
        example = "Bearer")
    private String type = "Bearer";

    @Schema(description = "User ID",
            example = "1")
    private Long id;

    @Schema(description = "User email",
            example = "usuario@example.com")
    private String email;

    @Schema(description = "User first name",
            example = "Juan")
    private String firstName;

    @Schema(description = "User last name", example = "Pérez")
    private String lastName;

    @Schema(description = "User roles", example = "[\"ROLE_USER\"]")
    private Set<String> roles;

    @Schema(description = "Token expiration date", example = "2024-12-05T10:00:00")
    private LocalDateTime expiresAt;

    public JwtResponse(String token, UserResponse user) {
        this.token = token;
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.roles = user.getRoles();
    }

    public JwtResponse(String token, UserResponse user, LocalDateTime expiresAt) {
        this(token, user);
        this.expiresAt = expiresAt;
    }

    public JwtResponse(String token, Long id, String email, java.util.Set<String> roles) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.roles = roles;
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
