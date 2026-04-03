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
    // Distinct subclass so slot() can tell Column apart from Row
    static class ColumnLayout extends LinearLayout {
        ColumnLayout(Context context) {
            super(context);
            setOrientation(VERTICAL);
        }
    }

    // Scrollable variant: a ScrollView whose single child is a ColumnLayout.
    // Using a distinct subclass lets the constructor recognise and reuse it across rebuilds.
    static class ScrollColumnLayout extends ScrollView {
        final ColumnLayout inner;

        ScrollColumnLayout(Context context) {
            super(context);
            inner = new ColumnLayout(context);
            inner.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            addView(inner);
        }
    }

    private final ColumnLayout ll_ref;

    public Column(Runnable children) {
        BuildContext c = BuildContext.current();
        // Accept an existing ScrollColumnLayout from a previous render so the scroll
        // container is reused instead of replaced on every rebuild.
        View existing = c.peekSlot();
        if (existing instanceof ScrollColumnLayout) {
            c.advanceSlot();
            ll_ref = ((ScrollColumnLayout)existing).inner;
        } else {
            ll_ref = c.slot(ColumnLayout.class, () -> new ColumnLayout(c.getContext()));
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

    /// Keyed constructor: the ColumnLayout is matched by key across renders so that
    /// reordering or inserting items in a list does not recreate unrelated columns.
    public Column(Object key, Runnable children) {
        BuildContext c = BuildContext.current();
        ll_ref = c.slot(key, ColumnLayout.class, () -> new ColumnLayout(c.getContext()));
        var inner = c.scope(ll_ref);
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
            var parent = ll_ref.getParent();
            if (parent instanceof ScrollColumnLayout) {
                // Already wrapped from a previous render; just re-apply styling.
                modifier.applyTo((ScrollColumnLayout)parent);
                modifier.applyLayoutTo((ScrollColumnLayout)parent);
            } else if (parent instanceof ViewGroup) {
                // First scroll render: wrap the existing ColumnLayout in a ScrollColumnLayout.
                var vg = (ViewGroup)parent;
                var scl = new ScrollColumnLayout(ll_ref.getContext());
                // Move rendered children into scl.inner so they survive the wrap.
                while (ll_ref.getChildCount() > 0) {
                    var child = ll_ref.getChildAt(0);
                    ll_ref.removeViewAt(0);
                    scl.inner.addView(child);
                }
                int idx = vg.indexOfChild(ll_ref);
                vg.removeView(ll_ref);
                vg.addView(scl, idx);
                modifier.applyTo(scl);
                modifier.applyLayoutTo(scl);
            }
        } else {
            modifier.applyTo(ll_ref);
            modifier.applyLayoutTo(ll_ref);
        }
        return this;
    }
}
