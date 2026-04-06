/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package com.ycombinator.news.activities;

import android.os.Bundle;

import org.jspecify.annotations.Nullable;

import com.ycombinator.news.components.SettingsScreen;

public class SettingsActivity extends BaseActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SettingsScreen(this));
    }
}
