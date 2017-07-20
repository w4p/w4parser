package com.w4.parser.client;

import com.w4.parser.client.promise.W4ParsePromise;
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

    public <T> T parse(Class<T> clazz) {
        return W4Parser.parse(this.content, clazz);
    }

    public <T> void parseAsync(Class<T> clazz, W4ParsePromise<T> promise) {
        W4Parser.parseAsync(this.content, clazz, promise);
    }
}
