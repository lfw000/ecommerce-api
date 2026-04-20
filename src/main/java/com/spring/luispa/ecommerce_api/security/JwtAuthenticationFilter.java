package com.spring.luispa.ecommerce_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            String token = headerAuth.substring(7);
            logger.debug("JWT token extracted from Authorization header");
            return token;
        }

        String tokenParam = request.getParameter( "token");
        if (StringUtils.hasText(tokenParam)) {
            logger.debug("JWT token extracted from query parameter");
            return tokenParam;
        }

        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/api/auth") || path.startsWith("/api/public")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = parseJwt(request);

            if (jwt != null) {
                if (!jwtUtils.validateJwtToken(jwt)) {
                    logger.warn("Invalid JWT token for request: {}", path);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Invalid or expired JWT token");
                    return;
                }

                String email = jwtUtils.getEmailFromJwtToken(jwt);
                logger.debug("JWT token valid for user: {}", email);

                UserDetails userDetails;

                try {
                    userDetails = userDetailsService.loadUserByUsername(email);
                } catch (UsernameNotFoundException e) {
                    logger.warn("User not found for JWT token: {}", email);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("User not found");
                    return;
                }

                if (!userDetails.isEnabled()) {
                    logger.warn("User is disabled: {}", email);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("User account is disabled");
                    return;
                }

                if (!userDetails.isAccountNonLocked()) {
                    logger.warn("User account is locked: {}", email);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("User account is locked");
                    return;
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("User authenticated: {}", email);
            } else {
                logger.debug("No JWT token found for request: {}", path);
            }
        } catch (Exception ex) {
            logger.error("Cannot set user authentication: {}", ex.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Authentication failed");
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        return path.startsWith("/api/auth") || path.startsWith("/api/public");
    }
}
