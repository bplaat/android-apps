/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.reactdroid;

public abstract class Bin extends Widget {
    protected Widget child;

    protected Bin(WidgetContext context) {
        super(context);
    }

    abstract public Bin child(Widget child);
}
