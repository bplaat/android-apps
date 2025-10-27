/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.reactdroid;

import android.view.View;
import android.view.ViewGroup;

import org.jspecify.annotations.Nullable;

public abstract class StatelessWidget extends Widget {
    public StatelessWidget() {
        super();
    }

    abstract public void build();

    @Override
    public View render(ViewGroup parent, @Nullable View view) {
        var widgets = new ArrayList<Widget>();
        WidgetContext.widgets = widgets;
        build();
        // return super.render(parent, view);
    }
}
