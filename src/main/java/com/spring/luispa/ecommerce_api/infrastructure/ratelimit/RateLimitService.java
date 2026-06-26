package com.spring.luispa.ecommerce_api.infrastructure.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Value("${rate-limiting.enabled:true}")
    private boolean enabled;

    @Value("${rate-limiting.login.capacity:5}")
    private int loginCapacity;

    @Value("${rate-limiting.login.refill-time:60}")
    private int loginRefillTime;

    @Value("${rate-limiting.register.capacity:10}")
    private int registerCapacity;

    @Value("${rate-limiting.register.refill-time:60}")
    private int registerRefillTime;

    @Value("${rate-limiting.general.capacity:100}")
    private int generalCapacity;

    @Value("${rate-limiting.general.refill-time:60}")
    private int generalRefillTime;

    @Value("${rate-limiting.order-creation.capacity:20}")
    private int orderCreationCapacity;

    @Value("${rate-limiting.order-creation.refill-time:60}")
    private int orderCreationRefillTime;

    @Value("${rate-limiting.payment-processing.capacity:10}")
    private int paymentProcessingCapacity;

    @Value("${rate-limiting.payment-processing.refill-time:60}")
    private int paymentProcessingRefillTime;

    private Bucket createBucket(int capacity, int refillTimeSeconds) {
        Refill refill = Refill.greedy(capacity, Duration.ofSeconds(refillTimeSeconds));
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public boolean isAllowed(String key, RateLimitType type) {
        if (!enabled) {
            return true;
        }

        Bucket bucket = cache.computeIfAbsent(key, k -> {
            switch (type) {
                case LOGIN:
                    return createBucket(loginCapacity, loginRefillTime);
                case REGISTER:
                    return createBucket(registerCapacity, registerRefillTime);
                case ORDER_CREATION:
                    return createBucket(orderCreationCapacity, orderCreationRefillTime);
                case PAYMENT_PROCESSING:
                    return createBucket(paymentProcessingCapacity, paymentProcessingRefillTime);
                default:
                    return createBucket(generalCapacity, generalRefillTime);
            }
        });

        return bucket.tryConsume(1);
    }

    public void reset(String key) {
        cache.remove(key);
    }

    public long getRemainingTokens(String key, RateLimitType type) {
        Bucket bucket = cache.get(key);
        if (bucket == null) {
            return getCapacity(type);
        }
        return bucket.getAvailableTokens();
    }

    public int getCapacity(RateLimitType type) {
        switch (type) {
            case LOGIN:
                return loginCapacity;
            case REGISTER:
                return registerCapacity;
            case ORDER_CREATION:
                return orderCreationCapacity;
            case PAYMENT_PROCESSING:
                return paymentProcessingCapacity;
            default:
                return generalCapacity;
        }
    }
}
