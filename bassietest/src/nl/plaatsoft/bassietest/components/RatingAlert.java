package nl.plaatsoft.bassietest.components;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import nl.plaatsoft.bassietest.R;

public class RatingAlert {
    private static final int LAUNCHES_UNTIL_PROMPT = 5;
    private static final int TIME_UNTIL_PROMPT = 2 * 24 * 60 * 60 * 1000;

    public static void updateAndShow(Context context, String storePageUrl) {
        var settings = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (settings.getBoolean("rating_alert_hidden", false))
            return;

        // Increment or set rating counters and time
        var launchCount = settings.getInt("rating_alert_launch_count", 0) + 1;
        var firstLaunchTime = settings.getLong("rating_alert_first_launch_time", -1);
        {
            var settingsEditor = settings.edit();
            settingsEditor.putInt("rating_alert_launch_count", launchCount);
            if (firstLaunchTime == -1) {
                firstLaunchTime = System.currentTimeMillis();
                settingsEditor.putLong("rating_alert_first_launch_time", firstLaunchTime);
            }
            settingsEditor.commit();
        }

        // Wait at least some time before opening
        if (
            launchCount >= LAUNCHES_UNTIL_PROMPT &&
            System.currentTimeMillis() - firstLaunchTime >= TIME_UNTIL_PROMPT
        ) {
            new AlertDialog.Builder(context)
                .setTitle(R.string.rating_alert_title_label)
                .setMessage(R.string.rating_alert_message_label)
                .setNeutralButton(R.string.rating_alert_later_button, (dialog, whichButton) -> {
                    // Reset the rating counters
                    var settingsEditor = settings.edit();
                    settingsEditor.putInt("rating_alert_launch_count", 0);
                    settingsEditor.putLong("rating_alert_first_launch_time", System.currentTimeMillis());
                    settingsEditor.commit();
                })
                .setNegativeButton(R.string.rating_alert_never_button, (dialog, whichButton)-> {
                    var settingsEditor = settings.edit();
                    settingsEditor.putBoolean("rating_alert_hidden", true);
                    settingsEditor.commit();
                })
                .setPositiveButton(R.string.rating_alert_rating_button, (dialog, whichButton) -> {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(storePageUrl)));
                    var settingsEditor = settings.edit();
                    settingsEditor.putBoolean("rating_alert_hidden", true);
                    settingsEditor.commit();
                })
                .show();
        }
    }
}
