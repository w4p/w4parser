package com.w4.parser.client.promise;

public interface W4QueueCompletePromise<T> {
    void complete(T object);
}
