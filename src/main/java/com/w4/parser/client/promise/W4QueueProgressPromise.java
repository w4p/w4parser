package com.w4.parser.client.promise;

import com.w4.parser.client.W4QueueResult;
import com.w4.parser.client.queue.W4QueueTask;

public interface W4QueueProgressPromise {
    void onProgress(W4QueueTask task, Object model);
}
