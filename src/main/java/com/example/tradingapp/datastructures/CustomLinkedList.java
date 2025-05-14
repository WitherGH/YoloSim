package com.example.tradingapp.datastructures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class CustomLinkedList<T> implements Iterable<T> {
    private static class Node<E> {
        E value;
        Node<E> next;
        Node(E v) { value = v; }
    }

    private Node<T> head, tail;
    private int size = 0;

    public void add(T item) {
        Node<T> n = new Node<>(item);
        if (head == null) head = n;
        else           tail.next = n;
        tail = n;
        size++;
    }

    public boolean remove(T item) {
        Node<T> prev = null, cur = head;
        while (cur != null) {
            if ((item == null && cur.value == null)
                    || (item != null && item.equals(cur.value))) {
                if (prev == null) head = cur.next;
                else              prev.next = cur.next;
                if (cur == tail)  tail = prev;
                size--;
                return true;
            }
            prev = cur;
            cur  = cur.next;
        }
        return false;
    }

    public T get(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException(index + " out of [0," + size + ")");
        Node<T> cur = head;
        for (int i = 0; i < index; i++) cur = cur.next;
        return cur.value;
    }

    public int size() {
        return size;
    }

    public int indexOf(T item) {
        Node<T> cur = head;
        for (int i = 0; cur != null; i++, cur = cur.next) {
            if ((item == null && cur.value == null)
                    || (item != null && item.equals(cur.value)))
                return i;
        }
        return -1;
    }

    public void set(int index, T value) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException(index + " out of [0," + size + ")");
        Node<T> cur = head;
        for (int i = 0; i < index; i++) cur = cur.next;
        cur.value = value;
    }

    public List<T> toList() {
        List<T> result = new ArrayList<>(size);
        Node<T> cur = head;
        while (cur != null) {
            result.add(cur.value);
            cur = cur.next;
        }
        return result;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            Node<T> cur = head;
            @Override public boolean hasNext() { return cur != null; }
            @Override public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T v = cur.value;
                cur = cur.next;
                return v;
            }
        };
    }

    public void clear() {
        head = tail = null;
        size = 0;
    }
}
