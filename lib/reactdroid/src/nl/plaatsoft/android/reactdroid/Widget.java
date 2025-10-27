/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.reactdroid;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import org.jspecify.annotations.Nullable;

public abstract class Widget {
    protected Modifier modifier;
    protected Object key;

    public Widget(Modifier modifier) {
        this.modifier = modifier;
        WidgetContext.widgets.add(this);
    }

    public Widget() {
        this(null);
    }

    public Widget key(Object key) {
        this.key = key;
        return this;
    }

    abstract public View render(ViewGroup parent, @Nullable View view);
}
