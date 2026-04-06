/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package com.ycombinator.news.activities;

import android.os.Bundle;

import org.jspecify.annotations.Nullable;

import com.ycombinator.news.components.UserScreen;

public class UserActivity extends BaseActivity {
    public static final String EXTRA_USERNAME = "username";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        var username = getIntent().getStringExtra(EXTRA_USERNAME);
        setContentView(new UserScreen(this, username != null ? username : ""));
    }
}
