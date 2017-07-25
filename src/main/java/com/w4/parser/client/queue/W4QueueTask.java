package com.w4.parser.client.queue;

import com.w4.parser.client.W4QueueResult;
import com.w4.parser.client.W4Request;
import com.w4.parser.client.W4Response;
import com.w4.parser.client.promise.W4ParsePromise;
import com.w4.parser.client.promise.W4QueueCompletePromise;
import com.w4.parser.client.promise.W4QueueTaskPromise;
import com.w4.parser.processor.W4Processor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

@Getter
@Setter
public class W4QueueTask<T> {
    private static final Logger LOG = LoggerFactory.getLogger(W4QueueTask.class);

    private W4Queue queue;
    private W4Request w4Request;
    private W4Response w4Response;

    private W4QueueTask parent; //used for interbal @W4Fetch
    private W4ParsePromise<T> taskPromise;

    private Class<T> clazz;

    private long startedAt;
    private long stopedAt;

    private int depth = 0;

    public W4QueueTask(Class<T> clazz, W4Queue queue) {
        this.queue = queue;
        this.clazz = clazz;
    }

    public W4QueueTask setData(String data) {
        this.w4Response = new W4Response();
        this.w4Response.setContent(data);
        this.w4Response.setQueueTask(this);
        return this;
    }

    public W4QueueTask setUrl(String url) {
        this.queue = queue;
        this.w4Request = new W4Request(this, url);
        return this;
    }

    private void run(W4ParsePromise<T> promise) {
        if (promise != null) {
//            if (this.taskPromise == null) {
//                this.taskPromise = promise;
//            }
            this.startedAt = System.currentTimeMillis();
            if (this.w4Request != null) {
                this.w4Request.fetchAsync((response -> {
                    this.w4Response = response;
                    this.w4Response.parse(promise);
                }));
            } else {
                CompletableFuture.runAsync(() -> {
                    if (this.w4Response != null) {
                        this.w4Response.parse(promise);
                    } else {
                        promise.complete(null);
                    }
                });
            }
        } else {
            LOG.error("Queue task promise should be not null");
        }
    }

    public void run() {
        run(this.taskPromise);
    }

}
