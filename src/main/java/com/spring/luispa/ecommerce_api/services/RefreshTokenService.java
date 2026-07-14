package com.spring.luispa.ecommerce_api.services;

import com.spring.luispa.ecommerce_api.domain.user.RefreshToken;
import com.spring.luispa.ecommerce_api.domain.user.RefreshTokenRepository;
import com.spring.luispa.ecommerce_api.domain.user.User;
import com.spring.luispa.ecommerce_api.domain.user.UserRepository;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessRuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    @Value("${app.jwt.refresh-expiration-ms}")
    private int refreshExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId, String clientIp) {
        log.info("Creating refresh token: userId={}, ip={}", userId, clientIp);

        User user = userRepository.findById(userId).
                orElseThrow(() -> {
                    log.warn("User not found for refresh token creation: userId={}", userId);
                    return new BusinessRuleException("User not found", "USER_NOT_FOUND");
                });

        refreshTokenRepository.deleteByUserId(userId);

        log.debug("Revoked previous refresh token for userId: {}", userId);

        RefreshToken refreshToken = new RefreshToken(
                UUID.randomUUID().toString(),
                user,
                Instant.now().plusMillis(refreshExpirationMs),
                clientIp);

        RefreshToken savedRefreshToken = refreshTokenRepository.save(refreshToken);

        log.info("Refresh token created: tokenId={}, userId={}, expiresAt={}, ip={}",
                savedRefreshToken.getId(), userId, savedRefreshToken.getExpiryDate(), clientIp);

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken verify(String token, String clientIp) {
        log.debug("Verifying refresh token: token={}, ip={}", maskToken(token), clientIp);

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Invalid refresh token attempt: ip={}", clientIp);
                    return new BusinessRuleException("Invalid refresh token", "INVALID_REFRESH_TOKEN");
                });

        if (!refreshToken.isValid()) {
            log.warn("Refresh token is invalid: tokenId={}, userId={}, valid={}, revoked={}, ip={}",
                    refreshToken.getId(),
                    refreshToken.getUser().getId(),
                    refreshToken.isValid(),
                    refreshToken.isRevoked(),
                    clientIp);
            refreshToken.revoke(clientIp);
            refreshTokenRepository.save(refreshToken);

            log.debug("Refresh token revoked: tokenId={}, userId={}, ip={}",
                    refreshToken.getId(), refreshToken.getUser().getId(), clientIp);

            throw new BusinessRuleException("Refresh token expired or revoked", "INVALID_REFRESH_TOKEN");
        }

        log.info("Refresh token verified: tokenId={}, userId={}, ip={}",
                refreshToken.getId(), refreshToken.getUser().getId(), clientIp);

        return refreshToken;
    }

    @Transactional
    public void revoke(String token, String clientIp) {
        log.debug("Revoking refresh token: ip={}", clientIp);

        refreshTokenRepository.findByToken(token)
                .ifPresentOrElse(rt -> {
                    rt.revoke(clientIp);
                    refreshTokenRepository.save(rt);

                    log.info("Refresh token revoked: tokenId={}, userId={}, ip={}",
                            rt.getId(), rt.getUser().getId(), clientIp);
                },
                () -> {
                    log.debug("Refresh token not found for revocation: ip={}", clientIp);
                });
    }

    // Helper methods

    private String maskToken(String token) {
        if (token == null || token.length() < 12) {
            return "***";
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 6);
    }
}
