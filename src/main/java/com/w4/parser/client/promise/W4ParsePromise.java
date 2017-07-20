package com.w4.parser.client.promise;

public interface W4ParsePromise<T> {
    void complete(T t);
}
