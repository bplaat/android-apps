/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package net.tweakers.android;

import android.app.Activity;
import android.os.Build;
import android.view.ViewGroup;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import nl.plaatsoft.android.compat.WindowInsetsCompat;

import org.jspecify.annotations.Nullable;

public abstract class BaseActivity extends Activity {
    private @Nullable OnBackInvokedCallback onBackCallback;

    protected void useWindowInsets(ViewGroup... scrollViews) {
        getWindow().getDecorView().setOnApplyWindowInsetsListener((view, windowInsets) -> {
            if (scrollViews != null) {
                for (var scrollView : scrollViews) {
                    scrollView.setClipToPadding(false);
                    if (scrollView.getTag() == null)
                        scrollView.setTag(scrollView.getPaddingBottom());
                }
            }

            var insets = WindowInsetsCompat.getInsets(windowInsets);
            view.setPadding(insets.left(), insets.top(), insets.right(), scrollViews != null ? 0 : insets.bottom());
            if (scrollViews != null) {
                for (var scrollView : scrollViews)
                    scrollView.setPadding(scrollView.getPaddingLeft(), scrollView.getPaddingTop(),
                            scrollView.getPaddingRight(), (int) scrollView.getTag() + insets.bottom());
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
                getOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                        onBackCallback);
            } else {
                getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(onBackCallback);
            }
        }
    }
}
