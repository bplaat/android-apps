/*
 * Copyright (c) 2021-2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.rfidviewer.activities;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;
import java.util.Locale;
import javax.annotation.Nullable;

import nl.plaatsoft.rfidviewer.Settings;

public abstract class BaseActivity extends Activity {
    protected @SuppressWarnings("null") Settings settings;
    private @Nullable OnBackInvokedCallback onBackCallback;

    @Override
    public void attachBaseContext(@SuppressWarnings("null") Context context) {
        settings = new Settings(context);
        var language = settings.getLanguage();
        var theme = settings.getTheme();

        // Update configuration when different from system defaults
        if (language != Settings.LANGUAGE_SYSTEM || theme != Settings.THEME_SYSTEM
                || (theme == Settings.THEME_SYSTEM && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)) {
            var configuration = new Configuration(context.getResources().getConfiguration());

            if (language == Settings.LANGUAGE_ENGLISH)
                configuration.setLocale(Locale.forLanguageTag("en"));
            if (language == Settings.LANGUAGE_DUTCH)
                configuration.setLocale(Locale.forLanguageTag("nl"));

            if (theme == Settings.THEME_LIGHT) {
                configuration.uiMode |= Configuration.UI_MODE_NIGHT_NO;
                configuration.uiMode &= ~Configuration.UI_MODE_NIGHT_YES;
            }
            if (theme == Settings.THEME_DARK) {
                configuration.uiMode |= Configuration.UI_MODE_NIGHT_YES;
                configuration.uiMode &= ~Configuration.UI_MODE_NIGHT_NO;
            }
            // Set dark mode on when in battery saver mode
            if (theme == Settings.THEME_SYSTEM && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (((PowerManager) context.getSystemService(Context.POWER_SERVICE)).isPowerSaveMode()) {
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
    protected void useWindowInsets(ViewGroup... scrollViews) {
        getWindow().getDecorView().setOnApplyWindowInsetsListener((view, windowInsets) -> {
            if (scrollViews != null) {
                for (var scrollView : scrollViews) {
                    scrollView.setClipToPadding(false);
                    if (scrollView.getTag() == null)
                        scrollView.setTag(scrollView.getPaddingBottom());
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                var insets = windowInsets.getInsets(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout() | WindowInsets.Type.ime());
                view.setPadding(insets.left, insets.top, insets.right, scrollViews != null ? 0 : insets.bottom);
                if (scrollViews != null) {
                    for (var scrollView : scrollViews)
                        scrollView.setPadding(
                                scrollView.getPaddingLeft(),
                                scrollView.getPaddingTop(),
                                scrollView.getPaddingRight(),
                                (int) scrollView.getTag() + insets.bottom);
                }
            } else {
                view.setPadding(
                        windowInsets.getSystemWindowInsetLeft(),
                        windowInsets.getSystemWindowInsetTop(),
                        windowInsets.getSystemWindowInsetRight(),
                        scrollViews != null ? 0 : windowInsets.getSystemWindowInsetBottom());
                if (scrollViews != null) {
                    for (var scrollView : scrollViews)
                        scrollView.setPadding(
                                scrollView.getPaddingLeft(),
                                scrollView.getPaddingTop(),
                                scrollView.getPaddingRight(),
                                (int) scrollView.getTag() + windowInsets.getSystemWindowInsetBottom());
                }
            }
            return windowInsets;
        });
    }

    // Back button override
    protected boolean shouldBackOverride() {
        return false;
    }

    protected void onBack() {
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (shouldBackOverride()) {
            onBack();
        } else {
            super.onBackPressed();
        }
    }

    protected void updateBackListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (onBackCallback == null)
                onBackCallback = () -> onBack();
            if (shouldBackOverride()) {
                getOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                        onBackCallback);
            } else {
                getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(onBackCallback);
            }
        }
    }
}
