package com.spring.luispa.ecommerce_api.infrastructure.ratelimit;

import com.spring.luispa.ecommerce_api.security.UserDetailsImpl;
import com.spring.luispa.ecommerce_api.shared.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class RateLimitAspect {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitAspect.class);

    private final RateLimitService rateLimitService;

    public RateLimitAspect(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Around("@annotation(rateLimited)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimited rateLimited) throws Throwable {
        String key = buildKey(rateLimited);

        logger.debug("Checking rate limit for key {}", key);

        if (!rateLimitService.isAllowed(key, rateLimited.type())) {
            logger.warn("Rate limit exceeded for key {}", key);
            throw new RateLimitExceededException();
        }

        Object result = joinPoint.proceed();

        HttpServletResponse response = getResponse();
        if (response != null) {
            long remaining = rateLimitService.getRemainingTokens(key, rateLimited.type());
            int limit = rateLimitService.getCapacity(rateLimited.type());
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(remaining));
            response.setHeader("X-Rate-Limit-Limit", String.valueOf(limit));
        }

        return result;
    }

    private String buildKey(RateLimited rateLimited) {
        if (!rateLimited.key().isEmpty()) {
            return rateLimited.key();
        }

        String ip = getClientIp();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetailsImpl) {
                Long userId = ((UserDetailsImpl) principal).getId();
                return ip + ":" + userId;
            }
        }

        return ip;
    }

    private String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return "unknown";
        }

        String ip =  request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private HttpServletRequest getRequest() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    private HttpServletResponse getResponse() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
        } catch (Exception e) {
            return null;
        }
    }
}
