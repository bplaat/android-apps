/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package com.ycombinator.news.activities;

import android.content.Intent;
import android.os.Bundle;

import org.jspecify.annotations.Nullable;

import com.ycombinator.news.components.StoryScreen;

public class StoryActivity extends BaseActivity {
    public static final String EXTRA_STORY_ID = "story_id";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        var storyId = getIntent().getIntExtra(EXTRA_STORY_ID, 0);
        setContentView(new StoryScreen(this, storyId, username -> {
            var intent = new Intent(this, UserActivity.class);
            intent.putExtra(UserActivity.EXTRA_USERNAME, username);
            startActivity(intent);
        }));
    }
}
