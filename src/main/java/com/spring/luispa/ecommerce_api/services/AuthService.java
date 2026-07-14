package com.spring.luispa.ecommerce_api.services;

import com.spring.luispa.ecommerce_api.api.dto.request.LoginRequest;
import com.spring.luispa.ecommerce_api.api.dto.request.RefreshTokenRequest;
import com.spring.luispa.ecommerce_api.api.dto.response.JwtResponse;
import com.spring.luispa.ecommerce_api.api.dto.response.RefreshTokenResponse;
import com.spring.luispa.ecommerce_api.api.dto.response.UserResponse;
import com.spring.luispa.ecommerce_api.domain.user.RefreshToken;
import com.spring.luispa.ecommerce_api.infrastructure.logging.LoggingAspect;
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
    private final LoggingAspect loggingAspect;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils,
                       UserService userService,
                       RefreshTokenService refreshTokenService, LoggingAspect loggingAspect) {
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.loggingAspect = loggingAspect;
    }

    @Transactional
    public JwtResponse authenticate(LoginRequest request, String clientIp) {
        logger.info("Login attempt: email={}, ip={}", request.getEmail(), clientIp);

        try {
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
        logger.info("Token refresh request: ip={}", clientIp);

        RefreshToken refreshToken = refreshTokenService.verify(request.getRefreshToken(), clientIp);

        loggingAspect.setUserIdInMDC(refreshToken.getUser().getId());

        String newAccessToken = jwtUtils.generateTokenFromEmail(refreshToken.getUser().getEmail());

        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(refreshToken.getUser().getId(), clientIp);

        refreshTokenService.revoke(request.getRefreshToken(), clientIp);

        logger.info("Token refresh successful: userId={}, ip={}", refreshToken.getUser().getId(), clientIp);

        return new RefreshTokenResponse(
                newAccessToken,
                jwtUtils.getJwtExpirationMs() / 1000,
                newRefreshToken.getToken());
    }

    @Transactional
    public void logout(String refreshToken, String clientIp) {
        logger.info("Logout request: ip={}", clientIp);

        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revoke(refreshToken, clientIp);
            logger.info("Logout successful, refresh token revoked from IP: {}", clientIp);
        } else {
            logger.debug("Logout called without refresh token, ip={}", clientIp);
        }
    }
}
