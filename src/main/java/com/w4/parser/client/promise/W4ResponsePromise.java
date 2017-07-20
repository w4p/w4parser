package com.w4.parser.client.promise;

import com.w4.parser.client.W4Response;

public interface W4ResponsePromise {
    void complete(W4Response response);
}
