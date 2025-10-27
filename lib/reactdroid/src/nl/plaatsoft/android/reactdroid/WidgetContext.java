/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.reactdroid;

import java.util.List;

import android.content.Context;

public class WidgetContext {
    public static Context context;
    public static List<Widget> widgets;

    public static Context getContext() {
        return context;
    }
}
