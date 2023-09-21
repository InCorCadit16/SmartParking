package com.example.smartparkingclient.api.utils;

import com.example.smartparkingclient.api.models.BaseModel;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class StreamUtils {
    public static int getIndexByFilter(List<? extends BaseModel> list, Predicate<BaseModel> filter) {
        return IntStream.range(0, list.size())
                .filter(i -> filter.test(list.get(i)))
                .findFirst()
                .orElse(-1);
    }
}
