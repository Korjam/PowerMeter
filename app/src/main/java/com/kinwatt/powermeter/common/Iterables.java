package com.kinwatt.powermeter.common;

import java.util.ArrayList;
import java.util.List;

public final class Iterables {

    public static <T, S> T aggregate(Iterable<S> list, T seed, FunctionTwo<T, S, T> func) {
        for (S item : list) {
            seed = func.apply(seed, item);
        }
        return seed;
    }

    public static <T> T first(Iterable<T> list, Predicate<T> predicate) {
        for (T item : list) {
            if (predicate.apply(item)) {
                return item;
            }
        }
        return null;
    }

    public static <T> T last(Iterable<T> list, Predicate<T> predicate) {
        T res = null;
        for (T item : list) {
            if (predicate.apply(item)) {
                res = item;
            }
        }
        return res;
    }

    public static <T, S> Iterable<S> select(Iterable<T> list, Function<T, S> selector) {
        List<S> res = new ArrayList<>();
        for (T item : list) {
            res.add(selector.apply(item));
        }
        return res;
    }

    public static <T> Iterable<T> where(Iterable<T> list, Predicate<T> predicate) {
        List<T> res = new ArrayList<>();
        for (T item : list) {
            if (predicate.apply(item)) {
                res.add(item);
            }
        }
        return res;
    }

    public static <T> boolean any(Iterable<T> list, Predicate<T> predicate) {
        for (T item : list) {
            if (predicate.apply(item)) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean all(Iterable<T> list, Predicate<T> predicate) {
        boolean res = true;
        for (T item : list) {
            res &= predicate.apply(item);
        }
        return res;
    }

    public static <T> List<T> toList(Iterable<T> list) {
        ArrayList<T> res = new ArrayList<>();
        for (T item : list) {
            res.add(item);
        }
        return res;
    }
}
