package com.w4.parser.client;

import com.w4.parser.client.promise.W4ParsePromise;
import com.w4.parser.client.queue.HasQueue;
import com.w4.parser.client.queue.W4Queue;
import com.w4.parser.processor.W4Parser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@NoArgsConstructor
public class W4Response {
    private String url;
    private int responseCode;
    private String content = "";

    private String error;

    private W4Request request;

    public W4Response(W4Request request) {
        this.request = request;
    }

    public <T> T parse(Class<T> clazz) {
        return W4Parser.parse(this.content, clazz, this.request.getQueue());
    }

    public <T> void parseAsync(Class<T> clazz, W4ParsePromise<T> promise) {
        W4Parser.parseAsync(this.content, clazz, this.request.getQueue(), promise);
    }
}
