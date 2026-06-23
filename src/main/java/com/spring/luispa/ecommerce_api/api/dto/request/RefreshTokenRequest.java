package com.spring.luispa.ecommerce_api.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to refresh access token")
public class RefreshTokenRequest {

    @Schema(description = "Refresh token", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
