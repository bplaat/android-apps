/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.reactdroid;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class Box extends Container {
    public static int HORIZONTAL = 0;
    public static int VERTICAL = 1;

    protected int orientation = VERTICAL;

    protected Box(WidgetContext context) {
        super(context);
    }

    public static Box create(WidgetContext context) {
        return new Box(context);
    }

    public Box orientation(int orientation) {
        this.orientation = orientation;
        return this;
    }

    public Box child(Widget child) {
        if (child != null) {
            children.add(child);
        }
        return this;
    }

    public Box child(List<Widget> children) {
        for (var child : children) {
            if (child != null) {
                this.children.add(child);
            }
        }
        return this;
    }

    public View render(ViewGroup parent, View view) {
        LinearLayout linearLayout;
        if (view != null && view.getClass().equals(LinearLayout.class)) {
            linearLayout = (LinearLayout) view;
        } else {
            if (view != null) {
                int index = parent.indexOfChild(view);
                parent.removeView(view);
                linearLayout = new LinearLayout(context.getContext());
                parent.addView(linearLayout, index);
            } else {
                linearLayout = new LinearLayout(context.getContext());
                parent.addView(linearLayout);
            }
            linearLayout.setTag(key);
        }

        if (linearLayout.getOrientation() != orientation) {
            linearLayout.setOrientation(orientation);
        }

        linearLayout.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);

        for (int i = 0; i < children.size(); i++) {
            children.get(i).render(linearLayout, linearLayout.getChildAt(i));
        }
        for (int i = children.size(); i < linearLayout.getChildCount(); i++) {
            linearLayout.removeViewAt(i);
        }

        return linearLayout;
    }
}
