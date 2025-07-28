/*
 * Copyright (c) 2021-2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.rfidviewer;

import android.content.Intent;
import android.os.Build;

public class Utils {
    private Utils() {
    }

    @SuppressWarnings("deprecation")
    public static <T> T intentGetParcelableExtra(Intent intent, String name, Class<T> clazz) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            return intent.getParcelableExtra(name, clazz);
        return intent.getParcelableExtra(name);
    }
}
