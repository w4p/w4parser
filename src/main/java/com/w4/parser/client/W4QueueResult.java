package com.w4.parser.client;

import com.w4.parser.client.queue.W4QueueTask;
import com.w4.parser.client.queue.W4TaskResult;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class W4QueueResult<T> extends LinkedList<W4TaskResult<T>> {

    private long completeTime;

    public synchronized void addResult(int idx, W4TaskResult<T> taskResult) {
        this.set(idx, taskResult);
    }

    public synchronized void addResult(int idx, W4QueueTask task, List<T> model) {
        this.set(idx, new W4TaskResult<>(task, model));
    }

}
