/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import android.content.Context;
import android.widget.FrameLayout;

public class Box {
    static class BoxLayout extends FrameLayout {
        BoxLayout(Context context) {
            super(context);
        }
    }

    private final BoxLayout ref;

    public Box(Runnable children) {
        var c = BuildContext.current();
        ref = c.slot(BoxLayout.class, () -> new BoxLayout(c.getContext()));
        var inner = c.scope(ref);
        BuildContext.push(inner);
        try {
            children.run();
        } finally {
            inner.cleanup();
            BuildContext.pop();
        }
    }

    public Box modifier(Modifier modifier) {
        modifier.applyTo(ref);
        modifier.applyLayoutTo(ref);
        return this;
    }

    public Box onClick(Runnable handler) {
        ref.setOnClickListener(v -> handler.run());
        return this;
    }

    public Box onClick(android.view.View.OnClickListener handler) {
        ref.setOnClickListener(handler);
        return this;
    }
}
