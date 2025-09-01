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

public class Row extends Container {
    public Row(WidgetContext context) {
        super(context);
    }

    public Row paddingDp(int padding) {
        super.paddingDp(padding);
        return this;
    }

    public Row paddingDp(int paddingVertical, int paddingHorizontal) {
        super.paddingDp(paddingVertical, paddingHorizontal);
        return this;
    }

    @Override
    public Row child(Widget child) {
        if (child != null) {
            children.add(child);
        }
        return this;
    }

    @Override
    public Row child(List<Widget> children) {
        for (var child : children) {
            if (child != null) {
                this.children.add(child);
            }
        }
        return this;
    }

    @Override
    public View render(ViewGroup parent, @Nullable View view) {
        LinearLayout linearLayout;
        if (view != null && view.getClass().equals(LinearLayout.class)) {
            linearLayout = (LinearLayout) view;
        } else {
            if (view != null) {
                int index = parent.indexOfChild(view);
                parent.removeView(view);
                linearLayout = new LinearLayout(getContext());
                parent.addView(linearLayout, index);
            } else {
                linearLayout = new LinearLayout(getContext());
                parent.addView(linearLayout);
            }
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setTag(key);
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
