/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.reacttest.activities;

import android.app.Activity;
import android.os.Bundle;

import nl.plaatsoft.reacttest.components.SettingsScreen;

import org.jspecify.annotations.Nullable;

public class SettingsActivity extends Activity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new SettingsScreen(this));
    }
}
