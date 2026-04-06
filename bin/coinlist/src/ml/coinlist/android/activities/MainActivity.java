/*
 * Copyright (c) 2021-2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package ml.coinlist.android.activities;

import android.content.Intent;
import android.os.Bundle;

import nl.plaatsoft.android.alerts.UpdateAlert;

import org.jspecify.annotations.Nullable;

import ml.coinlist.android.components.HomeScreen;
import ml.coinlist.android.components.SettingsScreen;

public class MainActivity extends BaseActivity {
    @SuppressWarnings("null") private HomeScreen homeScreen;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeScreen = new HomeScreen(this, () -> startActivity(new Intent(this, SettingsActivity.class)));
        setContentView(homeScreen);

        UpdateAlert.checkAndShow(this,
            "https://raw.githubusercontent.com/bplaat/android-apps/refs/heads/master/bin/coinlist/bob.toml",
            SettingsScreen.STORE_PAGE_URL);
    }

    @Override
    public void onResume() {
        super.onResume();
        homeScreen.onResume();
    }
}
