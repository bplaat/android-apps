/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.reactdroid;

import android.view.View;
import android.view.ViewGroup;

abstract public class StatelessWidget extends Widget {
    public StatelessWidget(WidgetContext context) {
        super(context);
    }

    abstract public Widget build();

    public View render(ViewGroup parent, View view) {
        return build().render(parent, view);
    }
}
