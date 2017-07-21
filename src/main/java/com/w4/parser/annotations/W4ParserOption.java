package com.w4.parser.annotations;

import java.util.concurrent.TimeUnit;

public @interface W4ParserOption {
    int timeout() default 30000;
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
