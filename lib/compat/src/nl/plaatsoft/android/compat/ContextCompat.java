/*
 * Copyright (c) 2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.compat;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

public class ContextCompat {
    private ContextCompat() {
    }

    @SuppressWarnings("deprecation")
    public static String getInstallerPackageName(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            try {
                return context.getPackageManager().getInstallSourceInfo(context.getPackageName())
                        .getInstallingPackageName();
            } catch (NameNotFoundException e) {
            }
        }
        return context.getPackageManager().getInstallerPackageName(context.getPackageName());
    }

    @SuppressWarnings("deprecation")
    public static int getColor(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return context.getResources().getColor(id, null);
        return context.getResources().getColor(id);
    }

    @SuppressWarnings("deprecation")
    public static String[] getLanguages(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            var locals = context.getResources().getConfiguration().getLocales();
            var languages = new String[locals.size()];
            for (int i = 0; i < locals.size(); i++)
                languages[i] = locals.get(i).getLanguage();
            return languages;
        }
        return new String[] { context.getResources().getConfiguration().locale.getLanguage() };
    }
}
