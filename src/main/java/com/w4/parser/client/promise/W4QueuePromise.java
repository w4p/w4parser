package com.w4.parser.client.promise;

import com.w4.parser.client.W4QueueResult;

public interface W4QueuePromise {
    void complete(W4QueueResult result);
}
