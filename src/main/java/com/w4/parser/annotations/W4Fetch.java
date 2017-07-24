package com.w4.parser.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(value={ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface W4Fetch {
    String[] url() default "";
    String[] path() default "";

    int maxDepth() default 3;
    int maxFetch() default 10;

    int timeout() default 30000;
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
