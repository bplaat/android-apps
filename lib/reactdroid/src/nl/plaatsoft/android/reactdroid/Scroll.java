/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.reactdroid;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import nl.plaatsoft.android.compat.WindowInsetsCompat;

import org.jspecify.annotations.Nullable;

public class Scroll extends Bin {
    protected boolean useWindowInsets = false;
    protected WindowInsetsCompat.Insets windowInsets;

    public Scroll(WidgetContext context) {
        super(context);
    }

    public Scroll useWindowInsets() {
        this.useWindowInsets = true;
        return this;
    }

    @Override
    public Scroll child(Widget child) {
        this.child = child;
        return this;
    }

    @Override
    public View render(ViewGroup parent, @Nullable View view) {
        ScrollView scrollView;
        if (view != null && view.getClass().equals(ScrollView.class)) {
            scrollView = (ScrollView) view;
        } else {
            if (view != null) {
                int index = parent.indexOfChild(view);
                parent.removeView(view);
                scrollView = new ScrollView(getContext());
                parent.addView(scrollView, index);
            } else {
                scrollView = new ScrollView(getContext());
                parent.addView(scrollView);
            }
            scrollView.setTag(key);
        }

        if (useWindowInsets) {
            scrollView.setOnApplyWindowInsetsListener((v, windowInsets) -> {
                this.windowInsets = WindowInsetsCompat.getInsets(windowInsets);
                scrollView.setClipToPadding(false);
                scrollView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom + this.windowInsets.bottom());
                return windowInsets;
            });
            if (windowInsets != null)
                scrollView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom + this.windowInsets.bottom());
        } else {
            scrollView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        }

        child.render(scrollView, scrollView.getChildAt(0));

        if (child == null && scrollView.getChildCount() > 0) {
            scrollView.removeViewAt(0);
        }

        return scrollView;
    }
}
