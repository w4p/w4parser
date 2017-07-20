package com.w4.parser.adapters;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TypeAdapters {

    private static Map<Class<?>, TypeAdapter<?>> typeTokenCache =  Collections.synchronizedMap(new HashMap<>());

    static {
        typeTokenCache.put(String.class, data -> data);
        typeTokenCache.put(Boolean.class, data -> Boolean.valueOf(data));
        typeTokenCache.put(Short.class, data -> Short.valueOf(data));
        typeTokenCache.put(Integer.class, data -> Integer.valueOf(data));
        typeTokenCache.put(Long.class, data -> Long.valueOf(data));
        typeTokenCache.put(Float.class, data -> Float.valueOf(data));
        typeTokenCache.put(Number.class, data -> new Number() {
            @Override
            public int intValue() {
                try {
                    return Integer.parseInt(data);
                } catch (NumberFormatException var4) {
                    try {
                        return (int)Long.parseLong(data);
                    } catch (NumberFormatException var3) {
                        return (new BigDecimal(data)).intValue();
                    }
                }
            }

            @Override
            public long longValue() {
                try {
                    return Long.parseLong(data);
                } catch (NumberFormatException var2) {
                    return (new BigDecimal(data)).longValue();
                }
            }

            @Override
            public float floatValue() {
                return Float.parseFloat(data);
            }

            @Override
            public double doubleValue() {
                return Double.parseDouble(data);
            }
        });
        typeTokenCache.put(BigDecimal.class, data -> new BigDecimal(data));
        typeTokenCache.put(BigInteger.class, data -> new BigInteger(data));
        typeTokenCache.put(Enum.class, data -> new BigInteger(data));

    }

    public static boolean isContainType(Class clazz) {
        return typeTokenCache.containsKey(clazz);
    }

    public static <T> T getValue(String data, Class<T> clazz) {
        TypeAdapter<T> adapter = (TypeAdapter<T>) typeTokenCache.get(clazz);
        if (adapter != null) {
            return adapter.toObject(data);
        }
        return null;
    }

}
