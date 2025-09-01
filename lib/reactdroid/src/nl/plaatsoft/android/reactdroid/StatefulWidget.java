/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.reactdroid;

import android.view.View;
import android.view.ViewGroup;

import org.jspecify.annotations.Nullable;

public abstract class StatefulWidget extends Widget {
    protected ViewGroup _parent;
    protected View _view;

    protected StatefulWidget(WidgetContext c) {
        super(c);
    }

    public abstract Widget build();

    public void refresh() {
        build().render(_parent, _view);
    }

    @Override
    public View render(ViewGroup parent, @Nullable View view) {
        _parent = parent;
        return _view = build().render(parent, view);
    }
}
