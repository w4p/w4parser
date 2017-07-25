package com.w4.parser.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(value={ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface W4Xpath {
    String[] path() default "";
    String defaultValue() default "";
    W4RegExp[] postProcess() default {};
    int maxCount() default  0;

    boolean html() default false;
    boolean xml() default false;
}
