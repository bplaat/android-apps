/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible;

import android.content.Context;
import android.os.Build;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class Utils {
    private Utils() {
    }

    @SuppressWarnings("deprecation")
    public static int contextGetColor(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return context.getResources().getColor(id, null);
        return context.getResources().getColor(id);
    }

    @SuppressWarnings("deprecation")
    public static String[] contextGetLanguages(Context context) {
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
