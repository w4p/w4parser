package com.w4.parser.client;

import com.w4.parser.processor.W4Parser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
