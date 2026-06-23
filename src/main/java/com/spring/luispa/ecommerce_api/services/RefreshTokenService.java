package com.spring.luispa.ecommerce_api.services;

import com.spring.luispa.ecommerce_api.domain.user.RefreshToken;
import com.spring.luispa.ecommerce_api.domain.user.RefreshTokenRepository;
import com.spring.luispa.ecommerce_api.domain.user.User;
import com.spring.luispa.ecommerce_api.domain.user.UserRepository;
import com.spring.luispa.ecommerce_api.shared.exception.BusinessRuleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

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
        User user = userRepository.findById(userId).
                orElseThrow(() -> new BusinessRuleException("User not found", "USER_NOT_FOUND"));

        refreshTokenRepository.deleteByUserId(userId);

        RefreshToken refreshToken = new RefreshToken(
                UUID.randomUUID().toString(),
                user,
                Instant.now().plusMillis(refreshExpirationMs),
                clientIp);

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken verify(String token, String clientIp) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessRuleException("Invalid refresh token", "INVALID_REFRESH_TOKEN"));

        if (!refreshToken.isValid()) {
            refreshToken.revoke(clientIp);
            refreshTokenRepository.save(refreshToken);
            throw new BusinessRuleException("Refresh token expired or revoked", "INVALID_REFRESH_TOKEN");
        }

        return refreshToken;
    }

    @Transactional
    public void revoke(String token, String clientIp) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(rt -> {
                    rt.revoke(clientIp);
                    refreshTokenRepository.save(rt);
                });
    }
}
