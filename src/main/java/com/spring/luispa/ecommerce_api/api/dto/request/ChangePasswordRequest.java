package com.spring.luispa.ecommerce_api.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to change user password")
public class ChangePasswordRequest {

    @Schema(description = "Current password",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "password")
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @Schema(description = "New password (minimum 8 characters)",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "password")
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 60, message = "Password must be between 8 and 60 characters")
    private String newPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
