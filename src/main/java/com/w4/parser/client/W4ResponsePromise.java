package com.w4.parser.client;

public interface W4ResponsePromise {
    void complete(W4Response response);
}
