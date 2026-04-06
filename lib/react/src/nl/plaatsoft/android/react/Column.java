/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class Column {
    static class ColumnLayout extends LinearLayout {
        ColumnLayout(Context context) {
            super(context);
            setOrientation(VERTICAL);
        }
    }

    static class ScrollColumnLayout extends ScrollView {
        final ColumnLayout inner;

        ScrollColumnLayout(Context context) {
            super(context);
            inner = new ColumnLayout(context);
            inner.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            addView(inner);
        }
    }

    private final ColumnLayout ref;

    public Column(Runnable children) {
        BuildContext c = BuildContext.current();
        View existing = c.peekSlot();
        if (existing instanceof ScrollColumnLayout) {
            c.advanceSlot();
            ref = ((ScrollColumnLayout)existing).inner;
        } else {
            ref = c.slot(ColumnLayout.class, () -> new ColumnLayout(c.getContext()));
        }
        var inner = c.scope(ref);
        BuildContext.push(inner);
        try {
            children.run();
        } finally {
            inner.cleanup();
            BuildContext.pop();
        }
    }

    public Column modifier(Modifier modifier) {
        if (modifier.isScrollVertical()) {
            var parent = ref.getParent();
            if (parent instanceof ScrollColumnLayout) {
                modifier.applyTo((ScrollColumnLayout)parent);
                modifier.applyLayoutTo((ScrollColumnLayout)parent);
            } else if (parent instanceof ViewGroup) {
                var vg = (ViewGroup)parent;
                var scl = new ScrollColumnLayout(ref.getContext());
                while (ref.getChildCount() > 0) {
                    var child = ref.getChildAt(0);
                    ref.removeViewAt(0);
                    scl.inner.addView(child);
                }
                int idx = vg.indexOfChild(ref);
                vg.removeView(ref);
                vg.addView(scl, idx);
                modifier.applyTo(scl);
                modifier.applyLayoutTo(scl);
            }
        } else {
            var target = ref.getParent() instanceof ScrollColumnLayout ? (ScrollColumnLayout)ref.getParent() : ref;
            modifier.applyTo(target);
            modifier.applyLayoutTo(target);
        }
        return this;
    }

    public Column onClick(Runnable handler) {
        ref.setOnClickListener(v -> handler.run());
        return this;
    }

    public Column onClick(android.view.View.OnClickListener handler) {
        ref.setOnClickListener(handler);
        return this;
    }
}
