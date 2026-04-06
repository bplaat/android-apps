/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package com.ycombinator.news.activities;

import android.content.Intent;
import android.os.Bundle;

import nl.plaatsoft.android.alerts.UpdateAlert;

import org.jspecify.annotations.Nullable;

import com.ycombinator.news.components.HomeScreen;
import com.ycombinator.news.components.SettingsScreen;

public class MainActivity extends BaseActivity {
    private @SuppressWarnings("null") HomeScreen homeScreen;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeScreen = new HomeScreen(this, () -> startActivity(new Intent(this, SettingsActivity.class)), storyId -> {
            var intent = new Intent(this, StoryActivity.class);
            intent.putExtra(StoryActivity.EXTRA_STORY_ID, storyId);
            startActivity(intent);
        });
        setContentView(homeScreen);

        UpdateAlert.checkAndShow(this,
            "https://raw.githubusercontent.com/bplaat/android-apps/refs/heads/master/bin/hackernews/bob.toml",
            SettingsScreen.STORE_PAGE_URL);
    }

    @Override
    public void onResume() {
        super.onResume();
        homeScreen.onResume();
    }
}
