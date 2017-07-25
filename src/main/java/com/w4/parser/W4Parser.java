package com.w4.parser;

import com.w4.parser.annotations.W4Fetch;
import com.w4.parser.annotations.W4Xpath;
import com.w4.parser.client.promise.W4ParsePromise;
import com.w4.parser.client.queue.W4Queue;
import com.w4.parser.processor.W4Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class W4Parser {
    private static final Logger LOG = LoggerFactory.getLogger(W4Parser.class);

    public static <T> void parse(Class<T> clazz, W4ParsePromise<T> promise) {
        if (clazz.isAnnotationPresent(W4Fetch.class)) {
            W4Fetch w4Xpath = clazz.getAnnotation(W4Fetch.class);
            if (w4Xpath.url().length > 0 &&  !w4Xpath.url()[0].isEmpty()) {
                String url = w4Xpath.url()[0];
                parse(url, clazz, promise);
                return;
            }
        }
        LOG.warn("W4Fetch url() is empty. Please add W4Fetch annotation to class {}", clazz.getCanonicalName());
    }

    public static <T> T parse(Class<T> clazz) {
        CompletableFuture<T> future = new CompletableFuture<>();
        parse(clazz, (model) -> {
            future.complete(model);
        });
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    public static <T> void parse(String url, Class<T> clazz, W4ParsePromise promise) {
        W4Processor.url(url, clazz).get((T model) -> {
            promise.complete(model);
        });
    }
}
