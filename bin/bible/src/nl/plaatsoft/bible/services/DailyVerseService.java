/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import nl.plaatsoft.bible.R;
import nl.plaatsoft.bible.Settings;
import nl.plaatsoft.bible.models.Book;
import nl.plaatsoft.bible.models.Verse;

public class DailyVerseService extends JobService {
    public static final String NOTIFICATION_BOOK_KEY = "notification_book_key";
    public static final String NOTIFICATION_CHAPTER_NUMBER = "notification_chapter_number";
    public static final String NOTIFICATION_VERSE_ID = "notification_verse_id";
    public static final int JOB_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "daily_verse";
    private static final int NOTIFICATION_ID = 1;
    private static final int MORNING_HOUR = 8;

    public static void schedule(Context context) {
        var calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, MORNING_HOUR);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        var delay = calendar.getTimeInMillis() - System.currentTimeMillis();
        var jobScheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(new JobInfo.Builder(JOB_ID, new ComponentName(context, DailyVerseService.class))
                .setMinimumLatency(delay)
                .setOverrideDeadline(delay + 60 * 60 * 1000)
                .setPersisted(true)
                .build());
    }

    public static void cancel(Context context) {
        ((JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(JOB_ID);
    }

    @Override
    public boolean onStartJob(@SuppressWarnings("null") JobParameters params) {
        new Thread(() -> {
            showDailyVerseNotification();
            schedule(this);
            jobFinished(params, false);
        }).start();
        return true;
    }

    @Override
    public boolean onStopJob(@SuppressWarnings("null") JobParameters params) {
        return false;
    }

    @SuppressWarnings("deprecation")
    private void showDailyVerseNotification() {
        var settings = new Settings(this);
        var biblePath = settings.getOpenBible();
        var bibleService = BibleService.getInstance();
        bibleService.installBiblesFromAssets(this, true);

        // Use day-of-year as seed so the verse is consistent throughout the day
        var calendar = Calendar.getInstance();
        var random = new Random((long)calendar.get(Calendar.YEAR) * 1000 + calendar.get(Calendar.DAY_OF_YEAR));

        try {
            var bible = bibleService.readBible(this, biblePath, true);
            var allBooks = new ArrayList<Book>();
            for (var testament : bible.testaments()) allBooks.addAll(testament.books());
            var randomBook = allBooks.get(random.nextInt(allBooks.size()));
            var randomChapter = bibleService.readChapter(
                this, biblePath, randomBook.key(), random.nextInt(randomBook.chapters().size()) + 1);
            if (randomChapter == null)
                return;

            var realVerses = new ArrayList<Verse>();
            for (var verse : randomChapter.verses()) {
                if (!verse.isSubtitle())
                    realVerses.add(verse);
            }
            if (realVerses.isEmpty())
                return;

            var randomVerse = realVerses.get(random.nextInt(realVerses.size()));
            var title = randomBook.name() + " " + randomChapter.number() + ":" + randomVerse.number();
            var text = randomVerse.text();

            var intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            if (intent == null)
                return;
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(NOTIFICATION_BOOK_KEY, randomBook.key());
            intent.putExtra(NOTIFICATION_CHAPTER_NUMBER, randomChapter.number());
            intent.putExtra(NOTIFICATION_VERSE_ID, randomVerse.id());
            var pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            var notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_DEFAULT));
                notificationManager.notify(NOTIFICATION_ID,
                    new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_book_cross)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setStyle(new Notification.BigTextStyle().bigText(text))
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build());
            } else {
                notificationManager.notify(NOTIFICATION_ID,
                    new Notification.Builder(this)
                        .setSmallIcon(R.drawable.ic_book_cross)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setStyle(new Notification.BigTextStyle().bigText(text))
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build());
            }
        } catch (Exception exception) {
            Log.e(getPackageName(), "Can't show daily verse notification", exception);
        }
    }
}
