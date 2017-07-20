package com.w4.parser.client;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class W4HttpClient {

    HttpClient jettyClient = new HttpClient(new SslContextFactory(true));

    public W4HttpClient create() {
        return new W4HttpClient();
    }

    public W4HttpClient method(HttpMethod method) {
        return null; //ToDo
    }
}
