package com.w4.parser.client.queue;

import com.w4.parser.client.queue.W4QueueTask;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
public class W4TaskResult<T> {
    private W4QueueTask task;
    private List<T> model;

    public W4TaskResult(W4QueueTask task, List<T> model) {
        this.task = task;
        this.model = model;
    }

    public T getOne() {
        return this.model.get(0);
    }

    public List<T> getList() {
        return this.model;
    }

    public W4QueueTask getTask() {
        return task;
    }
}
