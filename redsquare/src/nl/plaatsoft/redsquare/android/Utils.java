/*
 * Copyright (c) 2020-2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.redsquare.android;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.Window;
import java.security.MessageDigest;

public class Utils {
    private Utils() {}

    @SuppressWarnings("deprecation")
    public static int contextGetColor(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return context.getResources().getColor(id, null);
        return context.getResources().getColor(id);
    }
}
