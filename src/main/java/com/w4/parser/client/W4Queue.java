package com.w4.parser.client;

import com.w4.parser.processor.W4Parser;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class W4Queue {

    private List<W4QueueTask> requestList = new ArrayList<>();
    private Map<Integer, Integer> index = new HashMap<>();
    private W4QueueResult result = new W4QueueResult();

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
                    latch.countDown();
                });
            } else {
                W4Parser.data(task.getData()).parseAsync(task.getClazz(), model -> {
                    int idx = this.index.get(task.hashCode());
                    this.result.addResult(idx, model);
                    latch.countDown();
                });
            }
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
        }
        return this.result;
    }


    @Getter
    @Setter
    private static class W4QueueTask<T> {
        private String url;
        private String data;
        private Class<T> clazz;

        public W4QueueTask(Class<T> clazz) {
            this.clazz = clazz;
        }

        public W4QueueTask setData(String data) {
            this.data = data;
            return this;
        }

        public W4QueueTask setUrl(String url) {
            this.url = url;
            return this;
        }
    }
}
