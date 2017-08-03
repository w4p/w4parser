package com.w4.parser.client.promise;

import com.w4.parser.client.queue.W4QueueTask;
import com.w4.parser.client.queue.W4TaskResult;

import java.util.List;

public interface W4QueueProgressPromise {
    void onProgress(W4TaskResult taskResult);
}
