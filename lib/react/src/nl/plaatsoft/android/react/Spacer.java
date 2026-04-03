/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import android.content.Context;
import android.view.View;

/// An empty view that takes up space. Size is set via modifier:
///   new Spacer().modifier(Modifier.of().size(16))
///   new Spacer().modifier(Modifier.of().fillHeight())
public class Spacer {
    // Distinct subclass so slot() can tell Spacer apart from other plain Views
    private static class SpacerView extends View {
        SpacerView(Context context) {
            super(context);
        }
    }

    private final SpacerView sv_ref;

    public Spacer() {
        var c = BuildContext.current();
        sv_ref = c.slot(SpacerView.class, () -> new SpacerView(c.getContext()));
    }

    public Spacer(Object key) {
        var c = BuildContext.current();
        sv_ref = c.slot(key, SpacerView.class, () -> new SpacerView(c.getContext()));
    }

    public Spacer modifier(Modifier modifier) {
        modifier.applyTo(sv_ref);
        modifier.applyLayoutTo(sv_ref);
        return this;
    }
}
