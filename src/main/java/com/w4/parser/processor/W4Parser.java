package com.w4.parser.processor;

import com.w4.parser.adapters.TypeAdapters;
import com.w4.parser.annotations.W4Fetch;
import com.w4.parser.annotations.W4RegExp;
import com.w4.parser.annotations.W4Xpath;
import com.w4.parser.client.queue.W4Queue;
import com.w4.parser.client.W4Request;
import com.w4.parser.client.W4Response;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class W4Parser {
    private static final Logger LOG = LoggerFactory.getLogger(W4Parser.class);

    public static W4Queue queue() {
        return new W4Queue();
    }

    public static W4Request url(String url) {
        return W4Request.url(url);
    }

    public static <T> W4Queue url(String url, Class<T> clazz) {
        W4Queue queue = new W4Queue();
        return queue.url(url, clazz);
    }

    public static W4Response data(String html) {
        W4Response response = new W4Response();
        response.setContent(html);
        return response;
    }

    public static <T> T data(String html, Class<T> clazz) {
        W4Response response = new W4Response();
        response.setContent(html);
        return response.parse(clazz);
    }

    public static <T> CompletableFuture<T> parseAsync(String html, Class<T> clazz, W4Queue queue) throws W4ParserException {
        final CompletableFuture<T> future = new CompletableFuture<>();
        parseAsync(html, clazz, queue, (model) -> {
            future.completedFuture(model);
        });
        return future;
    }

    public static <T> void parseAsync(String html, Class<T> clazz, W4Queue queue, W4ParsePromise promise) throws W4ParserException {
        CompletableFuture.runAsync(() -> {
            T model = parse(html, clazz, queue);
            promise.complete(model);
        });
    }

    public static <T> T parse(String html, Class<T> clazz, W4Queue queue) throws W4ParserException {
        W4ParserTask<T> parserTask = new W4ParserTask(queue, clazz);
        return parse(html, parserTask);
    }

    public static <T> T parse(String html, W4ParserTask<T> parserTask) throws W4ParserException {
        Class<T> clazz = parserTask.getModelClass();
        T model = null;
        try {
            model = clazz.newInstance();
            Element document = Jsoup.parse(html);

            if (clazz.isAnnotationPresent(W4Xpath.class)) {
                W4Xpath w4Xpath = clazz.getAnnotation(W4Xpath.class);
                if (w4Xpath.path().length > 0 && !w4Xpath.path()[0].isEmpty()) {
                    W4JPath w4JPath = new W4JPath(w4Xpath, w4Xpath.path()[0]);
                    document = document.select(w4JPath.getPath()).first();
                }

                if (w4Xpath.timeout() > 0) {
                    parserTask.setStopAt(parserTask.getStartedAt() + w4Xpath.timeUnit().toMillis(w4Xpath.timeout()));
                }
            }

//            long stopTime;
//            if (clazz.isAnnotationPresent(W4Fetch.class)) {
//                W4Fetch w4Fetch = clazz.getAnnotation(W4Fetch.class);
//                stopTime = System.currentTimeMillis() + w4Fetch.timeUnit().toMillis(w4Fetch.timeout());
//            } else {
//                stopTime = Long.MAX_VALUE;
//            }

            parse(document, model, parserTask);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new W4ParserException(e);
        }

        return model;
    }

    private static <T> T parse(Element element, T model, W4ParserTask w4ParserTask)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException {
        //Fields
        Class clazz = model.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Class<?> genericClass = getGenericClass(field);
            if (field.isAnnotationPresent(W4Xpath.class)) {
                LOG.debug("Found annotation in {} on field: {}", clazz.getCanonicalName(), field.getName());

                W4Xpath w4Xpath = field.getAnnotation(W4Xpath.class);
                if (w4Xpath.path().length > 0) {

                    for (int i = 0; i<w4Xpath.path().length; i++) {
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
                            field.setAccessible(true);


                            if (isCollection(field)) {
                                //IT is collection
                                Collection collection;
                                if (Set.class.isAssignableFrom(field.getType())) {
                                    collection = new HashSet();
                                } else {
                                    collection = new ArrayList();
                                }
                                field.set(model, collection);

                                int cnt = 0;
                                for (Iterator<Element> it = elements.iterator(); it.hasNext();) {
                                    if (w4Xpath.maxCount() != 0 && w4Xpath.maxCount() <= cnt) {
                                        break;
                                    }
                                    collection.add(getData(it.next(), w4JPath, genericClass, w4ParserTask));
                                    cnt++;
                                }
                            } else {
                                //bindData
                                Object val = getData(elements.first(), w4JPath, genericClass, w4ParserTask);
                                if (val != null) {
                                    field.set(model, val);
                                }
                            }

                            break;
                        }
                    }
                }
            }

            if (field.isAnnotationPresent(W4Fetch.class)) {
                //Fetch remote data
                W4Fetch w4Fetch = field.getAnnotation(W4Fetch.class);
                if (w4Fetch.url().length > 0 && !w4Fetch.url()[0].isEmpty()) {
                    //Hardcoded URL
                    for (String url : w4Fetch.url()) {
                        W4QueueTask task = new W4QueueTask(genericClass).setUrl(url);
                        task.setWrapModel(field.get(model));
                        w4ParserTask.getQueue().addInternalQueue(task);
                    }
                }
            }
        }

        return model;
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

//    private static <T> Collection getCollection(Elements elements, W4Xpath w4Xpath, W4JPath w4JsoupPath, Class<T> clazz)
//            throws IllegalAccessException, NoSuchMethodException, ClassNotFoundException {
//        //IT is collection
//        Collection collection;
//        if (Set.class.isAssignableFrom(clazz)) {
//            collection = new HashSet<>();
//        } else {
//            collection = new ArrayList<>();
//        }
//
//
////        ParameterizedType superclass = (ParameterizedType) collection.getClass().getGenericSuperclass();
////        Class<?> subTypeClass = (Class<?>) superclass.getActualTypeArguments()[0];
//
//        Method m = clazz.getDeclaredMethod("add", Object.class);
//        ParameterizedType type = (ParameterizedType) m.getGenericParameterTypes()[0];
//        final TypeToken<T> typeToken = new TypeToken<T>(collection.getClass()) { };
//        Type t = typeToken.getType();
//
////        Class cl = Class.forName(t.getTypeName());
//
////        ParameterizedType subType = (ParameterizedType) clazz.getGenericInterfaces()[0];
////        Class<?> subTypeClass = (Class<?>) subType.getActualTypeArguments()[0];
//
//
//        int cnt = 0;
//        for (Iterator<Element> it = elements.iterator(); it.hasNext();) {
//            if (w4Xpath.maxCount() != 0 && w4Xpath.maxCount() <= cnt) {
//                break;
//            }
//            collection.add(getData(it.next(), w4JsoupPath, (Class<T>) t));
//            cnt++;
//        }
//
//        return collection;
//    }

    private static <T> T getData(Element element, W4JPath w4JPath, Class<T> clazz, W4ParserTask w4ParserTask)
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
                return TypeAdapters.getValue(data.trim(), clazz);
            } catch (NumberFormatException e) {
                LOG.warn("Cast exception on field {}. Details: {}", w4JPath.getPath(), e.getMessage());
                return null;
            }
        }
        try {
            T value = clazz.newInstance();
            return parse(element, value, w4ParserTask);
        } catch (InstantiationException e) {
            throw new W4ParserException("Class " + clazz.getName() + " doesn't contain default contructor");
        }
    }
}
