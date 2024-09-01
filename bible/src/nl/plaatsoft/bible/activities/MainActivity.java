/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ScrollView;
import java.util.ArrayList;

import nl.plaatsoft.bible.models.Bible;
import nl.plaatsoft.bible.models.Book;
import nl.plaatsoft.bible.models.Chapter;
import nl.plaatsoft.bible.services.BibleService;
import nl.plaatsoft.bible.views.BiblesDialogBuilder;
import nl.plaatsoft.bible.views.BooksDialogBuilder;
import nl.plaatsoft.bible.views.ChapterView;
import nl.plaatsoft.bible.views.ChaptersDialogBuilder;
import nl.plaatsoft.bible.Consts;
import nl.plaatsoft.bible.R;

public class MainActivity extends BaseActivity implements PopupMenu.OnMenuItemClickListener {
    private static final int SEARCH_REQUEST_CODE = 0;
    private static final int SETTINGS_REQUEST_CODE = 1;

    private TextView bibleButton;
    private TextView bookButton;
    private TextView chapterButton;
    private ChapterView chapterPage;
    private ScrollView notAvailablePage;

    private BibleService bibleService = BibleService.getInstance();
    private ArrayList<Bible> bibles;
    private Bible openBible;
    private Book openBook;
    private Chapter openChapter;
    private AlertDialog dialog;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int oldFont = -1;
    private int oldLanguage = -1;
    private int oldTheme = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bibleButton = findViewById(R.id.main_bible_button);
        bookButton = findViewById(R.id.main_book_button);
        chapterButton = findViewById(R.id.main_chapter_button);
        chapterPage = findViewById(R.id.main_chapter_page);
        notAvailablePage = findViewById(R.id.main_not_available_page);
        useWindowInsets(chapterPage, notAvailablePage);

        // Bible button
        bibleButton.setOnClickListener(view -> {
            dialog = new BiblesDialogBuilder(this, bibles, openBible, bible -> {
                dialog.dismiss();
                if (!bible.path().equals(openBible.path())) {
                    var settingsEditor = settings.edit();
                    settingsEditor.putString("open_bible", bible.path());
                    settingsEditor.apply();
                    openBibleFromSettings();
                }
            }).show();
        });

        // Book button
        bookButton.setOnClickListener(view -> {
            dialog = new BooksDialogBuilder(this, openBible.testaments(), openBook, book -> {
                dialog.dismiss();
                if (!book.key().equals(openBook.key())) {
                    var editor = settings.edit();
                    editor.putString("open_book", book.key());
                    editor.putInt("open_chapter", 1);
                    editor.apply();
                    openChapterFromSettings(true);
                }
            }).show();
        });

        // Chapter button
        chapterButton.setOnClickListener(view -> {
            dialog = new ChaptersDialogBuilder(this, openBook.chapters(), openChapter, chapter -> {
                dialog.dismiss();
                if (chapter.number() != openChapter.number()) {
                    var editor = settings.edit();
                    editor.putInt("open_chapter", chapter.number());
                    editor.apply();
                    openChapterFromSettings(true);
                }
            }).show();
        });

        // Search button
        findViewById(R.id.main_search_button).setOnClickListener(view -> {
            startActivityForResult(new Intent(this, SearchActivity.class), SEARCH_REQUEST_CODE);
        });

        // Options menu button
        findViewById(R.id.main_options_menu_button).setOnClickListener(view -> {
            var optionsMenu = new PopupMenu(this, view, Gravity.TOP | Gravity.RIGHT);
            optionsMenu.getMenuInflater().inflate(R.menu.options, optionsMenu.getMenu());
            optionsMenu.setOnMenuItemClickListener(this);
            optionsMenu.show();
        });

        // Install bibles from assets and open last opened bible
        bibleService.installBiblesFromAssets(this);
        bibles = bibleService.getInstalledBibles(this);
        openBibleFromSettings();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.menu_options_random_chapter) {
            var randomTestament = openBible.testaments().get((int) (Math.random() * openBible.testaments().size()));
            var randomBook = randomTestament.books().get((int) (Math.random() * randomTestament.books().size()));
            var settingsEditor = settings.edit();
            settingsEditor.putString("open_book", randomBook.key());
            settingsEditor.putInt("open_chapter",
                    randomBook.chapters().get((int) (Math.random() * randomBook.chapters().size())).number());
            settingsEditor.apply();
            openChapterFromSettings(true);
            return true;
        }

        if (item.getItemId() == R.id.menu_options_settings) {
            oldFont = settings.getInt("font", Consts.Settings.FONT_DEFAULT);
            oldLanguage = settings.getInt("language", Consts.Settings.LANGUAGE_DEFAULT);
            oldTheme = settings.getInt("theme", Consts.Settings.THEME_DEFAULT);
            startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_REQUEST_CODE);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // When search activity is closed check open selected book / chapter verse
        if (requestCode == SEARCH_REQUEST_CODE) {
            // if (data.hasExtra("highlight_verse")) {
            // var highlightVerse = data.getStringExtra("highlight_verse");
            // }

            // FIXME

            openChapterFromSettings(true);
            return;
        }

        // When settings activity is closed check for restarts
        if (requestCode == SETTINGS_REQUEST_CODE) {
            if (oldFont != -1) {
                if (oldFont != settings.getInt("font", Consts.Settings.FONT_DEFAULT))
                    openChapterFromSettings(false);
            }
            if (oldLanguage != -1 && oldTheme != -1) {
                if (oldLanguage != settings.getInt("language", Consts.Settings.LANGUAGE_DEFAULT) ||
                        oldTheme != settings.getInt("theme", Consts.Settings.THEME_DEFAULT)) {
                    handler.post(() -> recreate());
                }
            }
        }
    }

    private void openBibleFromSettings() {
        openBible = bibleService.readBible(this,
                settings.getString("open_bible", Consts.Settings.getBibleDefault(this)),
                true);
        bibleButton.setText(openBible.abbreviation());
        openChapterFromSettings(true);
    }

    private void openChapterFromSettings(boolean scrollToTop) {
        var bookKey = settings.getString("open_book", Consts.Settings.BIBLE_BOOK_DEFAULT);
        openChapter = bibleService.readChapter(this, openBible.path(), bookKey,
                settings.getInt("open_chapter", Consts.Settings.BIBLE_CHAPTER_DEFAULT));
        if (openChapter == null) {
            openPage(notAvailablePage);
            return;
        }
        openPage(chapterPage);
        chapterButton.setText(String.valueOf(openChapter.number()));

        // Get book
        openBook = null;
        for (var testament : openBible.testaments()) {
            for (var book : testament.books()) {
                if (book.key().equals(bookKey)) {
                    openBook = book;
                    break;
                }
            }
        }
        bookButton.setText(openBook.name());

        // Update chapter view
        if (settings.getInt("font", Consts.Settings.FONT_DEFAULT) == Consts.Settings.FONT_SERIF)
            chapterPage.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        if (settings.getInt("font", Consts.Settings.FONT_DEFAULT) == Consts.Settings.FONT_SANS_SERIF)
            chapterPage.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        if (settings.getInt("font", Consts.Settings.FONT_DEFAULT) == Consts.Settings.FONT_MONOSPACE)
            chapterPage.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
        if (scrollToTop)
            chapterPage.scrollTo(0, 0);
        chapterPage.openChapter(openChapter);
    }

    private void openPage(View page) {
        chapterPage.setVisibility(page.equals(chapterPage) ? View.VISIBLE : View.GONE);
        notAvailablePage.setVisibility(page.equals(notAvailablePage) ? View.VISIBLE : View.GONE);
    }
}
