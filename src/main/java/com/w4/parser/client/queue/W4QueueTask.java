package com.w4.parser.client.queue;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class W4QueueTask<T> {
    private String url;
    private String data;
    private Class<T> clazz;

    public W4QueueTask(Class<T> clazz) {
        this.clazz = clazz;
    }

    public W4QueueTask setData(String data) {
        this.data = data;
        return this;
    }

    public W4QueueTask setUrl(String url) {
        this.url = url;
        return this;
    }
}
