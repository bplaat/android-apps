/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.reacttest.activities;

import android.os.Bundle;
import android.view.ViewGroup;

import nl.plaatsoft.android.reactdroid.WidgetContext;
import nl.plaatsoft.reacttest.components.HomeScreen;

import org.jspecify.annotations.Nullable;

public class MainActivity extends BaseActivity {
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        var context = new WidgetContext(this);
        var root = (ViewGroup) findViewById(android.R.id.content);
        new HomeScreen(context).render(root, null);
        useWindowInsets();
    }
}
