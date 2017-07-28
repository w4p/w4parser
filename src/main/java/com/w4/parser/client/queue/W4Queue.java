package com.w4.parser.client.queue;

import com.w4.parser.W4Parser;
import com.w4.parser.annotations.W4Fetch;
import com.w4.parser.annotations.W4Parse;
import com.w4.parser.client.W4QueueResult;
import com.w4.parser.client.W4Request;
import com.w4.parser.client.promise.W4ParsePromise;
import com.w4.parser.client.promise.W4QueueProgressPromise;
import com.w4.parser.client.promise.W4QueuePromise;
import com.w4.parser.client.promise.W4QueueTaskPromise;
import com.w4.parser.processor.W4Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class W4Queue {
    private static final Logger LOG = LoggerFactory.getLogger(W4Processor.class);

    private Queue<W4QueueTask> requestList = new ConcurrentLinkedQueue<>();
    private Map<Integer, Integer> index = new HashMap<>();
    private W4QueueResult result = new W4QueueResult();

    private W4QueueTask lastTask;

    private int maxThreads = 10;
    private int activeThreads = 0;
    private String userAgent;

    CountDownLatch latch;


    private W4QueueProgressPromise progressPromise;
    private long timeout = 60000 * 60 * 24;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    public synchronized W4Queue data(String data, Class clazz) {
        W4QueueTask task = new W4QueueTask(clazz, this).setData(data);
        addQueue(task);
        this.lastTask = task;
        return this;
    }

    public synchronized W4Queue url(String url, Class clazz) {
        W4QueueTask task = new W4QueueTask(clazz, this).setUrl(url);
        addQueue(task);
        this.lastTask = task;
        return this;
    }

    public W4Queue parse(Class clazz) {
        if (clazz.isAnnotationPresent(W4Fetch.class)) {
            W4Fetch w4Fetch = (W4Fetch) clazz.getAnnotation(W4Fetch.class);
            if (w4Fetch.url().length > 0 &&  !w4Fetch.url()[0].isEmpty()) {
                String url = w4Fetch.url()[0];
                return this.url(url, clazz);
            }
        }
        return this;
    }

    public W4Queue threads(int max) {
        this.maxThreads = max;
        return this;
    }

    public W4Queue agent(String ua) {
        this.userAgent = ua;
        return this;
    }

    public W4Queue timeout(long timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        return this;
    }

    public W4Request setup() {
        return (this.lastTask.getW4Request() != null) ? this.lastTask.getW4Request(): (new W4Request(this.lastTask));
    }

    public W4Queue onProgress(W4QueueProgressPromise progressPromise) {
        this.progressPromise = progressPromise;
        return this;
    }

    public W4Queue addInternalQueue(W4QueueTask task) {
        this.requestList.add(task);
        runTaskList(this.latch);
        return this;
    }

    private void addQueue(W4QueueTask task) {
        requestList.add(task);
        result.add(null);
        this.index.put(task.hashCode(), requestList.size() - 1);
    }

    public W4QueueResult run() {
        long startedAt = System.currentTimeMillis();
        latch = new CountDownLatch(this.requestList.size());
        runTaskList(latch);
        try {
            latch.await(this.timeout, this.timeUnit);
            this.result.setCompleteTime(System.currentTimeMillis() - startedAt);
        } catch (InterruptedException e) {
        }
        return this.result;
    }

    public void run(W4QueuePromise w4QueuePromise) {
        CompletableFuture.runAsync(() -> w4QueuePromise.complete(run()));
    }

    public <T extends Object> T get() {
        W4QueueResult<List<T>> result = run();
        return result.getFirst().get(0);
    }

    public <T extends Object> void get(W4ParsePromise<T> w4ParsePromise) {
        if (this.requestList.size() > 1) {
            LOG.warn("W4Processor queue contains {} items, but used only first", this.requestList.size());
        }
        run((result -> w4ParsePromise.complete((T) result.getFirst().get(0))));
    }

    public void runTaskList(CountDownLatch latch) {
        W4QueueTask task;
        while ((task = getTask()) != null && activeThreads < maxThreads) {
            runTask(latch, task, () -> {
                runTaskList(latch);
            });
        }
    }

    private W4QueueTask getTask() {
        if (activeThreads < maxThreads) {
            return this.requestList.poll();
        }
        return null;
    }

    private void runTask(CountDownLatch latch, W4QueueTask task, W4QueueTaskPromise taskPromise) {
        final W4ParsePromise internalPromise = task.getTaskPromise();
        W4ParsePromise<List> parsePromise = (list) -> {
            Integer idx = this.index.get(task.hashCode());
            if (idx != null) {
                this.result.addResult(idx, list);

                if (this.progressPromise != null) {
                    this.progressPromise.onProgress(task, list);
                }
                latch.countDown();
            }

            if (internalPromise != null) {
                internalPromise.complete(list);
            }
            taskPromise.taskCompleted();
        };

        task.setTaskPromise(parsePromise);
        task.run();
    }

    public synchronized void threadMonitor(int add) {
        this.activeThreads += add;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public int getElapsedTaskCount() {
        return this.requestList.size();
    }
}
