package com.w4p.parser.client;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.jetty.http.HttpHeader;

@Getter
@Setter
public class W4ClientHeader {

    private String header;
    private String value;

    public W4ClientHeader(String header, String value) {
        this.header = header;
        this.value = value;
    }

    public W4ClientHeader(HttpHeader header, String value) {
        this.header = header.toString();
        this.value = value;
    }
}
