package com.example.tradingapp.util;

import java.util.List;
import java.util.Comparator;

public class SearchUtils {
    public static <T> int binarySearch(
            List<T> list, T key, Comparator<? super T> cmp
    ) {
        int lo = 0, hi = list.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            int c = cmp.compare(list.get(mid), key);
            if (c == 0) return mid;
            if (c < 0) lo = mid + 1;
            else hi = mid - 1;
        }
        return -1;
    }
}
