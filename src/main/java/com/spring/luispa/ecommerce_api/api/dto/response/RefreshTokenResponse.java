package com.spring.luispa.ecommerce_api.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response with new access token")
public class RefreshTokenResponse {

    @Schema(description = "New access token")
    private String accessToken;

    @Schema(description = "Token type",
        example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Expiration time in seconds")
    private int expiresIn;

    @Schema(description = "New refresh token (if rotation is enabled)")
    private String refreshToken;

    public RefreshTokenResponse(String accessToken, int expiresIn, String refreshToken) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
