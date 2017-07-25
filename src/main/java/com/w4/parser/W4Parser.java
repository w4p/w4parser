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

    public static <T> W4Queue parse(Class<T> clazz) {
        if (clazz.isAnnotationPresent(W4Fetch.class)) {
            W4Fetch w4Xpath = clazz.getAnnotation(W4Fetch.class);
            if (w4Xpath.url().length > 0 &&  !w4Xpath.url()[0].isEmpty()) {
                String url = w4Xpath.url()[0];
                return parse(url, clazz);
            }
        }
        LOG.warn("W4Fetch url() is empty. Please add W4Fetch annotation to class {}", clazz.getCanonicalName());
        return null;
    }

    public static <T> W4Queue parse(String url, Class<T> clazz) {
        return W4Processor.url(url, clazz);
    }

    public static W4Queue queue() {
        return new W4Queue();
    }
}
