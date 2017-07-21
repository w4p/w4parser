package com.w4.parser.client.queue;

import com.w4.parser.client.W4QueueResult;
import com.w4.parser.client.promise.W4QueueProgressPromise;
import com.w4.parser.client.promise.W4QueuePromise;
import com.w4.parser.processor.W4Parser;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class W4Queue {

    private List<W4QueueTask> requestList = new ArrayList<>();
    private Map<Integer, Integer> index = new HashMap<>();
    private W4QueueResult result = new W4QueueResult();

    private W4QueueProgressPromise progressPromise;
    private long timeout = 60000 * 10;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    public synchronized W4Queue data(String data, Class clazz) {
        W4QueueTask task = new W4QueueTask(clazz).setData(data);
        addQueue(task);
        return this;
    }

    public synchronized W4Queue url(String url, Class clazz) {
        W4QueueTask task = new W4QueueTask(clazz).setUrl(url);
        addQueue(task);
        return this;
    }

    public W4Queue onProgress(W4QueueProgressPromise progressPromise) {
        this.progressPromise = progressPromise;
        return this;
    }

    private void addQueue(W4QueueTask task) {
        requestList.add(task);
        result.add(null);
        this.index.put(task.hashCode(), requestList.size() - 1);
    }

    public W4QueueResult run() {
        CountDownLatch latch = new CountDownLatch(this.requestList.size());
        for (W4QueueTask task : this.requestList) {
            if (task.getUrl() != null) {
                W4Parser.url(task.getUrl()).parseAsync(task.getClazz(), model -> {
                    int idx = this.index.get(task.hashCode());
                    this.result.addResult(idx, model);
                    if (this.progressPromise != null) {
                        this.progressPromise.onProgress(task, model);
                    }
                    latch.countDown();
                });
            } else {
                W4Parser.data(task.getData()).parseAsync(task.getClazz(), model -> {
                    int idx = this.index.get(task.hashCode());
                    this.result.addResult(idx, model);
                    if (this.progressPromise != null) {
                        this.progressPromise.onProgress(task, model);
                    }
                    latch.countDown();
                });
            }
        }
        try {
            latch.await(this.timeout, this.timeUnit);
        } catch (InterruptedException e) {
        }
        return this.result;
    }

    public void run(W4QueuePromise w4QueuePromise) {
        CompletableFuture.runAsync(() -> w4QueuePromise.complete(run()));
    }
}
