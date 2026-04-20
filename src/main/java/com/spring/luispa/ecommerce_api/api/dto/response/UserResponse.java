package com.spring.luispa.ecommerce_api.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Schema(description = "Response containing user data")
public class UserResponse {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "Email", example = "usuario@example.com")
    private String email;

    @Schema(description = "First Name", example = "Juan")
    private String firstName;

    @Schema(description = "Last Name", example = "Pérez")
    private String lastName;

    @Schema(description = "Account enabled", example = "true")
    private boolean enabled;

    @Schema(description = "List of user roles",
        example = "[\"ROLE_USER\", \"ROLE_ADMIN\"]")
    private Set<String> roles;

    @Schema(description = "List of user addresses")
    private List<AddressResponse> address;

    @Schema(description = "Creation date", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update date", example = "2024-01-20T14:25:00")
    private LocalDateTime updatedAt;

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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public List<AddressResponse> getAddress() {
        return address;
    }

    public void setAddress(List<AddressResponse> address) {
        this.address = address;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
