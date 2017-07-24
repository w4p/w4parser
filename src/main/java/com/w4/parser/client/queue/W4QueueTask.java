package com.w4.parser.client.queue;

import com.w4.parser.client.W4Request;
import com.w4.parser.client.W4Response;
import com.w4.parser.client.promise.W4ParsePromise;
import com.w4.parser.client.promise.W4QueueCompletePromise;
import com.w4.parser.client.promise.W4QueueTaskPromise;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class W4QueueTask<T> {
    private String url;

    private W4Request w4Request;
    private W4Response w4Response;

    private Class<T> clazz;
//    private Object wrapModel;

    private W4ParsePromise<T> parsePromise;
//    private W4QueueCompletePromise<T> completeEvent;

    public W4QueueTask(Class<T> clazz) {
        this.clazz = clazz;
    }

    public W4QueueTask setData(String data, W4Queue queue) {
        this.w4Response = new W4Response(new W4Request(queue));
        this.w4Response.setContent(data);
        return this;
    }

    public W4QueueTask setUrl(String url) {
        this.url = url;
        this.w4Request = new W4Request(url);
        return this;
    }


}
