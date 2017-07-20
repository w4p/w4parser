package com.w4.parser.client;

public interface W4ParsePromise<T> {
    void complete(T t);
}
