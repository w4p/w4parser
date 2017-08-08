package com.w4p.parser.adapters;

public interface TypeAdapter<T> {
    T toObject(String data);
}
