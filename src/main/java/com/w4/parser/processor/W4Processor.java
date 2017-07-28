package com.w4.parser.processor;

import com.w4.parser.adapters.TypeAdapters;
import com.w4.parser.annotations.W4Fetch;
import com.w4.parser.annotations.W4Parse;
import com.w4.parser.annotations.W4RegExp;
import com.w4.parser.client.W4Response;
import com.w4.parser.client.promise.W4ParsePromise;
import com.w4.parser.client.queue.W4Queue;
import com.w4.parser.client.queue.W4QueueTask;
import com.w4.parser.exceptions.W4ParserException;
import com.w4.parser.jpath.W4JPath;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
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

    public static <T> void parse(W4Response w4Response, W4ParsePromise<List<T>> promise) throws W4ParserException {
//        LOG.info("Generic class of list: {}", getClassE(new ArrayList<String>() {}));
        W4QueueTask task = w4Response.getQueueTask();
        Class<T> clazz = task.getClazz();
        try {
            Element document = null;
            if (clazz.isAnnotationPresent(W4Parse.class)) {
                W4Parse w4Parse = clazz.getAnnotation(W4Parse.class);
                if (!w4Parse.useXMLParser()) {
                    document = Jsoup.parse(w4Response.getContent());
                } else {
                    document = Jsoup.parse(w4Response.getContent(), "", Parser.xmlParser());
                }

//                if (w4Parse.select().length > 0 && !w4Parse.select()[0].isEmpty()) {
//                    W4JPath w4JPath = new W4JPath(w4Parse, w4Parse.select()[0]);
//                    document = document.select(w4JPath.getPath()).first();
//                }
            } else {
                document = Jsoup.parse(w4Response.getContent(), "", Parser.xmlParser());
            }

            if (clazz.isAnnotationPresent(W4Fetch.class)) {
                W4Fetch w4Fetch = clazz.getAnnotation(W4Fetch.class);
                if (w4Fetch.timeout() > 0) {
                    task.setStopedAt(task.getStartedAt() + w4Fetch.timeUnit().toMillis(w4Fetch.timeout()));
                }
            }


            parse(document, clazz, task, (List<T> result) -> {
                promise.complete(result);
            });
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new W4ParserException(e);
        }
    }

    private static <T> Elements getClassElements(Element element, Class<T> clazz, W4QueueTask<?> task) {
        Elements elements = new Elements(element);

        if (clazz.isAnnotationPresent(W4Parse.class)) {
            W4Parse w4Parse = clazz.getAnnotation(W4Parse.class);
            if (w4Parse.select().length > 0 && !w4Parse.select()[0].isEmpty()) {
                W4JPath w4JPath = new W4JPath(w4Parse, w4Parse.select()[0]);
                elements = element.select(w4JPath.getPath());
            }
        } else if (task.getInheritXpath() != null) {
            W4Parse w4Parse = task.getInheritXpath();
            if (w4Parse.select().length > 0 && !w4Parse.select()[0].isEmpty()) {
                W4JPath w4JPath = new W4JPath(w4Parse, w4Parse.select()[0]);
                elements = element.select(w4JPath.getPath());
            }
        }
        return elements;
    }

    private static <T> void parse(Element element, Class<T> clazz,
                                  W4QueueTask<?> task, W4ParsePromise<List<T>> promise)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException {
        List<T> resultList = new ArrayList<>();
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
//        Elements elements = new Elements(element);

        if (TypeAdapters.isContainType(clazz)) {
            if (task.getInheritXpath() != null) {
                W4Parse w4Parse = task.getInheritXpath();
                if (w4Parse.select().length > 0 && !w4Parse.select()[0].isEmpty()) {
                    W4JPath w4JPath = new W4JPath(w4Parse, w4Parse.select()[0]);
                    Elements elements = element.select(w4JPath.getPath());
                    for (Element el : elements) {
                        resultList.add(getValue(el, w4JPath, clazz));
                    }
                }
            }
        } else {
            Elements elements = getClassElements(element, clazz, task);
            for (Element el : elements) {
                CompletableFuture<Void> future = new CompletableFuture<>();
                futureList.add(future);
                T parentModel = clazz.newInstance();
                parse(el, parentModel, task, (model) -> {
                    resultList.add(model);
                    future.complete(null);
                });
            }
        }

        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[futureList.size()])).thenRun(() -> {
            promise.complete(resultList);
        });
    }

    private static <T> void parse(Element element, T parentModel,
                               W4QueueTask<?> task, W4ParsePromise<T> promise)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException {
        //Fields
        if (System.currentTimeMillis() < task.getStopedAt() || task.getStopedAt() == 0) {
            List<CompletableFuture<Void>> futureList = new ArrayList<>();
            Class clazz = parentModel.getClass();

            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Class<?> genericClass = getGenericClass(field);
                if (field.isAnnotationPresent(W4Parse.class)) {
                    LOG.debug("Found annotation in {} on field: {}", clazz.getCanonicalName(), field.getName());
                    W4Parse w4Parse = field.getAnnotation(W4Parse.class);
//                    if (w4Parse.select().length > 0) {
                        for (int i = 0; i < w4Parse.select().length; i++) {
                            W4JPath w4JPath = new W4JPath(w4Parse, w4Parse.select()[i]);
                            LOG.debug("W4JPath: {}", w4JPath);
                            if (!w4JPath.getPath().startsWith("$")) {
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
                                        final Collection collection = getCollection(field, parentModel);
                                        int cnt = 0;
                                        for (Iterator<Element> it = elements.iterator(); it.hasNext(); ) {
                                            if (w4Parse.maxCount() != 0 && w4Parse.maxCount() <= cnt) {
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
                            } else {
                                Object val = TypeAdapters.getValue(getInternalData(task, w4JPath), genericClass);
                                if (isCollection(field)) {
                                    final Collection collection = getCollection(field, parentModel);
                                    collection.add(val);
                                } else {
                                    setFieldValue(field, parentModel, val);
                                }
                            }
                        }
//                    }
                }

                if (field.isAnnotationPresent(W4Fetch.class)) {
//                //Fetch remote data

                    W4Fetch w4Fetch = field.getAnnotation(W4Fetch.class);
                    if (task.getDepth() < w4Fetch.depth()) {
                        final Collection collection = getCollection(field, parentModel);
                        if (w4Fetch.url().length > 0 && !w4Fetch.url()[0].isEmpty()) {
//                    //Hardcoded URL
                            int cnt = 0;
                            for (String url : w4Fetch.url()) {
                                if (w4Fetch.maxFetch() != 0 && w4Fetch.maxFetch() <= cnt) {
                                    break;
                                }
                                futureList.add(processSubTask(url, genericClass, task, field, parentModel));
                                cnt++;
                            }
                        }

                        //Parse links by href
                        if (w4Fetch.href().length > 0) {
                            int cnt = 0;
                            for (W4Parse w4ParseSub : w4Fetch.href()) {
                                for (int i = 0; i < w4ParseSub.select().length; i++) {
                                    if (w4Fetch.maxFetch() != 0 && w4Fetch.maxFetch() <= cnt) {
                                        break;
                                    }
                                    W4JPath w4JPath = new W4JPath(w4ParseSub, w4ParseSub.select()[i]);
                                    Elements links = element.select(w4JPath.getPath());
                                    if (links.size() > 0) {
                                        for (Element link : links) {
                                            if (w4Fetch.maxFetch() != 0 && w4Fetch.maxFetch() <= cnt) {
                                                break;
                                            }
                                            getData(link, w4JPath, String.class, null, (url) -> {
                                                url = normalizeURL(task.getW4Response(), url);
                                                if (url != null) {
                                                    futureList.add(processSubTask(url, genericClass, task, field, parentModel));
                                                }
                                            });
                                            cnt++;
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

    private static <E> CompletableFuture<Void> processSubTask(String url, Class<E> genericClass,
                                                                 W4QueueTask<?> parentTask, Field field, Object parentModel) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        W4QueueTask<E> subtask = new W4QueueTask(genericClass, parentTask.getQueue())
                .setUrl(url);
        subtask.setParentTask(parentTask);
        if (field.isAnnotationPresent(W4Parse.class)) {
            subtask.setInheritXpath(field.getAnnotation(W4Parse.class));
        }
        subtask.setStopedAt(parentTask.getStopedAt());
        subtask.setTaskPromise((list) -> {
            if (!isCollection(field)) {
                setFieldValue(field, parentModel, ((List) list).get(0));
            } else {
                final Collection collection = getCollection(field, parentModel);
                collection.addAll((Collection) list);
            }
            future.complete(null);
        });
        subtask.setDepth(parentTask.getDepth() + 1);
        parentTask.getQueue().addInternalQueue(subtask);

        return future;
    }

    private static Collection getCollection(Field field, Object parentModel) {
        Collection collection = null;
        if (isCollection(field)) {
            try {
                collection = (Collection) field.get(parentModel);
                if (collection == null) {
                    if (Set.class.isAssignableFrom(field.getType())) {
                        collection = new HashSet();
                    } else {
                        collection = new ArrayList();
                    }
                    field.set(parentModel, collection);
                }
            } catch (IllegalAccessException e) {
                LOG.error(e.getMessage());
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
        return isCollection(field.getType());
    }

    private static boolean isCollection(Class classOf) {
        return Collection.class.isAssignableFrom(classOf);
    }

    private static <T> void getData(Element element, W4JPath w4JPath, Class<T> clazz,
                                 W4QueueTask<?> task, W4ParsePromise<T> promise)
            throws IllegalAccessException, NoSuchMethodException {
        if (TypeAdapters.isContainType(clazz)) {
            if (w4JPath.getPath().startsWith("$")) {
                //Internal selector
                promise.complete(TypeAdapters.getValue(getInternalData(task, w4JPath), clazz));
            } else {
                promise.complete(getValue(element, w4JPath, clazz));
            }
        } else {
            try {
                T t = clazz.newInstance();
                parse(element, t, task, promise);
            } catch (InstantiationException e) {
                throw new W4ParserException("Class " + clazz.getName() + " doesn't contain default contructor");
            }
        }
    }

    private static <T> T getValue(Element element, W4JPath w4JPath, Class<T> clazz) {
        if (TypeAdapters.isContainType(clazz)) {
            String data = (w4JPath.getAttr() != null && !w4JPath.getAttr().isEmpty())
                    ? element.attr(w4JPath.getAttr()).trim()
                    : ((w4JPath.getXpath().html()) ? element.html() : element.text()).trim();
            if (w4JPath.getXpath().postProcess().length > 0) {
                for (W4RegExp w4RegExp : w4JPath.getXpath().postProcess()) {
                    data = data.replaceAll(w4RegExp.search(), w4RegExp.replace()).trim();
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
        return null;
    }

    private static String getInternalData(W4QueueTask task, W4JPath jPath) {
        switch (jPath.getPath()) {
            case W4Select.CONTENT_LENGTH : {
                return String.valueOf(task.getW4Response().getContent().length());
            }
            case W4Select.CURRENT_URL : {
                return task.getW4Request().getUrl();
            }
            case W4Select.RESPONSE_CODE : {
                return String.valueOf(task.getW4Response().getResponseCode());
            }
            case W4Select.USER_AGENT : {
                return String.valueOf(task.getW4Request().getRequest().getAgent());
            }
            default: return null;
        }
    }


    private static String normalizeURL(W4Response w4Response, String path) {
        URI uri = w4Response.getQueueTask().getW4Request().getRequest().getURI();
        return uri.resolve(path).toASCIIString();
    }

    private static <E> Class<E> getClassE(Collection<E> list) {
        Class<?> listClass = list.getClass();

        Type gSuper = listClass.getGenericSuperclass();
        if(!(gSuper instanceof ParameterizedType))
            throw new IllegalArgumentException();

        ParameterizedType pType = (ParameterizedType)gSuper;

        Type tArg = pType.getActualTypeArguments()[0];
        if(!(tArg instanceof Class<?>))
            throw new IllegalArgumentException();

        @SuppressWarnings("unchecked")
        final Class<E> classE = (Class<E>)tArg;
        return classE;
    }
}
