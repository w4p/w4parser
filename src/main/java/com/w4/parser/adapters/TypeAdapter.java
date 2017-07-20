package com.w4.parser.adapters;

public interface TypeAdapter<T> {
    T toObject(String data);
}
