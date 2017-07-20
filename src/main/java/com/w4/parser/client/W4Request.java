package com.w4.parser.client;

import lombok.Getter;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpVersion;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Getter
public class W4Request {

    private Request request;

    public W4Request(String url) {
        request = W4Client.get().client().newRequest(url);
    }

    public static W4Request url(String url) {
        return new W4Request(url);
    }

    public W4Request method(HttpMethod method) {
        this.request.method(method);
        return this;
    }

    public W4Request timeout(long timeout, TimeUnit timeUnit) {
        this.request.timeout(timeout, timeUnit);
        return this;
    }

    public W4Request version(HttpVersion version) {
        this.request.version(version);
        return this;
    }

    public W4Request header(HttpHeader httpHeader, String value) {
        this.request.header(httpHeader, value);
        return this;
    }

    public W4Request header(String httpHeader, String value) {
        this.request.header(httpHeader, value);
        return this;
    }

    public W4Request agent(String ua) {
        this.request.agent(ua);
        return this;
    }

    public W4Response fetch() {
        W4Response response = new W4Response();
        try {
            ContentResponse r = this.request.send();
            response.setResponseCode(r.getStatus());
            response.setContent(r.getContentAsString());
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            response.setError(e.getMessage());
        }

        return response;
    }

    public <T> T parse(Class<T> clazz) {
        W4Response response = fetch();
        return response.parse(clazz);
    }

}
