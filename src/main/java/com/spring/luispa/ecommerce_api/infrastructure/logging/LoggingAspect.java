package com.spring.luispa.ecommerce_api.infrastructure.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    // MDC (Mapped Diagnostic Context) keys
    private static final String MDC_CORRELATION_ID = "correlationId";
    private static final String MDC_USER_ID = "requestId";
    private static final String MDC_IP = "clientIp";

    // Controller logging

    @Around("execution(* com.spring.luispa.ecommerce_api.api.controller.*.*(..))")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        String correlationId = UUID.randomUUID().toString();
        MDC.put(MDC_CORRELATION_ID, correlationId);

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = signature.getMethod().getName();

        log.info("[{}] {}.{} - Request started", correlationId, className, methodName);

        if (log.isDebugEnabled() && joinPoint.getArgs().length > 0) {
            log.debug("[{}] {}.{} - Args: {}", correlationId, className, methodName, truncateArgs(joinPoint.getArgs()));
        }

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            log.info("[{}] {}.{} - Completed in {}ms", correlationId, className, methodName, duration);

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[{}] {}.{} - Failed after {}ms: {}",  correlationId, className, methodName, duration,
                    e.getMessage(), e);
            throw e;
        } finally {
            MDC.remove(MDC_CORRELATION_ID);
            MDC.remove(MDC_USER_ID);
            MDC.remove(MDC_IP);
        }
    }

    // Service logging

    @Around("execution(* com.spring.luispa.ecommerce_api.services.*.*(..))")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        if (log.isDebugEnabled()) {
            log.debug("{}.{} - Args: {}", className, methodName, truncateArgs(joinPoint.getArgs()));
        }

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            if (duration > 1000) {
                log.warn("{}.{} - Completed in {}ms (SLOW)", className, methodName, duration);
            } else {
                log.debug("{}.{} - Completed in {}ms", className, methodName, duration);
            }

            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("{}.{} - Failed after {}ms: {}", className, methodName, duration, ex.getMessage(), ex);
            throw ex;
        }
    }

    // Logging methods with @Timed annotation

    @Around("@annotation(timed)")
    public Object logTimed(ProceedingJoinPoint joinPoint, Timed timed) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        String timerName = timed.value().isEmpty() ? methodName : timed.value();

        if (timed.logArgs() && log.isDebugEnabled()) {
            log.debug("[{}] - Args: {}", timerName, truncateArgs(joinPoint.getArgs()));
        }

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            if (timed.logResult()) {
                log.info("[{}] - Completed in {}ms - Result: {}", timerName, duration, result);
            } else {
                log.info("[{}] - Completed in {}ms", timerName, duration);
            }

            return result;
        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[{}] - Failed after {}ms: {}", timerName, duration, ex.getMessage(), ex);
            throw ex;
        }
    }

    // Helper methods

    private String truncateArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        String argsStr = Arrays.toString(args);
        return argsStr.length() > 500 ? argsStr.substring(0, 500) + "..." : argsStr;
    }

    public void setUserIdInMDC(Long userId) {
        if (userId != null) {
            MDC.put(MDC_USER_ID, userId.toString());
        }
    }

    public void setClientIpInMDC(String ip) {
        if (ip != null) {
            MDC.put(MDC_IP, ip);
        }
    }

    public void clearMDC() {
        MDC.remove(MDC_CORRELATION_ID);
        MDC.remove(MDC_USER_ID);
        MDC.remove(MDC_IP);
    }
}
