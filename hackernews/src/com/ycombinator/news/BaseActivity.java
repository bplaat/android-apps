/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package com.ycombinator.news;

import android.app.Activity;
import android.os.Build;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

public abstract class BaseActivity extends Activity {
    private OnBackInvokedCallback onBackCallback = null;

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

    // Back button override
    protected boolean shouldBackOverride() {
        return false;
    }

    protected void onBack() {}

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
                getOnBackInvokedDispatcher().registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, onBackCallback);
            } else {
                getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(onBackCallback);
            }
        }
    }
}
