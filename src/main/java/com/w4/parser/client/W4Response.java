package com.w4.parser.client;

import com.w4.parser.W4Parser;
import com.w4.parser.client.promise.W4ParsePromise;
import com.w4.parser.client.queue.W4QueueTask;
import com.w4.parser.processor.W4Processor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class W4Response {
    private String url;
    private int responseCode;
    private String content = "";

    private String error;

    private W4QueueTask queueTask;
    private Class modelClass;

    public W4Response(W4QueueTask<?> task) {
        this.queueTask = task;
        this.modelClass = task.getClazz();
    }

//    public <T> T parse(Class<T> clazz) {
//        return W4Processor.parse(this.content, clazz, this.queueTask);
//    }
//
//    public <T> void parseAsync(Class<T> clazz, W4ParsePromise<T> promise) {
//        W4Processor.parseAsync(this.content, this.queueTask, promise);
//    }

    public <T> void parse(W4ParsePromise<List<T>> promise) {
        try {
            W4Processor.parse(this, promise);
        } catch (Throwable e) {
            W4Parser.LOG.error(e.getMessage());
        }
    }
}
