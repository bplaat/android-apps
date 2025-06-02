/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.services;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import javax.annotation.Nullable;

import nl.plaatsoft.bible.models.Song;
import nl.plaatsoft.bible.models.SongBundle;
import nl.plaatsoft.bible.models.SongWithText;

public class SongBundleService {
    private static @Nullable SongBundleService instance;

    private final HashMap<String, SQLiteDatabase> databaseCache = new HashMap<>();

    private SongBundleService() {
    }

    public static SongBundleService getInstance() {
        if (instance == null)
            instance = new SongBundleService();
        return Objects.requireNonNull(instance);
    }

    public void installSongBundlesFromAssets(Context context, boolean skipExisting) {
        // Copy and unzip .songbundle files from assets dir to app data dir
        try {
            var songBundlesDir = new File(context.getCacheDir(), "songbundles");
            if (!songBundlesDir.exists())
                songBundlesDir.mkdirs();
            for (var filename : context.getAssets().list("songbundles")) {
                if (!filename.endsWith(".songbundle"))
                    continue;

                var file = new File(songBundlesDir, filename);
                if (skipExisting && file.exists() && file.length() > 0)
                    continue;

                var assetsFile = context.getAssets().openFd("songbundles/" + filename);
                if (file.exists() && file.length() == assetsFile.getLength())
                    continue;
                try (var gzipInputStream = new GZIPInputStream(assetsFile.createInputStream());
                        var fileOutputStream = new FileOutputStream(file)) {
                    var buffer = new byte[1024];
                    var length = 0;
                    while ((length = gzipInputStream.read(buffer)) > 0)
                        fileOutputStream.write(buffer, 0, length);
                } catch (Exception exception) {
                    Log.e(context.getPackageName(), "Can't copy and unzip: " + file.getPath(), exception);
                }
            }
        } catch (Exception exception) {
            Log.e(context.getPackageName(), "Can't index songbundle assets", exception);
        }
    }

    public ArrayList<SongBundle> getInstalledSongBundles(Context context) {
        var songBundles = new ArrayList<SongBundle>();
        var songBundlesDir = new File(context.getCacheDir(), "songbundles");
        for (var file : songBundlesDir.listFiles()) {
            if (!file.getName().endsWith(".songbundle"))
                continue;
            try {
                songBundles.add(readSongBundle(context, "songbundles/" + file.getName()));
            } catch (Exception exception) {
                Log.e(context.getPackageName(), "Can't read: " + file.getPath(), exception);
            }
        }
        Collections.sort(songBundles, (a, b) -> a.name().compareTo(b.name()));
        Collections.sort(songBundles, (a, b) -> a.language().compareTo(b.language()));
        return songBundles;
    }

    private SQLiteDatabase getDatabase(Context context, String path) {
        if (databaseCache.containsKey(path))
            return databaseCache.get(path);
        var database = SQLiteDatabase.openDatabase(context.getCacheDir() + "/" + path, null,
                SQLiteDatabase.OPEN_READONLY);
        databaseCache.put(path, database);
        return database;
    }

    public SongBundle readSongBundle(Context context, String path) {
        var database = getDatabase(context, path);
        var metadata = new HashMap<String, String>();
        try (var cursor = database.rawQuery("SELECT key, value FROM metadata", null)) {
            while (cursor.moveToNext())
                metadata.put(cursor.getString(cursor.getColumnIndex("key")),
                        cursor.getString(cursor.getColumnIndex("value")));
        }

        var songs = new ArrayList<Song>();
        try (var cursor = database.rawQuery("SELECT id, number, title FROM songs", null)) {
            while (cursor.moveToNext())
                songs.add(new Song(cursor.getInt(cursor.getColumnIndex("id")),
                        cursor.getString(cursor.getColumnIndex("number")),
                        cursor.getString(cursor.getColumnIndex("title"))));
        }
        Collections.sort(songs, (a, b) -> a.number().compareTo(b.number()));
        Collections.sort(songs, (a, b) -> Integer.parseInt(a.number().replaceAll("^\\d+", "$0"))
                - Integer.parseInt(b.number().replaceAll("^\\d+", "$0")));

        return new SongBundle(path, metadata.get("name"), metadata.get("abbreviation"),
                metadata.get("language"), metadata.get("copyright"), metadata.get("scraped_at"), songs);
    }

    public @Nullable SongWithText readSong(Context context, String path, String songNumber) {
        var database = getDatabase(context, path);
        try (var cursor = database.rawQuery("SELECT id, number, title, text, copyright FROM songs WHERE number = ?",
                new String[] { songNumber })) {
            if (!cursor.moveToNext())
                return null;
            return new SongWithText(cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex("number")),
                    cursor.getString(cursor.getColumnIndex("title")),
                    cursor.getString(cursor.getColumnIndex("text")),
                    cursor.getString(cursor.getColumnIndex("copyright")));
        }
    }

    public ArrayList<Song> searchSongs(Context context, String path, String query, int maxResults) {
        var database = getDatabase(context, path);
        var songs = new ArrayList<Song>();
        try (var cursor = database.rawQuery(
                "SELECT id, number, title FROM songs WHERE number LIKE ? OR title LIKE ? OR text LIKE ? LIMIT ?",
                new String[] { "%" + query + "%", "%" + query + "%", "%" + query + "%", String.valueOf(maxResults) })) {
            while (cursor.moveToNext())
                songs.add(new Song(cursor.getInt(cursor.getColumnIndex("id")),
                        cursor.getString(cursor.getColumnIndex("number")),
                        cursor.getString(cursor.getColumnIndex("title"))));
        }
        return songs;
    }
}
