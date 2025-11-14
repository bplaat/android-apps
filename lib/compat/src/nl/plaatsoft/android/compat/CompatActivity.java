/*
 * Copyright (c) 2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.compat;

import java.util.Locale;

import android.app.Activity;
import android.os.Build;
import android.view.ViewGroup;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import org.jspecify.annotations.Nullable;

public abstract class CompatActivity extends Activity {
    private @Nullable OnBackInvokedCallback onBackCallback;

    // MARK: Window insets
    protected void useWindowInsets(ViewGroup... scrollViews) {
        getWindow().getDecorView().setOnApplyWindowInsetsListener((view, windowInsets) -> {
            var insets = WindowInsetsCompat.getInsets(windowInsets);
            if (scrollViews != null && scrollViews.length > 0) {
                view.setPadding(insets.left(), insets.top(), insets.right(), 0);
                for (var scrollView : scrollViews) {
                    if (scrollView != null) {
                        scrollView.setClipToPadding(false);
                        scrollView.setPaddingRelative(0, 0, 0, insets.bottom());
                    }
                }
            } else {
                view.setPadding(insets.left(), insets.top(), insets.right(), insets.bottom());
            }
            return windowInsets;
        });
    }

    // MARK: Back button
    protected boolean shouldBackOverride() {
        return false;
    }

    protected void onBack() {
        // Default noop
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
                getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, onBackCallback);
            } else {
                getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(onBackCallback);
            }
        }
    }
}
