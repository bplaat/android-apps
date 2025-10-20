/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.reactdroid;

import android.view.View;
import android.view.ViewGroup;

import org.jspecify.annotations.Nullable;

public abstract class StatefulWidget extends StatelessWidget {
    private ViewGroup parent;
    private View view;

    protected StatefulWidget(WidgetContext c) {
        super(c);
    }

    public void refresh() {
        build().render(parent, view);
    }

    @Override
    public View render(ViewGroup parent, @Nullable View view) {
        this.parent = parent;
        return this.view = build().render(parent, view);
    }
}
