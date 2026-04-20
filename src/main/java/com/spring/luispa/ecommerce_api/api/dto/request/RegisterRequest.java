package com.spring.luispa.ecommerce_api.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "New user registration request")
public class RegisterRequest {

    @Schema(description = "User email (unique)",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100)
    private String email;

    @Schema(description = "User password (minimum 8 characters)",
        example = "securePassword123!",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "password")
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 60, message = "Password must be between 8 and 60 characters")
    private String password;

    @Schema(description = "User's first name",
        example = "Johh",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;

    @Schema(description = "User's last name",
        example = "Smith",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}
