package com.w4.parser.processor;

import com.w4.parser.adapters.TypeAdapters;
import com.w4.parser.annotations.W4RegExp;
import com.w4.parser.annotations.W4Xpath;
import com.w4.parser.client.W4Request;
import com.w4.parser.client.W4Response;
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

    public static W4Request url(String url) {
        return W4Request.url(url);
    }

    public static W4Response data(String html) {
        W4Response response = new W4Response();
        response.setContent(html);
        return response;
    }

    public static <T> CompletableFuture<T> parseAsync(String html, Class<T> clazz) throws W4ParserException {
        final CompletableFuture<T> result = CompletableFuture.supplyAsync(() -> parse(html, clazz));
        return result;
    }

    public static <T> T parse(String html, Class<T> clazz) throws W4ParserException {
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
            }

            parse(document, model);

        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new W4ParserException(e);
        }

        return model;
    }

    private static <T> T parse(Element element, T model)
            throws IllegalAccessException, InstantiationException, NoSuchMethodException {
        //Fields
        Class clazz = model.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
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

                            if (Collection.class.isAssignableFrom(field.getType())) {
                                //IT is collection
                                Collection collection;
                                if (Set.class.isAssignableFrom(field.getType())) {
                                    collection = new HashSet();
                                } else {
                                    collection = new ArrayList();
                                }
                                field.set(model, collection);

                                ParameterizedType subType = (ParameterizedType) field.getGenericType();
                                Class<?> subTypeClass = (Class<?>) subType.getActualTypeArguments()[0];

                                int cnt = 0;
                                for (Iterator<Element> it = elements.iterator(); it.hasNext();) {
                                    if (w4Xpath.maxCount() != 0 && w4Xpath.maxCount() <= cnt) {
                                        break;
                                    }
                                    collection.add(getData(it.next(), w4JPath, subTypeClass));
                                    cnt++;
                                }
//                                field.set(model, getCollection(elements, w4Xpath, w4JPath, field.getType()));
                            } else {
                                //bindData
                                Object val = getData(elements.first(), w4JPath, field.getType());
                                if (val != null) {
                                    field.set(model, val);
                                }
                            }

                            break;
                        }
                    }
                }
            }
        }

        return model;
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

    private static <T> T getData(Element element, W4JPath w4JPath, Class<T> clazz)
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
            return parse(element, value);
        } catch (InstantiationException e) {
            throw new W4ParserException("Class " + clazz.getName() + " doesn't contain default contructor");
        }
    }
}
