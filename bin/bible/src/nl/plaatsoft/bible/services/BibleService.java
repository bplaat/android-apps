/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.services;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import nl.plaatsoft.bible.models.Bible;
import nl.plaatsoft.bible.models.Book;
import nl.plaatsoft.bible.models.Chapter;
import nl.plaatsoft.bible.models.ChapterWithVerses;
import nl.plaatsoft.bible.models.SearchVerse;
import nl.plaatsoft.bible.models.Testament;
import nl.plaatsoft.bible.models.Verse;

import org.jspecify.annotations.Nullable;

public class BibleService {
    private static @Nullable BibleService instance;

    private final HashMap<String, SQLiteDatabase> databaseCache = new HashMap<>();

    private BibleService() {
    }

    public static BibleService getInstance() {
        if (instance == null)
            instance = new BibleService();
        return Objects.requireNonNull(instance);
    }

    public void installBiblesFromAssets(Context context, boolean skipExisting) {
        // Copy and unzip .bible files from assets dir to app data dir
        try {
            var biblesDir = new File(context.getCacheDir(), "bibles");
            if (!biblesDir.exists())
                biblesDir.mkdirs();
            for (var filename : context.getAssets().list("bibles")) {
                if (!filename.endsWith(".bible"))
                    continue;

                var file = new File(biblesDir, filename);
                if (skipExisting && file.exists() && file.length() > 0)
                    continue;

                var assetsFile = context.getAssets().openFd("bibles/" + filename);
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
            Log.e(context.getPackageName(), "Can't index bible assets", exception);
        }
    }

    public ArrayList<Bible> getInstalledBibles(Context context) {
        var bibles = new ArrayList<Bible>();
        var biblesDir = new File(context.getCacheDir(), "bibles");
        for (var file : biblesDir.listFiles()) {
            if (!file.getName().endsWith(".bible"))
                continue;
            try {
                bibles.add(readBible(context, "bibles/" + file.getName(), false));
            } catch (Exception exception) {
                Log.e(context.getPackageName(), "Can't read: " + file.getPath(), exception);
            }
        }
        Collections.sort(bibles, (a, b) -> a.name().compareTo(b.name()));
        var bibleLanguageOrder = Arrays.asList("en", "nl", "de");
        Collections.sort(bibles, (a, b) -> Integer.compare(bibleLanguageOrder.indexOf(a.language()),
                bibleLanguageOrder.indexOf(b.language())));
        return bibles;
    }

    private SQLiteDatabase getDatabase(Context context, String path) {
        if (databaseCache.containsKey(path))
            return databaseCache.get(path);
        var database = SQLiteDatabase.openDatabase(context.getCacheDir() + "/" + path, null,
                SQLiteDatabase.OPEN_READONLY);
        databaseCache.put(path, database);
        return database;
    }

    public Bible readBible(Context context, String path, boolean readIndex) {
        var database = getDatabase(context, path);

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
        var releasedAt = metadata.get("released_at");
        var scrapedAt = metadata.get("scraped_at");
        if (scrapedAt == null) // Backwards compatibility
            scrapedAt = "?";

        // Read index
        var testaments = new ArrayList<Testament>();
        if (readIndex) {
            // Read testaments
            try (var testamentsCursor = database.rawQuery("SELECT id, key, name FROM testaments", null)) {
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
                                            chaptersCursor.getInt(chaptersCursor.getColumnIndex("number"))));
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
        return new Bible(path, name, abbreviation, language, copyright, releasedAt, scrapedAt,
                testaments);
    }

    public @Nullable ChapterWithVerses readChapter(Context context, String path, String bookKey, int chapterNumber) {
        var database = getDatabase(context, path);
        try (var chapterCursor = database.rawQuery(
                "SELECT id, number FROM chapters WHERE book_id = (SELECT id FROM books WHERE key = ?) AND number = ?",
                new String[] { bookKey, String.valueOf(chapterNumber) })) {
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

            return new ChapterWithVerses(chapterId, chapterCursor.getInt(chapterCursor.getColumnIndex("number")),
                    verses);
        }
    }

    public ArrayList<SearchVerse> searchVerses(Context context, String path, String query, int maxResults) {
        var database = getDatabase(context, path);
        var results = new ArrayList<SearchVerse>();
        try (var cursor = database.rawQuery(
                "SELECT books.id AS book_id, books.key AS book_key, books.name AS book_name, " +
                        "chapters.id AS chapter_id, chapters.number AS chapter_number, " +
                        "verses.id AS verse_id, verses.number AS verse_number, verses.text AS verse_text, " +
                        "verses.is_subtitle AS verse_is_subtitle, verses.is_last AS verse_is_last " +
                        "FROM verses " +
                        "JOIN chapters ON verses.chapter_id = chapters.id " +
                        "JOIN books ON chapters.book_id = books.id " +
                        "WHERE verses.text LIKE ? LIMIT ?",
                new String[] { "%" + query + "%", String.valueOf(maxResults) })) {
            while (cursor.moveToNext()) {
                results.add(new SearchVerse(
                        new Verse(
                                cursor.getInt(cursor.getColumnIndex("verse_id")),
                                cursor.getString(cursor.getColumnIndex("verse_number")),
                                cursor.getString(cursor.getColumnIndex("verse_text")),
                                cursor.getInt(cursor.getColumnIndex("verse_is_subtitle")) == 1,
                                cursor.getInt(cursor.getColumnIndex("verse_is_last")) == 1),
                        new Book(
                                cursor.getInt(cursor.getColumnIndex("book_id")),
                                cursor.getString(cursor.getColumnIndex("book_key")),
                                cursor.getString(cursor.getColumnIndex("book_name")),
                                new ArrayList<>()),
                        new Chapter(
                                cursor.getInt(cursor.getColumnIndex("chapter_id")),
                                cursor.getInt(cursor.getColumnIndex("chapter_number")))));
            }
        }
        return results;
    }
}
