package com.w4.parser.annotations;

public @interface W4Client {
    int timeout() default 30000;
}
