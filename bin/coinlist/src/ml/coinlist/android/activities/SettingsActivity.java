/*
 * Copyright (c) 2021-2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package ml.coinlist.android.activities;

import android.os.Bundle;

import org.jspecify.annotations.Nullable;

import ml.coinlist.android.components.SettingsScreen;

public class SettingsActivity extends BaseActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SettingsScreen(this));
    }
}
