package com.w4p.parser.client.promise;

public interface W4ParsePromise<T> {
    void complete(T t);
}
