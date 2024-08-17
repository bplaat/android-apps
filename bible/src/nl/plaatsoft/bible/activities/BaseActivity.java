/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.activities;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PowerManager;
import android.view.ViewGroup;
import android.view.WindowInsets;
import java.util.Locale;

import nl.plaatsoft.bible.Consts;

public abstract class BaseActivity extends Activity {
    protected SharedPreferences settings;

    @Override
    public void attachBaseContext(Context context) {
        settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        var language = settings.getInt("language", Consts.Settings.LANGUAGE_DEFAULT);
        var theme = settings.getInt("theme", Consts.Settings.THEME_DEFAULT);

        // Update configuration when they differ from system defaults or when in battery saver mode
        if (
            language != Consts.Settings.LANGUAGE_SYSTEM ||
            theme != Consts.Settings.THEME_SYSTEM ||
            (theme == Consts.Settings.THEME_SYSTEM && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
        ) {
            var configuration = new Configuration(context.getResources().getConfiguration());

            if (language == Consts.Settings.LANGUAGE_ENGLISH)
                configuration.setLocale(new Locale("en"));
            if (language == Consts.Settings.LANGUAGE_DUTCH)
                configuration.setLocale(new Locale("nl"));

            if (theme == Consts.Settings.THEME_LIGHT) {
                configuration.uiMode |= Configuration.UI_MODE_NIGHT_NO;
                configuration.uiMode &= ~Configuration.UI_MODE_NIGHT_YES;
            }
            if (theme == Consts.Settings.THEME_DARK) {
                configuration.uiMode |= Configuration.UI_MODE_NIGHT_YES;
                configuration.uiMode &= ~Configuration.UI_MODE_NIGHT_NO;
            }
            // Set dark mode on when in battery saver mode
            if (theme == Consts.Settings.THEME_SYSTEM && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
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

    @SuppressWarnings("deprecation")
    protected void useWindowInsets(ViewGroup ...scrollViews) {
        getWindow().getDecorView().setOnApplyWindowInsetsListener((view, windowInsets) -> {
            if (scrollViews != null) {
                for (var scrollView : scrollViews) {
                    scrollView.setClipToPadding(false);
                    if (scrollView.getTag() == null)
                        scrollView.setTag(scrollView.getPaddingBottom());
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                var insets = windowInsets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout() | WindowInsets.Type.ime());
                view.setPadding(insets.left, insets.top, insets.right, scrollViews != null ? 0 : insets.bottom);
                if (scrollViews != null) {
                    for (var scrollView : scrollViews)
                        scrollView.setPadding(
                            scrollView.getPaddingLeft(),
                            scrollView.getPaddingTop(),
                            scrollView.getPaddingRight(),
                            (int)scrollView.getTag() + insets.bottom
                        );
                }
            } else {
                view.setPadding(
                    windowInsets.getSystemWindowInsetLeft(),
                    windowInsets.getSystemWindowInsetTop(),
                    windowInsets.getSystemWindowInsetRight(),
                    scrollViews != null ? 0 : windowInsets.getSystemWindowInsetBottom()
                );
                if (scrollViews != null) {
                    for (var scrollView : scrollViews)
                        scrollView.setPadding(
                            scrollView.getPaddingLeft(),
                            scrollView.getPaddingTop(),
                            scrollView.getPaddingRight(),
                            (int)scrollView.getTag() + windowInsets.getSystemWindowInsetBottom()
                        );
                }
            }
            return windowInsets;
        });
    }
}
