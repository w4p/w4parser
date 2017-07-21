package com.w4.parser.client;

import com.w4.parser.client.promise.W4ParsePromise;
import com.w4.parser.client.promise.W4ResponsePromise;
import lombok.Getter;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Getter
public class W4Request {
    private static final Logger LOG = LoggerFactory.getLogger(W4Request.class);

    private Request request;
    private String url;

    public W4Request(String url) {
        this.url = url;
        this.request = W4Client.get().client().newRequest(url);
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
            LOG.info("Fetch data from: {}", this.request.getURI());
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

    public void fetchAsync(W4ResponsePromise responsePromise) {
        LOG.info("Fetch data from: {}", this.request.getURI());
        this.request.send(new BufferingResponseListener(1024*1024*500) {
            final W4Response response = new W4Response();
            @Override
            public void onComplete(Result result) {
                response.setUrl(request.getURI().toString());
                try {
                    response.setResponseCode(result.getResponse().getStatus());
                    if (result.isSucceeded()) {
                        if (response.getResponseCode() == 200) {
                            response.setContent(getContentAsString());
                        }
                    }
                    Throwable t = result.getResponseFailure();
                    if (t != null) {
                        response.setError(t.getMessage());
                    }
                } catch (Throwable t) {
                    LOG.warn(t.getMessage(), request.getURI());
                } finally {
                    try {
                        responsePromise.complete(response);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        });
    }

    public <T> void parseAsync(Class<T> clazz, W4ParsePromise<T> promise) {
        fetchAsync(response -> promise.complete(response.parse(clazz)));
    }


}
