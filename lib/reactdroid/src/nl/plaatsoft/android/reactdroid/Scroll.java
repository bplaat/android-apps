/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.reactdroid;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

public class Scroll extends Bin {
    protected Scroll(WidgetContext context) {
        super(context);
    }

    public static Scroll create(WidgetContext context) {
        return new Scroll(context);
    }

    public Scroll child(Widget child) {
        this.child = child;
        return this;
    }

    public View render(ViewGroup parent, View view) {
        ScrollView scrollView;
        if (view != null && view.getClass().equals(ScrollView.class)) {
            scrollView = (ScrollView) view;
        } else {
            if (view != null) {
                int index = parent.indexOfChild(view);
                parent.removeView(view);
                scrollView = new ScrollView(context.getContext());
                parent.addView(scrollView, index);
            } else {
                scrollView = new ScrollView(context.getContext());
                parent.addView(scrollView);
            }
            scrollView.setTag(key);
        }

        scrollView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

        child.render(scrollView, scrollView.getChildAt(0));

        if (child == null && scrollView.getChildCount() > 0) {
            scrollView.removeViewAt(0);
        }

        return scrollView;
    }
}
