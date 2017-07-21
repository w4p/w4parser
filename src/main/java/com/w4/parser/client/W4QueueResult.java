package com.w4.parser.client;

import java.util.LinkedList;

public class W4QueueResult <T extends Object> extends LinkedList<T> {

    public synchronized void addResult(int idx, T model) {
        this.set(idx, model);
    }
}
