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
    // Distinct subclass so slot() can tell Row apart from Column
    static class RowLayout extends LinearLayout {
        RowLayout(Context context) {
            super(context);
            setOrientation(HORIZONTAL);
        }
    }

    // Scrollable variant: a HorizontalScrollView whose single child is a RowLayout.
    // Using a distinct subclass lets the constructor recognise and reuse it across rebuilds.
    static class ScrollRowLayout extends HorizontalScrollView {
        final RowLayout inner;

        ScrollRowLayout(Context context) {
            super(context);
            inner = new RowLayout(context);
            inner.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
            addView(inner);
        }
    }

    private final RowLayout ll_ref;

    public Row(Runnable children) {
        BuildContext c = BuildContext.current();
        // Accept an existing ScrollRowLayout from a previous render so the scroll
        // container is reused instead of replaced on every rebuild.
        View existing = c.peekSlot();
        if (existing instanceof ScrollRowLayout) {
            c.advanceSlot();
            ll_ref = ((ScrollRowLayout)existing).inner;
        } else {
            ll_ref = c.slot(RowLayout.class, () -> new RowLayout(c.getContext()));
        }
        var inner = c.scope(ll_ref);
        BuildContext.push(inner);
        try {
            children.run();
        } finally {
            inner.cleanup();
            BuildContext.pop();
        }
    }

    /// Keyed constructor: the RowLayout is matched by key across renders.
    public Row(Object key, Runnable children) {
        BuildContext c = BuildContext.current();
        ll_ref = c.slot(key, RowLayout.class, () -> new RowLayout(c.getContext()));
        var inner = c.scope(ll_ref);
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
            var parent = ll_ref.getParent();
            if (parent instanceof ScrollRowLayout) {
                // Already wrapped from a previous render; just re-apply styling.
                modifier.applyTo((ScrollRowLayout)parent);
                modifier.applyLayoutTo((ScrollRowLayout)parent);
            } else if (parent instanceof ViewGroup) {
                // First scroll render: wrap the existing RowLayout in a ScrollRowLayout.
                var vg = (ViewGroup)parent;
                var srl = new ScrollRowLayout(ll_ref.getContext());
                // Move rendered children into srl.inner so they survive the wrap.
                while (ll_ref.getChildCount() > 0) {
                    var child = ll_ref.getChildAt(0);
                    ll_ref.removeViewAt(0);
                    srl.inner.addView(child);
                }
                int idx = vg.indexOfChild(ll_ref);
                vg.removeView(ll_ref);
                vg.addView(srl, idx);
                modifier.applyTo(srl);
                modifier.applyLayoutTo(srl);
            }
        } else {
            modifier.applyTo(ll_ref);
            modifier.applyLayoutTo(ll_ref);
        }
        return this;
    }
}
