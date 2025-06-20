/*
 * Copyright (c) 2020-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.alerts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class RatingAlert {
    private static final int LAUNCHES_UNTIL_PROMPT = 5;
    private static final int TIME_UNTIL_PROMPT = 2 * 24 * 60 * 60 * 1000;

    public static void updateAndShow(Context context, String storePageUrl) {
        var settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (settings.getBoolean("ratingalert_hidden", false))
            return;

        // Increment or set rating counters and time
        var launchCount = settings.getInt("ratingalert_launch_count", 0) + 1;
        var firstLaunchTime = settings.getLong("ratingalert_first_launch_time", -1);
        {
            var settingsEditor = settings.edit();
            settingsEditor.putInt("ratingalert_launch_count", launchCount);
            if (firstLaunchTime == -1) {
                firstLaunchTime = System.currentTimeMillis();
                settingsEditor.putLong("ratingalert_first_launch_time", firstLaunchTime);
            }
            settingsEditor.commit();
        }

        // Wait at least some time before opening
        if (launchCount >= LAUNCHES_UNTIL_PROMPT &&
                System.currentTimeMillis() - firstLaunchTime >= TIME_UNTIL_PROMPT) {
            var appName = context.getPackageManager().getApplicationLabel(context.getApplicationInfo());
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.ratingalert_title_label).replace("$0", appName))
                    .setMessage(context.getString(R.string.ratingalert_message_label).replace("$0", appName))
                    .setNeutralButton(R.string.ratingalert_later_button, (dialog, which) -> {
                        // Reset the rating counters
                        var settingsEditor = settings.edit();
                        settingsEditor.putInt("ratingalert_launch_count", 0);
                        settingsEditor.putLong("ratingalert_first_launch_time", System.currentTimeMillis());
                        settingsEditor.commit();
                    })
                    .setNegativeButton(R.string.ratingalert_never_button, (dialog, which) -> {
                        var settingsEditor = settings.edit();
                        settingsEditor.putBoolean("ratingalert_hidden", true);
                        settingsEditor.commit();
                    })
                    .setPositiveButton(R.string.ratingalert_rate_button, (dialog, which) -> {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(storePageUrl)));
                        var settingsEditor = settings.edit();
                        settingsEditor.putBoolean("ratingalert_hidden", true);
                        settingsEditor.commit();
                    })
                    .show();
        }
    }
}
