package com.kinwatt.powermeter.common;

import java.util.List;

public final class Collections {

    public static <T> void remove(List<T> list, Predicate<T> predicate) {
        for (int i = list.size() - 1; i >= 0; i--) {
            if (predicate.apply(list.get(i))) {
                list.remove(i);
            }
        }
    }
}
