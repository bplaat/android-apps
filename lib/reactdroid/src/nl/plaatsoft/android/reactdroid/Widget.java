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

abstract public class Widget {
    protected WidgetContext c;
    protected long key;
    protected int paddingTop;
    protected int paddingRight;
    protected int paddingBottom;
    protected int paddingLeft;

    protected Widget(WidgetContext c) {
        this.c = c;
    }

    public Widget key(long key) {
        this.key = key;
        return this;
    }

    protected Context getContext() {
        return c.getContext();
    }

    public Widget paddingDp(float padding) {
        paddingTop = Utils.dpToPx(getContext(), padding);
        paddingRight = paddingTop;
        paddingBottom = paddingTop;
        paddingLeft = paddingTop;
        return this;
    }

    public Widget paddingDp(float paddingVertical, float paddingHorizontal) {
        paddingTop = Utils.dpToPx(getContext(), paddingVertical);
        paddingRight = Utils.dpToPx(getContext(), paddingHorizontal);
        paddingBottom = paddingTop;
        paddingLeft = paddingRight;
        return this;
    }

    public Widget paddingDp(float paddingTop, float paddingRight, float paddingBottom) {
        this.paddingTop = Utils.dpToPx(getContext(), paddingTop);
        this.paddingRight = Utils.dpToPx(getContext(), paddingRight);
        this.paddingBottom = Utils.dpToPx(getContext(), paddingBottom);
        paddingLeft = this.paddingRight;
        return this;
    }

    public Widget paddingDp(float paddingTop, float paddingRight, float paddingBottom, float paddingLeft) {
        this.paddingTop = Utils.dpToPx(getContext(), paddingTop);
        this.paddingRight = Utils.dpToPx(getContext(), paddingRight);
        this.paddingBottom = Utils.dpToPx(getContext(), paddingBottom);
        this.paddingLeft = Utils.dpToPx(getContext(), paddingLeft);
        return this;
    }

    abstract public View render(ViewGroup parent, @Nullable View view);
}
