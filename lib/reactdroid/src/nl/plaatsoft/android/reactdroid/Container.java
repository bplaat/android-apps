/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.reactdroid;

import java.util.ArrayList;
import java.util.List;

public abstract class Container extends Widget {
    protected List<Widget> children;

    protected Container(WidgetContext context) {
        super(context);
        children = new ArrayList<Widget>();
    }

    abstract public Container child(Widget child);

    abstract public Container child(List<Widget> children);
}
