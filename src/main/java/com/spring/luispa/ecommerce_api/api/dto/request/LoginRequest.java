package com.spring.luispa.ecommerce_api.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "User authentication request")
public class LoginRequest {

    @Schema(description = "User email",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(description = "User password (minimum 8 characters)",
    example = "pasword123!",
    requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    private String password;

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
}
