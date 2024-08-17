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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ScrollView;
import java.util.ArrayList;

import nl.plaatsoft.bible.models.Bible;
import nl.plaatsoft.bible.models.Book;
import nl.plaatsoft.bible.models.Chapter;
import nl.plaatsoft.bible.services.BibleService;
import nl.plaatsoft.bible.Consts;
import nl.plaatsoft.bible.Utils;
import nl.plaatsoft.bible.R;

public class MainActivity extends BaseActivity implements PopupMenu.OnMenuItemClickListener {
    private static final int SEARCH_REQUEST_CODE = 1;
    private static final int SETTINGS_REQUEST_CODE = 2;

    private TextView bibleButton;
    private TextView bookButton;
    private TextView chapterButton;
    private ScrollView chapterScroll;
    private LinearLayout chapterContents;
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
        chapterScroll = findViewById(R.id.main_chapter_scroll);
        chapterContents = findViewById(R.id.main_chapter_contents);
        useWindowInsets(chapterScroll);

        // Bible button
        bibleButton.setOnClickListener(view -> {
            var density = getResources().getDisplayMetrics().density;
            var alertView = new LinearLayout(this);
            alertView.setOrientation(LinearLayout.VERTICAL);
            alertView.setPadding(0, (int)(16 * density), 0, (int)(16 * density));

            var previousLanguage = "";
            for (var bile : bibles) {
                if (!previousLanguage.equals(bile.language())) {
                    previousLanguage = bile.language();
                    var languageView = new TextView(this);
                    languageView.setText(bile.language());
                    languageView.setTextSize(16);
                    languageView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                    languageView.setPadding((int)(24 * density), (int)(16 * density), (int)(24 * density), (int)(16 * density));
                    alertView.addView(languageView);
                }

                var bibleView = new TextView(this);
                bibleView.setText(bile.name() + " (" + bile.abbreviation() + ")");
                bibleView.setTextSize(16);
                bibleView.setPadding((int)(24 * density), (int)(16 * density), (int)(24 * density), (int)(16 * density));
                var outValue = new TypedValue();
                getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                bibleView.setBackgroundResource(outValue.resourceId);
                bibleView.setOnClickListener(view2 -> {
                    dialog.dismiss();
                    if (!bile.path().equals(openBible.path())) {
                        var settingsEditor = settings.edit();
                        settingsEditor.putString("open_bible", bile.path());
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
            var alertList = new LinearLayout(this);
            alertList.setOrientation(LinearLayout.VERTICAL);
            alertList.setPadding(0, (int)(16 * density), 0, 0);
            alertView.addView(alertList);

            for (var testament : openBible.testaments()) {
                var testamentView = new TextView(this);
                testamentView.setText(testament.name());
                testamentView.setTextSize(18);
                testamentView.setTextColor(Utils.contextGetColor(this, R.color.secondary_text_color));
                testamentView.setPadding((int)(16 * density), (int)(8 * density), (int)(16 * density), (int)(8 * density));
                alertList.addView(testamentView);

                for (var book : testament.books()) {
                    var bookView = new TextView(this);
                    bookView.setText(book.name());
                    bookView.setTextSize(18);
                    bookView.setPadding((int)(16 * density), (int)(16 * density), (int)(16 * density), (int)(16 * density));
                    var outValue = new TypedValue();
                    getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                    bookView.setBackgroundResource(outValue.resourceId);
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
                    alertList.addView(bookView);
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
            var alertView = new GridLayout(this);
            alertView.setColumnCount(6);
            alertView.setPadding(0, (int)(16 * density), 0, 0);

            for (var chapter : openBook.chapters()) {
                var chapterView = new TextView(this);
                chapterView.setWidth((int)(48 * density));
                chapterView.setHeight((int)(48 * density));
                chapterView.setText(String.valueOf(chapter.number()));
                chapterView.setTextSize(20);
                chapterView.setGravity(Gravity.CENTER);
                var outValue = new TypedValue();
                getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
                chapterView.setBackgroundResource(outValue.resourceId);
                chapterView.setOnClickListener(view2 -> {
                    dialog.dismiss();
                    if (chapter.number() != openChapter.number()) {
                        var settingsEditor = settings.edit();
                        settingsEditor.putInt("open_chapter", chapter.number());
                        settingsEditor.apply();
                        openChapterFromSettings(true);
                    }
                });
                alertView.addView(chapterView);
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
            // Get random testament
            var randomTestament = openBible.testaments().get((int)(Math.random() * openBible.testaments().size()));
            var randomBook = randomTestament.books().get((int)(Math.random() * randomTestament.books().size()));
            var settingsEditor = settings.edit();
            settingsEditor.putString("open_book", randomBook.key());
            settingsEditor.putInt("open_chapter", randomBook.chapters().get((int)(Math.random() * randomBook.chapters().size())).number());
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
            if (oldFont != -1 && oldLanguage != -1 && oldTheme != -1) {
                if (oldFont != settings.getInt("font", Consts.Settings.FONT_DEFAULT))
                    openChapterFromSettings(false);

                if (
                    oldLanguage != settings.getInt("language", Consts.Settings.LANGUAGE_DEFAULT) ||
                    oldTheme != settings.getInt("theme", Consts.Settings.THEME_DEFAULT)
                ) {
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
        openChapter = bibleService.readChapter(this, openBible.path(), bookKey, settings.getInt("open_chapter", Consts.Settings.BIBLE_CHAPTER_DEFAULT));
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

        // Create verse views
        var typefaceBold = Typeface.create(Typeface.SERIF, Typeface.BOLD);
        var typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL);
        if (settings.getInt("font", Consts.Settings.FONT_SERIF) == Consts.Settings.FONT_SANS_SERIF) {
            typefaceBold = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
        }
        if (settings.getInt("font", Consts.Settings.FONT_SERIF) == Consts.Settings.FONT_MONOSPACE) {
            typefaceBold = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
        }

        if (scrollToTop)
            chapterScroll.scrollTo(0, 0);

        chapterContents.removeAllViews();
        var ssb = new SpannableStringBuilder();
        for (var verse : openChapter.verses()) {
            if (verse.isSubtitle()) {
                addVerseBlock(new SpannableString(verse.text()), typefaceBold, true);
                continue;
            }
            if (verse.isNewParagraph() && ssb.length() > 0) {
                addVerseBlock(ssb, typeface, false);
                ssb = new SpannableStringBuilder();
            }

            if (ssb.length() > 0)
                ssb.append(" ");

            var verseNumberSpannable = new SpannableString(verse.number());
            verseNumberSpannable.setSpan(new ForegroundColorSpan(Utils.contextGetColor(this, R.color.secondary_text_color)), 0, verse.number().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            verseNumberSpannable.setSpan(new RelativeSizeSpan(0.75f), 0, verse.number().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.append(verseNumberSpannable);
            ssb.append(" ");
            ssb.append(verse.text());
        }
        if (ssb.length() > 0)
            addVerseBlock(ssb, typeface, false);
    }

    private void addVerseBlock(Spannable spannable, Typeface typeface, boolean isSubtitle) {
        var verseBlock = new TextView(this);
        verseBlock.setTypeface(typeface);
        verseBlock.setTextSize(18);
        if (!isSubtitle)
            verseBlock.setLineSpacing(0, 1.2f);
        var layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        var density = getResources().getDisplayMetrics().density;
        layoutParams.setMargins(0, (int)(8 * density), 0, (int)(8 * density));
        verseBlock.setLayoutParams(layoutParams);
        verseBlock.setText(spannable);
        chapterContents.addView(verseBlock);
    }
}
