package com.example.tradingapp.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SortUtils {
    public static <T> void mergeSort(List<T> list, Comparator<? super T> cmp) {
        if (list.size() < 2) return;
        int mid = list.size() / 2;
        List<T> left  = new ArrayList<>(list.subList(0, mid));
        List<T> right = new ArrayList<>(list.subList(mid, list.size()));
        mergeSort(left, cmp);
        mergeSort(right, cmp);
        merge(list, left, right, cmp);
    }

    private static <T> void merge(
            List<T> result, List<T> left, List<T> right, Comparator<? super T> cmp
    ) {
        int i = 0, j = 0, k = 0;
        while (i < left.size() && j < right.size()) {
            if (cmp.compare(left.get(i), right.get(j)) <= 0) {
                result.set(k++, left.get(i++));
            } else {
                result.set(k++, right.get(j++));
            }
        }
        while (i < left.size())  result.set(k++, left.get(i++));
        while (j < right.size()) result.set(k++, right.get(j++));
    }
}
