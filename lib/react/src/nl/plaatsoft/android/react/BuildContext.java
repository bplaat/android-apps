/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import org.jspecify.annotations.Nullable;

public class BuildContext {
    private static final Deque<BuildContext> stack = new ArrayDeque<>();

    public static void push(BuildContext c) {
        stack.push(c);
    }

    public static void pop() {
        stack.pop();
    }

    public static BuildContext current() {
        BuildContext c = stack.peek();
        if (c == null)
            throw new IllegalStateException("No active BuildContext");
        return c;
    }

    private final Context context;
    private final ViewGroup parent;
    private int index = 0;

    public BuildContext(Context context, ViewGroup parent) {
        this.context = context;
        this.parent = parent;
    }

    public Context getContext() {
        return context;
    }

    @SuppressWarnings("unchecked")
    public <V extends View> V slot(Class<V> type, Supplier<V> create) {
        @Nullable View existing = parent.getChildAt(index);
        V view;
        if (existing != null && existing.getClass() == type) {
            view = (V)existing;
        } else {
            view = create.get();
            if (existing != null) {
                parent.removeViewAt(index);
                parent.addView(view, index);
            } else {
                parent.addView(view);
            }
        }
        index++;
        return view;
    }

    public BuildContext scope(ViewGroup child) {
        return new BuildContext(context, child);
    }

    public void cleanup() {
        while (parent.getChildCount() > index) {
            parent.removeViewAt(index);
        }
    }

    public @Nullable View peekSlot() {
        return parent.getChildAt(index);
    }

    public void advanceSlot() {
        index++;
    }
}
