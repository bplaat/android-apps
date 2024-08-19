/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ScrollView;
import java.util.ArrayList;
import java.util.HashMap;

import nl.plaatsoft.bible.components.BooksFlowLayout;
import nl.plaatsoft.bible.components.ChapterView;
import nl.plaatsoft.bible.components.ChaptersGridLayout;
import nl.plaatsoft.bible.models.Bible;
import nl.plaatsoft.bible.models.Book;
import nl.plaatsoft.bible.models.Chapter;
import nl.plaatsoft.bible.services.BibleService;
import nl.plaatsoft.bible.Consts;
import nl.plaatsoft.bible.Utils;
import nl.plaatsoft.bible.R;

public class MainActivity extends BaseActivity implements PopupMenu.OnMenuItemClickListener {
    private static final int SETTINGS_REQUEST_CODE = 1;

    private TextView bibleButton;
    private TextView bookButton;
    private TextView chapterButton;
    private ChapterView chapterPage;
    private ScrollView notAvailablePage;
    private AlertDialog dialog;

    private Handler handler = new Handler(Looper.getMainLooper());
    private int oldFont = -1;
    private int oldLanguage = -1;
    private int oldTheme = -1;
    private BibleService bibleService = BibleService.getInstance();
    private ArrayList<Bible> bibles;
    private Bible openBible;
    private Book openBook;
    private Chapter openChapter;

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

        var languages = new HashMap<String, String>();
        languages.put("en", getResources().getString(R.string.settings_language_english));
        languages.put("nl", getResources().getString(R.string.settings_language_dutch));

        // Bible button
        var outValue = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        var selectableItemBackground = outValue.resourceId;
        getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
        var selectableItemBackgroundBorderless = outValue.resourceId;

        bibleButton.setOnClickListener(view -> {
            var density = getResources().getDisplayMetrics().density;
            var alertView = new LinearLayout(this);
            alertView.setOrientation(LinearLayout.VERTICAL);
            alertView.setPadding(0, (int) (16 * density), 0, (int) (16 * density));

            var previousLanguage = "";
            for (var bible : bibles) {
                if (!previousLanguage.equals(bible.language())) {
                    previousLanguage = bible.language();
                    var languageView = new TextView(this);
                    languageView.setText(languages.get(bible.language()));
                    languageView.setTextSize(16);
                    languageView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                    languageView.setTextColor(Utils.contextGetColor(this, R.color.secondary_text_color));
                    languageView.setPadding((int) (24 * density), (int) (16 * density), (int) (24 * density),
                            (int) (16 * density));
                    alertView.addView(languageView);
                }

                var bibleView = new TextView(this);
                bibleView.setText(bible.name());
                bibleView.setTextSize(16);
                bibleView.setPadding((int) (24 * density), (int) (16 * density), (int) (24 * density),
                        (int) (16 * density));
                if (bible.path().equals(openBible.path()))
                    bibleView.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
                bibleView.setBackgroundResource(selectableItemBackground);
                bibleView.setOnClickListener(view2 -> {
                    dialog.dismiss();
                    if (!bible.path().equals(openBible.path())) {
                        var settingsEditor = settings.edit();
                        settingsEditor.putString("open_bible", bible.path());
                        settingsEditor.apply();
                        openBibleFromSettings();
                    }
                });
                alertView.addView(bibleView);
            }

            dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.main_bible_alert_title_label)
                    .setView(alertView)
                    .show();
        });

        // Book button
        bookButton.setOnClickListener(view -> {
            var density = getResources().getDisplayMetrics().density;
            var alertView = new ScrollView(this);
            var rootList = new LinearLayout(this);
            rootList.setOrientation(LinearLayout.VERTICAL);
            rootList.setPadding(0, (int) (16 * density), 0, (int) (16 * density));
            alertView.addView(rootList);

            for (var testament : openBible.testaments()) {
                var testamentView = new TextView(this);
                testamentView.setText(testament.name());
                testamentView.setTextSize(16);
                testamentView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                testamentView.setTextColor(Utils.contextGetColor(this, R.color.secondary_text_color));
                testamentView.setPadding((int) (24 * density), (int) (16 * density), (int) (24 * density),
                        (int) (16 * density));
                rootList.addView(testamentView);

                var booksFlowLayout = new BooksFlowLayout(this);
                booksFlowLayout.setPadding((int) (16 * density), 0, (int) (16 * density), (int) (16 * density));
                rootList.addView(booksFlowLayout);

                for (var book : testament.books()) {
                    var bookView = new TextView(this);
                    bookView.setText(book.name());
                    bookView.setTextSize(16);
                    bookView.setPadding((int) (8 * density), (int) (8 * density), (int) (8 * density),
                            (int) (8 * density));
                    if (book.key().equals(openBook.key()))
                        bookView.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
                    bookView.setBackgroundResource(selectableItemBackground);
                    bookView.setOnClickListener(view2 -> {
                        dialog.dismiss();
                        if (!book.key().equals(openBook.key())) {
                            var settingsEditor = settings.edit();
                            settingsEditor.putString("open_book", book.key());
                            settingsEditor.putInt("open_chapter", 1);
                            settingsEditor.apply();
                            openChapterFromSettings(true);
                        }
                    });
                    booksFlowLayout.addView(bookView);
                }
            }

            dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.main_book_alert_title_label)
                    .setView(alertView)
                    .show();
        });

        // Chapter button
        chapterButton.setOnClickListener(view -> {
            var density = getResources().getDisplayMetrics().density;
            var alertView = new ScrollView(this);
            var chaptersGrid = new ChaptersGridLayout(this);
            chaptersGrid.setPadding((int) (8 * density), (int) (16 * density), (int) (8 * density),
                    (int) (16 * density));
            alertView.addView(chaptersGrid);

            for (var chapter : openBook.chapters()) {
                var chapterView = new TextView(this);
                chapterView.setText(String.valueOf(chapter.number()));
                chapterView.setTextSize(20);
                if (chapter.number() == openChapter.number())
                    chapterView.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
                chapterView.setGravity(Gravity.CENTER);
                chapterView.setBackgroundResource(selectableItemBackgroundBorderless);
                chapterView.setOnClickListener(view2 -> {
                    dialog.dismiss();
                    if (chapter.number() != openChapter.number()) {
                        var settingsEditor = settings.edit();
                        settingsEditor.putInt("open_chapter", chapter.number());
                        settingsEditor.apply();
                        openChapterFromSettings(true);
                    }
                });
                chaptersGrid.addView(chapterView);
            }

            dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.main_chapter_alert_title_label)
                    .setView(alertView)
                    .show();
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
        // Get default bible path
        var defaultBiblePath = Consts.Settings.BIBLE_DEFAULT.get("en");
        var languages = Utils.contextGetLanguages(this);
        for (var language : languages) {
            if (Consts.Settings.BIBLE_DEFAULT.containsKey(language)) {
                defaultBiblePath = Consts.Settings.BIBLE_DEFAULT.get(language);
                break;
            }
        }

        // Open bible and chapter
        openBible = bibleService.readBible(this, settings.getString("open_bible", defaultBiblePath), true);
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
        if (settings.getInt("font", Consts.Settings.FONT_SERIF) == Consts.Settings.FONT_SERIF)
            chapterPage.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        if (settings.getInt("font", Consts.Settings.FONT_SERIF) == Consts.Settings.FONT_SANS_SERIF)
            chapterPage.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        if (settings.getInt("font", Consts.Settings.FONT_SERIF) == Consts.Settings.FONT_MONOSPACE)
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
