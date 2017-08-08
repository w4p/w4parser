package com.w4p.parser.client.promise;

public interface W4QueueCompletePromise<T> {
    void complete(T object) throws IllegalAccessException;
}
