package com.w4p.parser.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(value={ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface W4Fetch {
    String[] url() default "";
    W4Parse[] href() default {};

    int depth() default 3;
    int maxFetch() default 10;

    int timeout() default 0;
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
