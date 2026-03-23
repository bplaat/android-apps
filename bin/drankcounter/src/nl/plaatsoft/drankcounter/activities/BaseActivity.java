/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.drankcounter.activities;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.PowerManager;
import android.view.ViewGroup;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import nl.plaatsoft.android.compat.CompatActivity;
import nl.plaatsoft.android.compat.WindowInsetsCompat;
import nl.plaatsoft.drankcounter.Settings;

import org.jspecify.annotations.Nullable;

public abstract class BaseActivity extends CompatActivity {
    protected @SuppressWarnings("null") Settings settings;

    @Override
    public void attachBaseContext(@SuppressWarnings("null") Context context) {
        settings = new Settings(context);
        var language = settings.getLanguage();
        var theme = settings.getTheme();

        // Update configuration when different from system defaults
        var needsLanguageOverride =
            language != Settings.LANGUAGE_SYSTEM && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU;
        var needsThemeOverride = theme != Settings.THEME_SYSTEM;
        var needsBatterySaverOverride = theme == Settings.THEME_SYSTEM && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q;
        if (needsLanguageOverride || needsThemeOverride || needsBatterySaverOverride) {
            var configuration = new Configuration(context.getResources().getConfiguration());
            if (needsLanguageOverride) {
                if (language == Settings.LANGUAGE_ENGLISH)
                    configuration.setLocale(Locale.forLanguageTag("en"));
                if (language == Settings.LANGUAGE_DUTCH)
                    configuration.setLocale(Locale.forLanguageTag("nl"));
            }
            if (needsThemeOverride) {
                if (theme == Settings.THEME_LIGHT) {
                    configuration.uiMode |= Configuration.UI_MODE_NIGHT_NO;
                    configuration.uiMode &= ~Configuration.UI_MODE_NIGHT_YES;
                }
                if (theme == Settings.THEME_DARK) {
                    configuration.uiMode |= Configuration.UI_MODE_NIGHT_YES;
                    configuration.uiMode &= ~Configuration.UI_MODE_NIGHT_NO;
                }
            }
            if (needsBatterySaverOverride) {
                if (((PowerManager)context.getSystemService(Context.POWER_SERVICE)).isPowerSaveMode()) {
                    configuration.uiMode |= Configuration.UI_MODE_NIGHT_YES;
                    configuration.uiMode &= ~Configuration.UI_MODE_NIGHT_NO;
                } else {
                    configuration.uiMode |= Configuration.UI_MODE_NIGHT_NO;
                    configuration.uiMode &= ~Configuration.UI_MODE_NIGHT_YES;
                }
            }
            super.attachBaseContext(context.createConfigurationContext(configuration));
            return;
        }
        super.attachBaseContext(context);
    }
}
