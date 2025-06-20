/*
 * Copyright (c) 2020-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bassietest.activities;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import nl.plaatsoft.android.alerts.RatingAlert;
import nl.plaatsoft.android.alerts.UpdateAlert;
import nl.plaatsoft.android.fetch.FetchDataTask;
import nl.plaatsoft.android.fetch.FetchImageTask;
import nl.plaatsoft.bassietest.R;
import nl.plaatsoft.bassietest.Utils;

public class MainActivity extends BaseActivity implements PopupMenu.OnMenuItemClickListener {
    private static final int SETTINGS_REQUEST_CODE = 1;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private @SuppressWarnings("null") ViewSwitcher pageSwitcher;
    private int oldLanguage = -1;
    private int oldTheme = -1;
    private boolean imageLoaded = false;
    private boolean infoLoaded = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pageSwitcher = findViewById(R.id.main_page_switcher);
        useWindowInsets(findViewById(R.id.main_landing_page), findViewById(R.id.main_data_page));

        // Options menu button
        findViewById(R.id.main_options_menu_button).setOnClickListener(view -> {
            var optionsMenu = new PopupMenu(this, view, Gravity.TOP | Gravity.RIGHT);
            optionsMenu.getMenuInflater().inflate(R.menu.options, optionsMenu.getMenu());
            optionsMenu.setOnMenuItemClickListener(this);
            optionsMenu.show();
        });

        // Landing action button
        findViewById(R.id.main_landing_hero_button).setOnClickListener(view -> {
            pageSwitcher.showNext();
            updateBackListener();

            // Fetch random Unsplash image
            if (!imageLoaded) {
                imageLoaded = true;
                var density = getResources().getDisplayMetrics().density;
                FetchImageTask.with(this)
                        .load("https://picsum.photos/" + (int) (320 * density) + "/" + (int) (240 * density))
                        .noCache()
                        .fadeIn()
                        .loadingColor(Utils.contextGetColor(this, R.color.loading_background_color))
                        .into(findViewById(R.id.main_data_random_image))
                        .fetch();
            }

            // Fetch IP info
            if (!infoLoaded) {
                infoLoaded = true;
                FetchDataTask.with(this).load("https://ipinfo.io/json").then(data -> {
                    try {
                        var jsonData = new JSONObject(new String(data, StandardCharsets.UTF_8));
                        var locationLabel = (TextView) findViewById(R.id.main_data_location_label);
                        locationLabel.setText(jsonData.getString("city") + ", " + jsonData.getString("region"));

                        var set = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.text_fade_in);
                        set.setTarget(locationLabel);
                        set.start();
                    } catch (JSONException exception) {
                        Log.e(getPackageName(), "Can't parse IP info", exception);
                    }
                }).fetch();
            }
        });

        // Show rating alert
        RatingAlert.updateAndShow(this, SettingsActivity.STORE_PAGE_URL);

        // Show update alert
        UpdateAlert.checkAndShow(this,
                "https://raw.githubusercontent.com/bplaat/android-apps/refs/heads/master/bassietest/bob.toml",
                SettingsActivity.STORE_PAGE_URL);
    }

    @Override
    public boolean onMenuItemClick(@SuppressWarnings("null") MenuItem item) {
        if (item.getItemId() == R.id.menu_options_settings) {
            oldLanguage = settings.getLanguage();
            oldTheme = settings.getTheme();
            startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_REQUEST_CODE);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @SuppressWarnings("null") Intent data) {
        // When settings activity is closed check for restart
        if (requestCode == SETTINGS_REQUEST_CODE) {
            if (oldLanguage != -1 && oldTheme != -1) {
                if (oldLanguage != settings.getLanguage() || oldTheme != settings.getTheme()) {
                    handler.post(() -> recreate());
                }
            }
        }
    }

    @Override
    protected boolean shouldBackOverride() {
        if (pageSwitcher.getDisplayedChild() == 1)
            return true;
        return false;
    }

    @Override
    protected void onBack() {
        if (pageSwitcher.getDisplayedChild() == 1) {
            pageSwitcher.showPrevious();
            updateBackListener();
        }
    }
}
