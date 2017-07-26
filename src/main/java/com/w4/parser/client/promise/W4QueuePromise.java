package com.w4.parser.client.promise;

import com.w4.parser.client.W4QueueResult;

import java.util.List;

public interface W4QueuePromise {
    void complete(W4QueueResult<List> result);
}
