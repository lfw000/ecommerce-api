package com.spring.luispa.ecommerce_api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spring.luispa.ecommerce_api.api.dto.response.ErrorResponse;
import com.spring.luispa.ecommerce_api.shared.exception.*;
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
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.objectMapper.setDateFormat(new StdDateFormat());
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

    private void sendErrorResponse(HttpServletResponse response, int status, String errorCode, String message) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(
                status,
                getHttpStatusName(status),
                errorCode,
                message,
                null
        );

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private String getHttpStatusName(int status) {
        switch (status) {
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 429: return "Too Many Requests";
            case 500: return "Internal Server Error";
            default: return "Error";
        }
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
                try {
                    jwtUtils.validateJwtTokenWithUser(jwt, userDetailsService);
                } catch (JwtExpiredException ex) {
                    logger.warn("JWT token expired: {}", ex.getMessage());
                    sendErrorResponse(
                            response,
                            401,
                            "JWT_EXPIRED",
                            "Your session has expired. Please refresh your token.");
                    return;
                } catch (JwtMalformedException ex) {
                    logger.warn("JWT token malformed: {}", ex.getMessage());
                    sendErrorResponse(
                            response,
                            400,
                            "JWT_MALFORMED",
                            "Invalid token format");
                    return;
                } catch (JwtUnsupportedException ex) {
                    logger.warn("JWT token unsupported: {}", ex.getMessage());
                    sendErrorResponse(
                            response,
                            400,
                            "JWT_UNSUPPORTED",
                            "Unsupported token format");
                    return;
                } catch (UserDisabledException ex) {
                    logger.warn("User is disabled: {}", ex.getMessage());
                    sendErrorResponse(
                            response,
                            403,
                            "USER_DISABLED",
                            "Your account has been disabled");
                    return;
                } catch (UserLockedException ex) {
                    logger.warn("User account is locked: {}", ex.getMessage());
                    sendErrorResponse(
                            response,
                            403,
                            "USER_LOCKED",
                            "Your account has been locked");
                    return;
                } catch (UserNotFoundException ex) {
                    logger.warn("User not found: {}", ex.getMessage());
                    sendErrorResponse(
                            response,
                            401,
                            "USER_NOT_FOUND",
                            "User not found");
                    return;
                }

                String email = jwtUtils.getEmailFromToken(jwt);
                UserDetails userDetails;

                try {
                    userDetails = userDetailsService.loadUserByUsername(email);
                } catch (UsernameNotFoundException e) {
                    logger.warn("User not found for JWT token: {}", email);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("User not found");
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
