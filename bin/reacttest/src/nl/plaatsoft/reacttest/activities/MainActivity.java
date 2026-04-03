/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.reacttest.activities;

import android.os.Bundle;

import nl.plaatsoft.android.compat.CompatActivity;
import nl.plaatsoft.reacttest.components.HomeScreen;

import org.jspecify.annotations.Nullable;

public class MainActivity extends CompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new HomeScreen(this));
        useWindowInsets();
    }
}
