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

import org.jspecify.annotations.Nullable;

public abstract class Box extends Container {
    public enum Orientation { HORIZONTAL, VERTICAL }

    protected Orientation orientation;

    public Box(Orientation orientation, Modifier modifier, OnChildren onChildren) {
        super(modifier, onChildren);
        this.orientation = orientation;
    }

    public Box(Orientation orientation, OnChildren onChildren) {
        this(orientation, null, onChildren);
    }

    @Override
    public View render(ViewGroup parent, @Nullable View view) {
        LinearLayout linearLayout;
        if (view != null && view.getClass().equals(LinearLayout.class)) {
            linearLayout = (LinearLayout)view;
        } else {
            if (view != null) {
                int index = parent.indexOfChild(view);
                parent.removeView(view);
                linearLayout = new LinearLayout(WidgetContext.getContext());
                parent.addView(linearLayout, index);
            } else {
                linearLayout = new LinearLayout(WidgetContext.getContext());
                parent.addView(linearLayout);
            }
            linearLayout.setOrientation(
                orientation == Orientation.HORIZONTAL ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
            linearLayout.setTag(key);
        }

        if (modifier != null) {
            modifier.applyTo(linearLayout);
        }

        // for (int i = 0; i < children.size(); i++) {
        //     children.get(i).render(linearLayout, linearLayout.getChildAt(i));
        // }
        // for (int i = children.size(); i < linearLayout.getChildCount(); i++) {
        //     linearLayout.removeViewAt(i);
        // }

        return linearLayout;
    }
}
