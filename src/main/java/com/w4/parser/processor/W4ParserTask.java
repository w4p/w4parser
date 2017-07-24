package com.w4.parser.processor;

import com.w4.parser.client.queue.W4Queue;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class W4ParserTask<T> {


    private W4Queue queue;
    private long startedAt = System.currentTimeMillis();
    private long stopAt;

    public W4ParserTask(Class<T> modelClass) {
        this.modelClass = modelClass;
    }

    public W4ParserTask(W4Queue queue, Class<T> modelClass) {
        this.queue = queue;
        this.modelClass = modelClass;
    }

    private Class<T> modelClass;
    private T model;

}
