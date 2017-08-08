package com.w4p.parser.client.promise;

import com.w4p.parser.client.W4Response;

public interface W4ResponsePromise {
    void complete(W4Response response);
}
