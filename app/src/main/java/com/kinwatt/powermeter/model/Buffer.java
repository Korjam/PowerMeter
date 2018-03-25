package com.kinwatt.powermeter.model;

import java.util.AbstractCollection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Buffer<T> extends AbstractCollection<T> {

    private ArrayDeque<T> innerQueue;
    private int capacity;

    public Buffer(int capacity) {
        innerQueue = new ArrayDeque<>();
        this.capacity = capacity;
    }

    @Override
    public boolean add(T t) {
        if (innerQueue.size() == capacity) {
            innerQueue.remove();
        }
        return innerQueue.add(t);
    }

    @Override
    public Iterator<T> iterator() {
        return innerQueue.iterator();
    }

    @Override
    public int size() {
        return innerQueue.size();
    }

    public List<T> last(int n) {
        List<T> res = new ArrayList<>();

        Object[] arr = innerQueue.toArray();
        for (int i = arr.length - 1; i >= arr.length - n; i--) {
            res.add((T)arr[i]);
        }

        return res;
    }

    public T last() {
        return innerQueue.peekLast();
    }

    public T peek() {
        return innerQueue.peekLast();
    }

    public T peek(int n) {
        Object[] arr = innerQueue.toArray();
        return (T)arr[arr.length - n - 1];
    }
}
