/*
 * Copyright (c) 2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.compat;

import android.os.Build;
import android.view.WindowInsets;

public class WindowInsetsCompat {
    private WindowInsetsCompat() {}

    public static record Insets(int left, int top, int right, int bottom) {}

    @SuppressWarnings("deprecation")
    public static Insets getInsets(WindowInsets windowInsets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            var insets = windowInsets.getInsets(
                WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout() | WindowInsets.Type.ime());
            return new Insets(insets.left, insets.top, insets.right, insets.bottom);
        } else {
            return new Insets(windowInsets.getSystemWindowInsetLeft(), windowInsets.getSystemWindowInsetTop(),
                windowInsets.getSystemWindowInsetRight(), windowInsets.getSystemWindowInsetBottom());
        }
    }
}
