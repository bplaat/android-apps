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

public class MainActivity extends BaseActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        var context = new WidgetContext(this);
        var root = (ViewGroup) findViewById(android.R.id.content);
        HomeScreen.create(context).render(root, null);

        useWindowInsets();
    }
}
