package com.spring.luispa.ecommerce_api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private int jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .subject(userPrincipal.getEmail())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String getEmailFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        if (authToken == null || authToken.isBlank()) {
            logger.warn("JWT token is null or empty");
            return false;
        }

        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build().parseClaimsJws(authToken);

            Claims claims = claimsJws.getBody();

            Date expiration = claims.getExpiration();
            if (expiration == null) {
                logger.warn("JWT has no expiration date");
                return false;
            }

            if (expiration.before(new Date())) {
                logger.warn("JWT token is expired. Expired at: {}", expiration);
                return false;
            }

            Date issuedAt = claims.getIssuedAt();
            if (issuedAt != null && issuedAt.after(new Date())) {
                logger.warn("JWT token issued in the future: {}", issuedAt);
                return false;
            }

            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) {
                logger.warn("JWT token has no subject (email)");
                return false;
            }

            logger.debug("JWT token is valid for user: {}", subject);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token expired at: {}", e.getClaims().getExpiration());
            return false;
        } catch (MalformedJwtException e) {
            logger.warn("Malformed JWT token: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            logger.warn("Invalid JWWT signature: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            logger.warn("Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            logger.warn("JWT claims string is empty: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateJwtTokenWithUser(String authToken, UserDetailsService userDetailsService) {
        if (!validateJwtToken(authToken)) {
            return false;
        }

        try {
            String email = getEmailFromJwtToken(authToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (!userDetails.isEnabled()) {
                logger.warn("User is disabled: {}", email);
                return false;
            }

            if (!userDetails.isAccountNonExpired()) {
                logger.warn("User account is expired: {}", email);
                return false;
            }

            if (!userDetails.isAccountNonLocked()) {
                logger.warn("User account is locked: {}", email);
                return false;
            }

            return true;
        } catch (UsernameNotFoundException e) {
            logger.warn("User not found for JWT token: {}", e.getMessage());
            return false;
        }
    }
}
