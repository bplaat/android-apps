/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.reactdroid;

import android.content.Context;

public class WidgetContext {
    protected Context context;

    public WidgetContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
}
