package com.w4p.parser.client.promise;

import com.w4p.parser.client.queue.W4TaskResult;

public interface W4QueueProgressPromise {
    void onProgress(W4TaskResult taskResult);
}
