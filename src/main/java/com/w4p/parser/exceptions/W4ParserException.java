package com.w4p.parser.exceptions;


public class W4ParserException extends RuntimeException {
    public W4ParserException(String message) {
        super(message);
    }

    public W4ParserException(Throwable cause) {
        super(cause);
    }
}
