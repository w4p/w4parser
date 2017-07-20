package com.w4.parser.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface W4Xpath {
    String[] path();
    String defaultValue() default "";
    W4RegExp[] postProcessValue() default {};
    int maxCount() default  0;
}
