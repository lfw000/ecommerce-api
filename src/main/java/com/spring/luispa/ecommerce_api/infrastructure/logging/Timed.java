package com.spring.luispa.ecommerce_api.infrastructure.logging;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Timed {

    String value() default "";
    boolean logArgs() default false;
    boolean logResult() default false;
}
