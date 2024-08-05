package nl.plaatsoft.bassietest.components;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import nl.plaatsoft.bassietest.Consts;
import nl.plaatsoft.bassietest.Utils;
import nl.plaatsoft.bassietest.R;

public class RatingAlert {
    public static void updateAndShow(Context context) {
        // Get settings
        SharedPreferences settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE);

        // Check if the rating is hidden for ever
        if (settings.getBoolean("rating_alert_hidden", false)) {
            return;
        }

        SharedPreferences.Editor settingsEditor = settings.edit();

        // Increment launch counter
        int launchCount = settings.getInt("rating_alert_launch_count", 0) + 1;
        settingsEditor.putInt("rating_alert_launch_count", launchCount);

        // Set date of first launch when not set
        long firstLaunchTime = settings.getLong("rating_alert_first_launch_time", 0);
        if (firstLaunchTime == 0) {
            firstLaunchTime = System.currentTimeMillis();
            settingsEditor.putLong("rating_alert_first_launch_time", firstLaunchTime);
        }

        settingsEditor.commit();

        // Wait at least n days before opening
        if (
            launchCount >= Consts.RATING_ALERT_LAUNCHES_UNTIL_PROMPT &&
            System.currentTimeMillis() - firstLaunchTime >= Consts.RATING_ALERT_TIME_UNTIL_PROMPT
        ) {
            // Show rating alert
            new AlertDialog.Builder(context)
                .setTitle(R.string.rating_alert_title_label)
                .setMessage(R.string.rating_alert_message_label)
                .setNeutralButton(R.string.rating_alert_later_button, (dialog, whichButton) -> {
                    // Reset the rating counters
                    SharedPreferences.Editor otherSettingsEditor = settings.edit();
                    otherSettingsEditor.putInt("rating_alert_launch_count", 0);
                    otherSettingsEditor.putLong("rating_alert_first_launch_time", System.currentTimeMillis());
                    otherSettingsEditor.commit();
                })
                .setNegativeButton(R.string.rating_alert_never_button, (dialog, whichButton)-> {
                    // Set the rating hidden flag
                    SharedPreferences.Editor otherSettingsEditor = settings.edit();
                    otherSettingsEditor.putBoolean("rating_alert_hidden", true);
                    otherSettingsEditor.commit();
                })
                .setPositiveButton(R.string.rating_alert_rating_button, (dialog, whichButton) -> {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Consts.STORE_PAGE_URL)));

                    // Set the rating hidden flag
                    SharedPreferences.Editor otherSettingsEditor = settings.edit();
                    otherSettingsEditor.putBoolean("rating_alert_hidden", true);
                    otherSettingsEditor.commit();
                })
                .show();
        }
    }
}
