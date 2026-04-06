/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class Row {
    static class RowLayout extends LinearLayout {
        RowLayout(Context context) {
            super(context);
            setOrientation(HORIZONTAL);
        }
    }

    static class ScrollRowLayout extends HorizontalScrollView {
        final RowLayout inner;

        ScrollRowLayout(Context context) {
            super(context);
            inner = new RowLayout(context);
            inner.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
            addView(inner);
        }
    }

    private final RowLayout ref;

    public Row(Runnable children) {
        BuildContext c = BuildContext.current();
        View existing = c.peekSlot();
        if (existing instanceof ScrollRowLayout) {
            c.advanceSlot();
            ref = ((ScrollRowLayout)existing).inner;
        } else {
            ref = c.slot(RowLayout.class, () -> new RowLayout(c.getContext()));
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

    public Row modifier(Modifier modifier) {
        if (modifier.isScrollHorizontal()) {
            var parent = ref.getParent();
            if (parent instanceof ScrollRowLayout) {
                modifier.applyTo((ScrollRowLayout)parent);
                modifier.applyLayoutTo((ScrollRowLayout)parent);
            } else if (parent instanceof ViewGroup) {
                var vg = (ViewGroup)parent;
                var srl = new ScrollRowLayout(ref.getContext());
                while (ref.getChildCount() > 0) {
                    var child = ref.getChildAt(0);
                    ref.removeViewAt(0);
                    srl.inner.addView(child);
                }
                int idx = vg.indexOfChild(ref);
                vg.removeView(ref);
                vg.addView(srl, idx);
                modifier.applyTo(srl);
                modifier.applyLayoutTo(srl);
            }
        } else {
            var target = ref.getParent() instanceof ScrollRowLayout ? (ScrollRowLayout)ref.getParent() : ref;
            modifier.applyTo(target);
            modifier.applyLayoutTo(target);
        }
        return this;
    }

    public Row onClick(Runnable handler) {
        ref.setOnClickListener(v -> handler.run());
        return this;
    }

    public Row onClick(android.view.View.OnClickListener handler) {
        ref.setOnClickListener(handler);
        return this;
    }
}
