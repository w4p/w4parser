package com.w4.parser.client;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.HttpCookieStore;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class W4Client {
    private static final Logger LOG = LoggerFactory.getLogger(W4Client.class);

    private static W4Client instance;
    private HttpClient jettyClient = new HttpClient(new SslContextFactory(true));
    private boolean jettyRunned = false;

    private W4Client() {
        jettyClient = new HttpClient(new SslContextFactory(true));
        jettyClient.setConnectBlocking(false);
        jettyClient.setMaxConnectionsPerDestination(5);
        jettyClient.setMaxRequestsQueuedPerDestination(500);
        jettyClient.setFollowRedirects(true);
        jettyClient.setMaxRedirects(3);
        jettyClient.setAddressResolutionTimeout(15000);
        jettyClient.setConnectTimeout(30000);
        jettyClient.setIdleTimeout(30000);
        jettyClient.setRemoveIdleDestinations(true);
        jettyClient.setRequestBufferSize(65536);
        jettyClient.setResponseBufferSize(65536);
        jettyClient.setTCPNoDelay(true);
        jettyClient.setCookieStore(new HttpCookieStore.Empty());

        try {
            jettyClient.start();
        } catch (Exception e) {
            LOG.error("Exception while creating HTTP client: {}", e.getMessage());
        }
    }

    public static W4Client get() {
        if (instance == null) {
            instance = new W4Client();
        }
        return instance;
    }

    public HttpClient client() {
        return jettyClient;
    }
}
