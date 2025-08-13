/*
 * Copyright (c) 2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.compat;

import android.os.Build;
import android.webkit.WebSettings;

public class WebSettingsCompat {
    private WebSettingsCompat() {
    }

    @SuppressWarnings("deprecation")
    public static void setForceDark(WebSettings webSettings, boolean forceDark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            webSettings.setAlgorithmicDarkeningAllowed(forceDark);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            webSettings.setForceDark(forceDark ? WebSettings.FORCE_DARK_ON : WebSettings.FORCE_DARK_OFF);
        }
    }
}
