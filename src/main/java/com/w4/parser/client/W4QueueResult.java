package com.w4.parser.client;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;

@Getter
@Setter
public class W4QueueResult <T extends Object> extends LinkedList<T> {

    private long completeTime;

    public synchronized void addResult(int idx, T model) {
        this.set(idx, model);
    }
}
