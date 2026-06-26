package com.spring.luispa.ecommerce_api.infrastructure.ratelimit;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimited {
    RateLimitType type() default RateLimitType.GENERAL;
    String key() default "";
}
