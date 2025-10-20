/*
 * Copyright (c) 2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.compat;

import java.util.ArrayList;
import java.util.Collection;

public class MapList<T> extends ArrayList<T> {
    private static final long serialVersionUID = 1;

    public static interface Predicate<T> {
        boolean test(T item);
    }
    public static interface Mapper<T, R> {
        R map(T item);
    }

    public MapList() {
        super();
    }

    public MapList(Collection<? extends T> c) {
        super(c);
    }

    public MapList(int initialCapacity) {
        super(initialCapacity);
    }

    public <R> MapList<R> map(Mapper<? super T, ? extends R> mapper) {
        var result = new MapList<R>(this.size());
        for (var item : this) {
            result.add(mapper.map(item));
        }
        return result;
    }

    public boolean some(Predicate<? super T> predicate) {
        for (var item : this) {
            if (predicate.test(item))
                return true;
        }
        return false;
    }

    public T find(Predicate<? super T> predicate) {
        for (var item : this) {
            if (predicate.test(item))
                return item;
        }
        return null;
    }

    public MapList<T> filter(Predicate<? super T> predicate) {
        var result = new MapList<T>();
        for (var item : this) {
            if (predicate.test(item))
                result.add(item);
        }
        return result;
    }
}
