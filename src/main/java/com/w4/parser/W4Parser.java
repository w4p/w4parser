package com.w4.parser;

import com.w4.parser.annotations.W4Fetch;
import com.w4.parser.client.queue.W4Queue;
import com.w4.parser.processor.W4Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class W4Parser {
    private static final Logger LOG = LoggerFactory.getLogger(W4Parser.class);

    public static <T extends Object> W4Queue parse(T obj) {
        Class clazz = obj.getClass();
        return parse(clazz);
    }

    public static <T> W4Queue parse(Class<T> clazz) {
        if (clazz.isAnnotationPresent(W4Fetch.class)) {
            W4Fetch w4Xpath = clazz.getAnnotation(W4Fetch.class);
            if (w4Xpath.url().length > 0 &&  !w4Xpath.url()[0].isEmpty()) {
                String url = w4Xpath.url()[0];
                return url(url, clazz);
            }
        }
        LOG.warn("W4Fetch url() is empty. Please add W4Fetch annotation to class {}", clazz.getCanonicalName());
        return null;
    }

    public static <T> W4Queue url(String url, Class<T> clazz) {
        return W4Processor.url(url, clazz);
    }

    public static <T> W4Queue data(String html, Class<T> clazz) {
        return W4Processor.data(html, clazz);
    }

    public static W4Queue queue() {
        return new W4Queue();
    }
}
