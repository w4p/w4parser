package com.w4.parser.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value={ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface W4Parse {
    String[] xpath() default "";
    String defaultValue() default "";
    W4RegExp[] postProcess() default {};
    int maxCount() default  0;

    boolean html() default false;
    boolean useXMLParser() default true;
}
