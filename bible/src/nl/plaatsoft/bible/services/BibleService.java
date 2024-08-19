/*
 * Copyright (c) 2024 Bastiaan van der Plaat
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
import java.util.zip.GZIPInputStream;

import nl.plaatsoft.bible.models.Chapter;
import nl.plaatsoft.bible.models.Book;
import nl.plaatsoft.bible.models.Bible;
import nl.plaatsoft.bible.models.Testament;
import nl.plaatsoft.bible.models.Verse;

public class BibleService {
    private static BibleService instance;

    private BibleService() {
    }

    public static BibleService getInstance() {
        if (instance == null)
            instance = new BibleService();
        return instance;
    }

    public void installBiblesFromAssets(Context context) {
        // Copy and unzip .bible files from assets dir to app data dir
        try {
            var biblesDir = new File(context.getFilesDir(), "bibles");
            if (!biblesDir.exists())
                biblesDir.mkdirs();

            for (var filename : context.getAssets().list("bibles")) {
                if (!filename.endsWith(".bible"))
                    continue;

                var file = new File(context.getFilesDir(), "bibles/" + filename);
                if (file.exists())
                    continue;
                try (var gzipInputStream = new GZIPInputStream(context.getAssets().open("bibles/" + filename));
                        var fileOutputStream = new FileOutputStream(file)) {
                    var buffer = new byte[1024];
                    int length;
                    while ((length = gzipInputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, length);
                    }
                } catch (Exception exception) {
                    Log.e(context.getPackageName(), "Can't copy and unzip .bible file", exception);
                }
            }
        } catch (Exception exception) {
            Log.e(context.getPackageName(), "Can't index assets bibles", exception);
        }
    }

    public ArrayList<Bible> getInstalledBibles(Context context) {
        var bibles = new ArrayList<Bible>();
        var biblesDir = new File(context.getFilesDir(), "bibles");
        for (var file : biblesDir.listFiles()) {
            try {
                bibles.add(readBible(context, "bibles/" + file.getName(), false));
            } catch (Exception exception) {
                Log.e(context.getPackageName(), "Can't read .bible file", exception);
            }
        }
        Collections.sort(bibles, (a, b) -> a.name().compareTo(b.name()));
        Collections.sort(bibles, (a, b) -> a.language().compareTo(b.language()));
        return bibles;
    }

    public Bible readBible(Context context, String path, boolean readIndex) {
        try (var database = SQLiteDatabase.openDatabase(context.getFilesDir() + "/" + path, null,
                SQLiteDatabase.OPEN_READONLY)) {
            // Read metadata
            var metadata = new HashMap<String, String>();
            try (var cursor = database.rawQuery("SELECT key, value FROM metadata", null)) {
                while (cursor.moveToNext())
                    metadata.put(cursor.getString(cursor.getColumnIndex("key")),
                            cursor.getString(cursor.getColumnIndex("value")));
            }
            var name = metadata.get("name");
            var abbreviation = metadata.get("abbreviation");
            var language = metadata.get("language");
            var copyright = metadata.get("copyright");
            var releasedAt = metadata.get("released_at").split("T")[0];

            // Read index
            ArrayList<Testament> testaments = null;
            if (readIndex) {
                // Read testaments
                try (var testamentsCursor = database.rawQuery("SELECT id, key, name FROM testaments", null)) {
                    testaments = new ArrayList<Testament>();
                    while (testamentsCursor.moveToNext()) {
                        var testamentId = testamentsCursor.getInt(testamentsCursor.getColumnIndex("id"));

                        // Read books
                        var books = new ArrayList<Book>();
                        try (var booksCursor = database.rawQuery(
                                "SELECT id, key, name FROM books WHERE testament_id = ?",
                                new String[] { String.valueOf(testamentId) })) {
                            while (booksCursor.moveToNext()) {
                                var bookId = booksCursor.getInt(booksCursor.getColumnIndex("id"));

                                // Read chapters
                                var chapters = new ArrayList<Chapter>();
                                try (var chaptersCursor = database.rawQuery(
                                        "SELECT id, number FROM chapters WHERE book_id = ?",
                                        new String[] { String.valueOf(bookId) })) {
                                    while (chaptersCursor.moveToNext()) {
                                        chapters.add(new Chapter(
                                                chaptersCursor.getInt(chaptersCursor.getColumnIndex("id")),
                                                chaptersCursor.getInt(chaptersCursor.getColumnIndex("number")),
                                                null));
                                    }
                                }

                                books.add(new Book(
                                        bookId,
                                        booksCursor.getString(booksCursor.getColumnIndex("key")),
                                        booksCursor.getString(booksCursor.getColumnIndex("name")),
                                        chapters));
                            }
                        }

                        testaments.add(new Testament(
                                testamentId,
                                testamentsCursor.getString(testamentsCursor.getColumnIndex("key")),
                                testamentsCursor.getString(testamentsCursor.getColumnIndex("name")),
                                books));
                    }
                }
            }
            return new Bible(path, name, abbreviation, language, copyright, releasedAt, testaments);
        }
    }

    public Chapter readChapter(Context context, String path, String bookKey, int chapterNumber) {
        var database = SQLiteDatabase.openDatabase(context.getFilesDir() + "/" + path, null,
                SQLiteDatabase.OPEN_READONLY);
        var chapterCursor = database.rawQuery(
                "SELECT id, number FROM chapters WHERE book_id = (SELECT id FROM books WHERE key = ?) AND number = ?",
                new String[] { bookKey, String.valueOf(chapterNumber) });
        try (database; chapterCursor) {
            if (!chapterCursor.moveToNext())
                return null;

            var chapterId = chapterCursor.getInt(chapterCursor.getColumnIndex("id"));

            // Read verses
            var verses = new ArrayList<Verse>();
            try (var versesCursor = database.rawQuery(
                    "SELECT id, number, text, is_subtitle, is_last FROM verses WHERE chapter_id = ?",
                    new String[] { String.valueOf(chapterId) })) {
                while (versesCursor.moveToNext()) {
                    verses.add(new Verse(
                            versesCursor.getInt(versesCursor.getColumnIndex("id")),
                            versesCursor.getString(versesCursor.getColumnIndex("number")),
                            versesCursor.getString(versesCursor.getColumnIndex("text")),
                            versesCursor.getInt(versesCursor.getColumnIndex("is_subtitle")) == 1,
                            versesCursor.getInt(versesCursor.getColumnIndex("is_last")) == 1));
                }
            }

            return new Chapter(chapterId, chapterCursor.getInt(chapterCursor.getColumnIndex("number")), verses);
        }
    }
}
