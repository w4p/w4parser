package com.w4.parser.client.queue;

import com.w4.parser.client.W4QueueResult;
import com.w4.parser.client.W4Request;
import com.w4.parser.client.promise.W4ParsePromise;
import com.w4.parser.client.promise.W4QueueProgressPromise;
import com.w4.parser.client.promise.W4QueuePromise;
import com.w4.parser.client.promise.W4QueueTaskPromise;
import com.w4.parser.processor.W4Parser;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class W4Queue {

    private Queue<W4QueueTask> requestList = new ConcurrentLinkedQueue<>();
    private Map<Integer, Integer> index = new HashMap<>();
    private W4QueueResult result = new W4QueueResult();

    private W4QueueTask lastTask;

    private int maxThreads = 10;
    private int activeThreads = 0;



    private W4QueueProgressPromise progressPromise;
    private long timeout = 60000 * 10;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    public synchronized W4Queue data(String data, Class clazz) {
        W4QueueTask task = new W4QueueTask(clazz).setData(data);
        addQueue(task);
        this.lastTask = task;
        return this;
    }

    public synchronized W4Queue url(String url, Class clazz) {
        W4QueueTask task = new W4QueueTask(clazz).setUrl(url);
        task.getW4Request().setQueue(this);
        addQueue(task);
        this.lastTask = task;
        return this;
    }

    public W4Queue threads(int max) {
        this.maxThreads = max;
        return this;
    }

    public W4Queue timeout(long timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        return this;
    }

    public W4Request setup() {
        return (this.lastTask.getW4Request() != null) ? this.lastTask.getW4Request(): (new W4Request(this));
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
        runTaskList(latch);
        try {
            latch.await(this.timeout, this.timeUnit);
        } catch (InterruptedException e) {
        }
        return this.result;
    }

    public void run(W4QueuePromise w4QueuePromise) {
        CompletableFuture.runAsync(() -> w4QueuePromise.complete(run()));
    }

    public void runTaskList(CountDownLatch latch) {
        W4QueueTask task;
        while ((task = getTask()) != null && activeThreads < maxThreads) {
            threadMonitor(1);
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

        W4ParsePromise parsePromise = (model) -> {
            int idx = this.index.get(task.hashCode());
            this.result.addResult(idx, model);
            if (this.progressPromise != null) {
                this.progressPromise.onProgress(task, model);
            }
            threadMonitor(-1);
            latch.countDown();

            taskPromise.taskCompleted();
        };

        if (task.getW4Request() != null) {
            task.getW4Request().parseAsync(task.getClazz(), parsePromise);
        } else {
            task.getW4Response().parseAsync(task.getClazz(), parsePromise);
        }


    }

    private synchronized void threadMonitor(int add) {
        this.activeThreads += add;
    }
}
