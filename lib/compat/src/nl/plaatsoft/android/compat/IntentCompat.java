/*
 * Copyright (c) 2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.compat;

import java.io.Serializable;

import android.content.Intent;
import android.os.Build;

public class IntentCompat {
    private IntentCompat() {
    }

    @SuppressWarnings("deprecation")
    public static <T> T getParcelableExtra(Intent intent, String name, Class<T> clazz) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            return intent.getParcelableExtra(name, clazz);
        return intent.getParcelableExtra(name);
    }

    @SuppressWarnings({ "deprecation", "unchecked" })
    public static <T extends Serializable> T getSerializableExtra(Intent intent, String name, Class<T> clazz) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return intent.getSerializableExtra(name, clazz);
        return (T) intent.getSerializableExtra(name);
    }
}
