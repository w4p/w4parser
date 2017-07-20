package com.w4.parser.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface W4Xpath {
    String[] path() default "";
    String defaultValue() default "";
    W4RegExp[] postProcess() default {};
    int maxCount() default  0;

    String[] followURL() default {};
    int maxFollowCount() default 5;
    int maxFollowDepth() default 3;
    boolean followAsync() default true;
}
