/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import android.content.Context;
import android.widget.FrameLayout;

/// Stacks children on top of each other, like a FrameLayout.
/// Children can be absolutely positioned with Modifier.of().position(x, y)
/// or aligned with Modifier.of().align(Gravity.CENTER).
public class Box {
    // Distinct subclass so slot() can tell Box apart from other FrameLayouts
    static class BoxLayout extends FrameLayout {
        BoxLayout(Context context) {
            super(context);
        }
    }

    private final BoxLayout fl_ref;

    public Box(Runnable children) {
        var c = BuildContext.current();
        var fl = c.slot(BoxLayout.class, () -> new BoxLayout(c.getContext()));
        fl_ref = fl;
        var inner = c.scope(fl);
        BuildContext.push(inner);
        try {
            children.run();
        } finally {
            inner.cleanup();
            BuildContext.pop();
        }
    }

    /// Keyed constructor: the BoxLayout is matched by key across renders.
    public Box(Object key, Runnable children) {
        var c = BuildContext.current();
        var fl = c.slot(key, BoxLayout.class, () -> new BoxLayout(c.getContext()));
        fl_ref = fl;
        var inner = c.scope(fl);
        BuildContext.push(inner);
        try {
            children.run();
        } finally {
            inner.cleanup();
            BuildContext.pop();
        }
    }

    public Box modifier(Modifier modifier) {
        modifier.applyTo(fl_ref);
        modifier.applyLayoutTo(fl_ref);
        return this;
    }
}
