/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import nl.plaatsoft.bible.activities.MainActivity;

public class DailyVerseNotification {
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "daily_verse_channel";
    private static final String CHANNEL_NAME = "Daily Verse Channel";

    public static void createChannelIfNeeded(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Bible daily verse notifications");

            var nm = context.getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }
    }

    public static void showNotification(Context context, String title, String text) {
        var intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        var flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        var pendingIntent = PendingIntent.getActivity(context, 0, intent, flags);

        var builder = new Notification.Builder(context)
                          .setSmallIcon(R.drawable.ic_book_cross)
                          .setContentTitle(title)
                          .setContentText(text)
                          .setContentIntent(pendingIntent)
                          .setAutoCancel(true) // dismiss when tapped
                          .setWhen(System.currentTimeMillis());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(Notification.PRIORITY_DEFAULT);
        }

        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= 33) {
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        var notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        var notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
