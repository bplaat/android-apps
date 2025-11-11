/*
 * Copyright (c) 2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.nos.android.activities;

import android.os.Bundle;
import android.view.ViewGroup;

import nl.plaatsoft.android.fetch.FetchImageTask;
import nl.plaatsoft.nos.android.R;

public class ImageActivity extends BaseActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        useWindowInsets(new ViewGroup[] {null});

        findViewById(R.id.image_back_button).setOnClickListener(view -> { finish(); });

        FetchImageTask.with(this).load(getIntent().getStringExtra("url")).into(findViewById(R.id.image_image)).fetch();
    }
}
