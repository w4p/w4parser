package com.w4p.parser.client.promise;

import com.w4p.parser.client.W4QueueResult;

public interface W4QueuePromise {
    void complete(W4QueueResult result);
}
