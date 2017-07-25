package com.w4.parser.processor;

import com.w4.parser.adapters.TypeAdapters;
import com.w4.parser.annotations.W4Fetch;
import com.w4.parser.annotations.W4RegExp;
import com.w4.parser.annotations.W4Xpath;
import com.w4.parser.client.W4Response;
import com.w4.parser.client.queue.W4Queue;
import com.w4.parser.client.promise.W4ParsePromise;
import com.w4.parser.client.queue.W4QueueTask;
import com.w4.parser.exceptions.W4ParserException;
import com.w4.parser.jpath.W4JPath;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class W4Processor {
    private static final Logger LOG = LoggerFactory.getLogger(W4Processor.class);

    public static W4Queue queue() {
        return new W4Queue();
    }

//    public static W4Request url(String url) {
//        return W4Request.url(url);
//    }

    public static <T> W4Queue url(String url, Class<T> clazz) {
        W4Queue queue = new W4Queue();
        return queue.url(url, clazz);
    }

    public static <T> W4Queue data(String html, Class<T> clazz) {
        W4Queue queue = new W4Queue();
        return queue.data(html, clazz);
    }

//    public static <T> T data(String html, Class<T> clazz) {
//        W4Queue queue = new W4Queue();
//        W4Response response = new W4Response(new W4Request(queue));
//        response.setContent(html);
//        return response.parse(clazz);
//    }

//    public static <T> CompletableFuture<T> parseAsync(String html, Class<T> clazz, W4Queue queue) throws W4ParserException {
//        final CompletableFuture<T> future = new CompletableFuture<>();
//        parseAsync(html, clazz, queue, (model) -> {
//            future.completedFuture(model);
//        });
//        return future;
//    }
//
//    public static <T> void parseAsync(String html, W4QueueTask<T> task, W4ParsePromise promise)
//            throws W4ParserException {
//        T model = parse(html, task, promise);
//        promise.complete(model);
//    }

//    public static <T> T parse(String html, Class<T> clazz,  W4QueueTask<T> task) throws W4ParserException {
////        W4ParserTask<T> parserTask = new W4ParserTask(queue, clazz);
//        return parse(html, task);
//    }

    public static <T> void parse(W4Response w4Response, W4ParsePromise<T> promise) throws W4ParserException {
        W4QueueTask task = w4Response.getQueueTask();
        Class<T> clazz = task.getClazz();
        try {
            Element document = Jsoup.parse(w4Response.getContent());

            if (clazz.isAnnotationPresent(W4Xpath.class)) {
                W4Xpath w4Xpath = clazz.getAnnotation(W4Xpath.class);
                if (w4Xpath.path().length > 0 && !w4Xpath.path()[0].isEmpty()) {
                    W4JPath w4JPath = new W4JPath(w4Xpath, w4Xpath.path()[0]);
                    document = document.select(w4JPath.getPath()).first();
                }
            }

            if (clazz.isAnnotationPresent(W4Fetch.class)) {
                W4Fetch w4Fetch = clazz.getAnnotation(W4Fetch.class);
                if (w4Fetch.timeout() > 0) {
                    task.setStopedAt(task.getStartedAt() + w4Fetch.timeUnit().toMillis(w4Fetch.timeout()));
                }
            }


            parse(document, clazz, task, (model) -> {
                promise.complete(model);
            });
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new W4ParserException(e);
        }
    }

    private static <T> void parse(Element element, Class<T> clazz,
                               W4QueueTask<?> task, W4ParsePromise<T> promise)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException {
        //Fields
        if (System.currentTimeMillis() < task.getStopedAt() || task.getStopedAt() == 0) {
            List<CompletableFuture<Void>> futureList = new ArrayList<>();
            T parentModel = clazz.newInstance();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Class<?> genericClass = getGenericClass(field);
                if (field.isAnnotationPresent(W4Xpath.class)) {
                    LOG.debug("Found annotation in {} on field: {}", clazz.getCanonicalName(), field.getName());

                    W4Xpath w4Xpath = field.getAnnotation(W4Xpath.class);
                    if (w4Xpath.path().length > 0) {

                        for (int i = 0; i < w4Xpath.path().length; i++) {
                            W4JPath w4JPath = new W4JPath(w4Xpath, w4Xpath.path()[i]);
                            LOG.debug("W4JPath: {}", w4JPath);

                            Elements elements;
                            if (!w4JPath.getPath().isEmpty()) {
                                elements = element.select(w4JPath.getPath());
                            } else {
                                elements = new Elements(element);
                            }
                            LOG.debug("Elements by {} found: {}", w4JPath.getPath(), elements.size());

                            if (elements.size() > 0) {
                                //FOUND DATA

                                if (isCollection(field)) {
                                    //IT is collection
                                    Collection collection = getCollection(field);
                                    field.set(parentModel, collection);

                                    int cnt = 0;
                                    for (Iterator<Element> it = elements.iterator(); it.hasNext(); ) {
                                        if (w4Xpath.maxCount() != 0 && w4Xpath.maxCount() <= cnt) {
                                            break;
                                        }
                                        final CompletableFuture<Void> future = new CompletableFuture<>();
                                        futureList.add(future);
                                        getData(it.next(), w4JPath, genericClass, task, (model) -> {
                                            if (model != null) {
                                                collection.add(model);
                                            }
                                            future.complete(null);
                                        });
                                        cnt++;
                                    }
                                } else {
                                    //bindData
                                    final CompletableFuture<Void> future = new CompletableFuture<>();
                                    futureList.add(future);
                                    getData(elements.first(), w4JPath, genericClass, task, (model) -> {
                                        setFieldValue(field, parentModel, model);
                                        future.complete(null);
                                    });
                                }
                                break;
                            }
                        }
                    }
                }

                if (field.isAnnotationPresent(W4Fetch.class)) {
//                //Fetch remote data
                    W4Fetch w4Fetch = field.getAnnotation(W4Fetch.class);
                    if (task.getDepth() < w4Fetch.maxDepth()) {
                        final Collection collection = getCollection(field);
                        if (isCollection(field)) {
                            field.set(parentModel, collection);
                        }
                        if (w4Fetch.url().length > 0 && !w4Fetch.url()[0].isEmpty()) {
//                    //Hardcoded URL
                            int cnt = 0;
                            for (String url : w4Fetch.url()) {
                                if (w4Fetch.maxFetch() != 0 && w4Fetch.maxFetch() <= cnt) {
                                    break;
                                }
                                W4QueueTask subtask = new W4QueueTask(genericClass, task.getQueue()).setUrl(url);
                                subtask.setStopedAt(task.getStopedAt());
                                final CompletableFuture<Void> future = new CompletableFuture<>();
                                futureList.add(future);
                                subtask.setTaskPromise((model) -> {
                                    if (!isCollection(field)) {
                                        setFieldValue(field, parentModel, model);
                                    } else {
                                        collection.add(model);
                                    }
                                    future.complete(null);
                                });
                                subtask.setDepth(task.getDepth() + 1);
                                task.getQueue().addInternalQueue(subtask);
                                cnt++;
                            }
                        }

                        //Parse links by xpath
                        if (w4Fetch.path().length > 0) {
                            int cnt = 0;
                            for (W4Xpath w4XpathSub : w4Fetch.path()) {
                                for (int i = 0; i < w4XpathSub.path().length; i++) {
                                    if (w4Fetch.maxFetch() != 0 && w4Fetch.maxFetch() <= cnt) {
                                        break;
                                    }
                                    W4JPath w4JPath = new W4JPath(w4XpathSub, w4XpathSub.path()[i]);
                                    Elements links = element.select(w4JPath.getPath());
                                    if (links.size() > 0) {
                                        for (Element link : links) {
                                            if (w4Fetch.maxFetch() != 0 && w4Fetch.maxFetch() <= cnt) {
                                                break;
                                            }
                                            cnt++;
                                            getData(link, w4JPath, String.class, null, (url) -> {
                                                url = normalizeURL(task.getW4Response(), url);
                                                if (url != null) {
                                                    W4QueueTask subtask = new W4QueueTask(genericClass, task.getQueue()).setUrl(url);
                                                    subtask.setStopedAt(task.getStopedAt());
                                                    final CompletableFuture<Void> future = new CompletableFuture<>();
                                                    futureList.add(future);
                                                    subtask.setTaskPromise((model) -> {
                                                        if (!isCollection(field)) {
                                                            setFieldValue(field, parentModel, model);
                                                        } else {
                                                            collection.add(model);
                                                        }
                                                        future.complete(null);
                                                    });
                                                    subtask.setDepth(task.getDepth() + 1);
                                                    task.getQueue().addInternalQueue(subtask);
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).thenRun(() -> {
                promise.complete(parentModel);
            });
        } else {
            promise.complete(null);
        }
    }

    private static Collection getCollection(Field field) {
        Collection collection = null;
        if (isCollection(field)) {
            if (Set.class.isAssignableFrom(field.getType())) {
                collection = new HashSet();
            } else {
                collection = new ArrayList();
            }
        }
        return collection;
    }

    private static void setFieldValue(Field field, Object parentModel, Object model) {
        if (model != null) {
            try {
                field.set(parentModel, model);
            } catch (IllegalAccessException e) {
                LOG.error(e.getMessage());
            }
        }
    }

    private static Class<?> getGenericClass(Field field) {
        if (Collection.class.isAssignableFrom(field.getType())) {
            ParameterizedType subType = (ParameterizedType) field.getGenericType();
            Class<?> subTypeClass = (Class<?>) subType.getActualTypeArguments()[0];
            return subTypeClass;
        } else {
            return field.getType();
        }
    }

    private static boolean isCollection(Field field) {
        return Collection.class.isAssignableFrom(field.getType());
    }

    private static <T> void getData(Element element, W4JPath w4JPath, Class<T> clazz,
                                 W4QueueTask<?> task, W4ParsePromise<T> promise)
            throws IllegalAccessException, NoSuchMethodException {
        if (TypeAdapters.isContainType(clazz)) {
            String data = (w4JPath.getAttr() != null && !w4JPath.getAttr().isEmpty())
                    ? element.attr(w4JPath.getAttr()).trim():element.text().trim();
            if (w4JPath.getXpath().postProcess().length > 0) {
                for (W4RegExp w4RegExp : w4JPath.getXpath().postProcess()) {
                    data = data.replaceAll(w4RegExp.search(), w4RegExp.replace());
                }
            }
            if (data == null || data.isEmpty()) {
                data = w4JPath.getXpath().defaultValue();
            }
            try {
//                return TypeAdapters.getValue(data.trim(), clazz);
                promise.complete(TypeAdapters.getValue(data.trim(), clazz));
            } catch (NumberFormatException e) {
                LOG.warn("Cast exception on field {}. Details: {}", w4JPath.getPath(), e.getMessage());
                promise.complete(null);
            }
        } else {
            try {
                parse(element, clazz, task, promise);
            } catch (InstantiationException e) {
                throw new W4ParserException("Class " + clazz.getName() + " doesn't contain default contructor");
            }
        }
    }


    private static String normalizeURL(W4Response w4Response, String path) {
        URI uri = w4Response.getQueueTask().getW4Request().getRequest().getURI();
        try {
            return new URI(uri.getScheme(), uri.getHost(), path, null).toASCIIString();
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
