package com.spring.luispa.ecommerce_api.security;

import com.spring.luispa.ecommerce_api.shared.exception.*;
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

    public void validateJwtToken(String authToken) {
        if (authToken == null || authToken.isBlank()) {
            throw new JwtMalformedException("JWT token is null or empty");
        }

        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);

            Claims claims = claimsJws.getBody();

            Date expiration = claims.getExpiration();
            if (expiration == null) {
                throw new JwtMalformedException("JWT has no expiration date");
            }

            if (expiration.before(new Date())) {
                throw new JwtExpiredException(expiration);
            }

            Date issuedAt = claims.getIssuedAt();
            if (issuedAt != null && issuedAt.after(new Date())) {
                throw new JwtMalformedException("JWT token issued in the future");
            }

            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) {
                throw new JwtMalformedException("JWT token has no subject (email)");
            }

            logger.debug("JWT token is valid for user: {}", subject);
        } catch (ExpiredJwtException e) {
            throw new JwtExpiredException(e.getClaims().getExpiration());
        } catch (MalformedJwtException e) {
            throw new JwtMalformedException(e.getMessage());
        } catch (SignatureException e) {
            throw new JwtSignatureException(e.getMessage());
        } catch (UnsupportedJwtException e) {
            throw new JwtUnsupportedException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new JwtMalformedException(e.getMessage());
        }
    }

    public void validateJwtTokenWithUser(String authToken, UserDetailsService userDetailsService) {
        validateJwtToken(authToken);

        try {
            String email = getEmailFromJwtToken(authToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (!userDetails.isEnabled()) {
                throw new UserDisabledException(email);
            }

            if (!userDetails.isAccountNonExpired()) {
                throw new UserAccountExpiredException(email);
            }

            if (!userDetails.isAccountNonLocked()) {
                throw new UserLockedException(email);
            }
        } catch (UsernameNotFoundException e) {
            throw new UserNotFoundException();
        }
    }
}
