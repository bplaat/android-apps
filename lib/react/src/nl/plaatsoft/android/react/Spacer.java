/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.react;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

public class Spacer {
    private static class SpacerLayout extends View {
        SpacerLayout(Context context) {
            super(context);
        }
    }

    private final SpacerLayout ref;

    public Spacer() {
        var c = BuildContext.current();
        ref = c.slot(SpacerLayout.class, () -> new SpacerLayout(c.getContext()));
    }

    public Spacer modifier(Modifier modifier) {
        modifier.applyTo(ref);
        modifier.applyLayoutTo(ref);
        return this;
    }

    public Spacer onClick(Runnable handler) {
        ref.setOnClickListener(v -> handler.run());
        return this;
    }

    public Spacer onClick(OnClickListener handler) {
        ref.setOnClickListener(handler);
        return this;
    }
}
