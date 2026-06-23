package com.spring.luispa.ecommerce_api.services;

import com.spring.luispa.ecommerce_api.api.dto.request.LoginRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.RefreshTokenRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.JwtResponse;
import com.spring.luispa.ecommerce_api.api.dto.response.RefreshTokenResponse;
import com.spring.luispa.ecommerce_api.api.dto.response.UserResponse;
import com.spring.luispa.ecommerce_api.domain.user.RefreshToken;
import com.spring.luispa.ecommerce_api.security.JwtUtils;
import com.spring.luispa.ecommerce_api.security.UserDetailsImpl;
import com.spring.luispa.ecommerce_api.shared.exception.InvalidCredentialsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils,
                       UserService userService,
                       RefreshTokenService refreshTokenService) {
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public JwtResponse authenticate(LoginRequest request, String clientIp) {
        try {
            logger.info("Login attempt for email: {}", request.getEmail());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            UserResponse userResponse = userService.findById(userDetails.getId());

            String accessToken = jwtUtils.generateJwtToken(authentication);

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId(), clientIp);

            logger.info("Login successful for user: {}", userDetails.getEmail());

            return new JwtResponse(accessToken, refreshToken.getToken(), userResponse);
        } catch (BadCredentialsException ex) {
            logger.warn("Failed login attempt for email: {}", request.getEmail());

            throw new InvalidCredentialsException();
        }
    }

    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request, String clientIp) {
        logger.info("Refresh token request from IP: {}", clientIp);

        RefreshToken refreshToken = refreshTokenService.verify(request.getRefreshToken(), clientIp);

        String newAccessToken = jwtUtils.generateTokenFromEmail(refreshToken.getUser().getEmail());

        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(refreshToken.getUser().getId(), clientIp);

        refreshTokenService.revoke(request.getRefreshToken(), clientIp);

        logger.info("Refresh token rotated for user: {}", refreshToken.getUser().getEmail());

        return new RefreshTokenResponse(
                newAccessToken,
                jwtUtils.getJwtExpirationMs() / 1000,
                newRefreshToken.getToken());
    }

    @Transactional
    public void logout(String refreshToken, String clientIp) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revoke(refreshToken, clientIp);
            logger.info("Logout successful, refresh token revoked from IP: {}", clientIp);
        }
    }
}
