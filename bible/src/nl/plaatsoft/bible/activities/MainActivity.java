/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ScrollView;
import java.util.ArrayList;

import nl.plaatsoft.bible.models.Bible;
import nl.plaatsoft.bible.models.Book;
import nl.plaatsoft.bible.models.Chapter;
import nl.plaatsoft.bible.models.Song;
import nl.plaatsoft.bible.models.SongBundle;
import nl.plaatsoft.bible.models.Verse;
import nl.plaatsoft.bible.services.BibleService;
import nl.plaatsoft.bible.services.SongBundleService;
import nl.plaatsoft.bible.views.BooksDialogBuilder;
import nl.plaatsoft.bible.views.ChapterView;
import nl.plaatsoft.bible.views.ChaptersDialogBuilder;
import nl.plaatsoft.bible.views.DrawerLayout;
import nl.plaatsoft.bible.views.SongView;
import nl.plaatsoft.bible.views.SongsDialogBuilder;
import nl.plaatsoft.bible.Consts;
import nl.plaatsoft.bible.R;

public class MainActivity extends BaseActivity implements PopupMenu.OnMenuItemClickListener {
    private static final int SEARCH_REQUEST_CODE = 0;
    private static final int SETTINGS_REQUEST_CODE = 1;

    private DrawerLayout drawer;
    private LinearLayout drawerBibles;
    private LinearLayout drawerSongBundles;
    private TextView nameButton;
    private TextView indexButton;
    private ChapterView chapterPage;
    private ScrollView chapterNotAvailablePage;
    private SongView songPage;

    private String app_version;
    private BibleService bibleService = BibleService.getInstance();
    private SongBundleService songBundleService = SongBundleService.getInstance();
    private ArrayList<Bible> bibles;
    private ArrayList<SongBundle> songBundles;

    private int openType;
    private Bible openBible;
    private Book openBook;
    private Chapter openChapter;
    private SongBundle openSongBundle;
    private Song openSong;

    private AlertDialog dialog;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int oldFont = -1;
    private int oldLanguage = -1;
    private int oldTheme = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawer = findViewById(R.id.main_drawer);
        drawerBibles = findViewById(R.id.main_drawer_bibles);
        drawerSongBundles = findViewById(R.id.main_drawer_song_bundles);
        nameButton = findViewById(R.id.main_name_button);
        indexButton = findViewById(R.id.main_index_button);
        chapterPage = findViewById(R.id.main_chapter_page);
        chapterNotAvailablePage = findViewById(R.id.main_chapter_not_available_page);
        songPage = findViewById(R.id.main_song_page);
        useWindowInsets(chapterPage, chapterNotAvailablePage, songPage);

        // Menu button
        findViewById(R.id.main_menu_button).setOnClickListener(view -> {
            populateDrawer();
            drawer.open();
            updateBackListener();
        });

        // Name button
        nameButton.setOnClickListener(view -> {
            if (openType == Consts.Settings.OPEN_TYPE_BIBLE) {
                dialog = new BooksDialogBuilder(this, openBible.testaments(), openBook.key(), book -> {
                    dialog.dismiss();
                    if (!book.key().equals(openBook.key())) {
                        var editor = settings.edit();
                        editor.putString("open_book", book.key());
                        editor.putInt("open_chapter", 1);
                        editor.apply();
                        openChapterFromSettings(-1);
                    }
                }).show();
            }
        });

        // Index button
        indexButton.setOnClickListener(view -> {
            if (openType == Consts.Settings.OPEN_TYPE_BIBLE) {
                if (openChapter == null)
                    return;
                dialog = new ChaptersDialogBuilder(this, openBook.chapters(), openChapter.number(), chapter -> {
                    dialog.dismiss();
                    if (chapter.number() != openChapter.number()) {
                        var editor = settings.edit();
                        editor.putInt("open_chapter", chapter.number());
                        editor.apply();
                        openChapterFromSettings(-1);
                    }
                }).show();
            }

            if (openType == Consts.Settings.OPEN_TYPE_SONG_BUNDLE) {
                dialog = new SongsDialogBuilder(this, openSongBundle.songs(), openSong.number(), song -> {
                    dialog.dismiss();
                    if (!song.number().equals(openSong.number())) {
                        var editor = settings.edit();
                        editor.putString("open_song_number", song.number());
                        editor.apply();
                        openSongFromSettings();
                    }
                }).show();
            }
        });

        // Search button
        findViewById(R.id.main_search_button).setOnClickListener(view -> {
            startActivityForResult(new Intent(this, SearchActivity.class), SEARCH_REQUEST_CODE);
        });

        // Options menu button
        findViewById(R.id.main_options_menu_button).setOnClickListener(view -> {
            var optionsMenu = new PopupMenu(this, view, Gravity.TOP | Gravity.RIGHT);
            if (openType == Consts.Settings.OPEN_TYPE_BIBLE)
                optionsMenu.getMenuInflater().inflate(R.menu.options_bible, optionsMenu.getMenu());
            if (openType == Consts.Settings.OPEN_TYPE_SONG_BUNDLE)
                optionsMenu.getMenuInflater().inflate(R.menu.options_song_bundle, optionsMenu.getMenu());
            optionsMenu.setOnMenuItemClickListener(this);
            optionsMenu.show();
        });

        // Drawer
        drawer.setOnCloseListener(() -> updateBackListener());

        // Chapter view
        updateFonts(false);
        chapterPage.setOnPreviousListener(() -> {
            var editor = settings.edit();
            if (openChapter.number() == 1) {
                // Find previous book
                var allBooks = new ArrayList<Book>();
                for (var testament : openBible.testaments())
                    allBooks.addAll(testament.books());
                for (var i = 0; i < allBooks.size(); i++) {
                    if (allBooks.get(i).key().equals(openBook.key())) {
                        var previousBook = allBooks.get(i == 0 ? allBooks.size() - 1 : i - 1);
                        editor.putString("open_book", previousBook.key());
                        editor.putInt("open_chapter", previousBook.chapters().size());
                        break;
                    }
                }
            } else {
                editor.putInt("open_chapter", openChapter.number() - 1);
            }
            editor.apply();
            openChapterFromSettings(-1);
        });
        chapterPage.setOnNextListener(() -> {
            var editor = settings.edit();
            if (openChapter.number() == openBook.chapters().size()) {
                // Find next book
                var allBooks = new ArrayList<Book>();
                for (var testament : openBible.testaments())
                    allBooks.addAll(testament.books());
                for (var i = 0; i < allBooks.size(); i++) {
                    if (allBooks.get(i).key().equals(openBook.key())) {
                        var nextBook = allBooks.get(i == allBooks.size() - 1 ? 0 : i + 1);
                        editor.putString("open_book", nextBook.key());
                        editor.putInt("open_chapter", 1);
                        break;
                    }
                }
            } else {
                editor.putInt("open_chapter", openChapter.number() + 1);
            }
            editor.apply();
            openChapterFromSettings(-1);
        });

        // Song view
        songPage.setOnPreviousListener(() -> {
            var songs = openSongBundle.songs();

            // Get index of openSong
            var openSongIndex = -1;
            for (int i = 0; i < songs.size(); i++) {
                if (songs.get(i).number().equals(openSong.number())) {
                    openSongIndex = i;
                    break;
                }
            }

            var editor = settings.edit();
            editor.putString("open_song_number",
                    songs.get(openSongIndex == 0 ? songs.size() - 1 : openSongIndex - 1).number());
            editor.apply();
            openSongFromSettings();
        });
        songPage.setOnNextListener(() -> {
            var songs = openSongBundle.songs();

            // Get index of openSong
            var openSongIndex = -1;
            for (int i = 0; i < songs.size(); i++) {
                if (songs.get(i).number().equals(openSong.number())) {
                    openSongIndex = i;
                    break;
                }
            }

            var editor = settings.edit();
            editor.putString("open_song_number",
                    songs.get(openSongIndex == songs.size() - 1 ? 0 : openSongIndex + 1).number());
            editor.apply();
            openSongFromSettings();
        });

        // Install bibles from assets and open last opened bible
        try {
            app_version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception exception) {
            Log.e(getPackageName(), "Can't get app version", exception);
        }
        Runnable installAssetsAndOpen = () -> {
            if (!settings.getString("installed_assets_version", "").equals(app_version)) {
                var editor = settings.edit();
                editor.putString("installed_assets_version", app_version);
                editor.apply();

                bibleService.installBiblesFromAssets(this);
                songBundleService.installSongBundlesFromAssets(this);
            }

            bibles = bibleService.getInstalledBibles(this);
            songBundles = songBundleService.getInstalledSongBundles(this);
            handler.post(() -> openFromSettings());
        };
        installAssetsAndOpen.run();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.menu_options_random_verse) {
            var randomTestament = openBible.testaments().get((int) (Math.random() * openBible.testaments().size()));
            var randomBook = randomTestament.books().get((int) (Math.random() * randomTestament.books().size()));
            var randomChapter = bibleService.readChapter(this, openBible.path(), randomBook.key(),
                    (int) (Math.random() * randomBook.chapters().size()) + 1);

            var realVerses = new ArrayList<Verse>();
            for (var verse : randomChapter.verses()) {
                if (!verse.isSubtitle())
                    realVerses.add(verse);
            }
            var randomVerse = realVerses.get((int) (Math.random() * realVerses.size()));

            var settingsEditor = settings.edit();
            settingsEditor.putString("open_book", randomBook.key());
            settingsEditor.putInt("open_chapter", randomChapter.number());
            settingsEditor.apply();
            openChapterFromSettings(randomVerse.id());
            return true;
        }

        if (item.getItemId() == R.id.menu_options_random_song) {
            var randomSong = openSongBundle.songs().get((int) (Math.random() * openSongBundle.songs().size()));
            var settingsEditor = settings.edit();
            settingsEditor.putString("open_song_number", randomSong.number());
            settingsEditor.apply();
            openSongFromSettings();
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
            if (resultCode == RESULT_OK) {
                if (openType == Consts.Settings.OPEN_TYPE_BIBLE)
                    openChapterFromSettings(data.getIntExtra("highlight_verse", -1));
                if (openType == Consts.Settings.OPEN_TYPE_SONG_BUNDLE)
                    openSongFromSettings();
            }
            return;
        }

        // When settings activity is closed check for restarts
        if (requestCode == SETTINGS_REQUEST_CODE) {
            if (oldFont != -1) {
                if (oldFont != settings.getInt("font", Consts.Settings.FONT_DEFAULT))
                    updateFonts(true);
            }
            if (oldLanguage != -1 && oldTheme != -1) {
                if (oldLanguage != settings.getInt("language", Consts.Settings.LANGUAGE_DEFAULT) ||
                        oldTheme != settings.getInt("theme", Consts.Settings.THEME_DEFAULT)) {
                    handler.post(() -> recreate());
                }
            }
        }
    }

    @Override
    protected boolean shouldBackOverride() {
        return drawer.isOpen();
    }

    @Override
    protected void onBack() {
        if (drawer.isOpen()) {
            drawer.close();
        }
    }

    private void openFromSettings() {
        openType = settings.getInt("open_type", Consts.Settings.OPEN_TYPE_DEFAULT);

        if (openType == Consts.Settings.OPEN_TYPE_BIBLE) {
            openBible = bibleService.readBible(this,
                    settings.getString("open_bible", Consts.Settings.getBibleDefault(this)),
                    true);
            nameButton.setClickable(true);
            openChapterFromSettings(-1);
        }

        if (openType == Consts.Settings.OPEN_TYPE_SONG_BUNDLE) {
            openSongBundle = songBundleService.readSongBundle(this,
                    settings.getString("open_song_bundle", Consts.Settings.SONG_BUNDLE_DEFAULT));
            nameButton.setClickable(false);
            nameButton.setText(openSongBundle.name());
            openSongFromSettings();
        }
    }

    private void openChapterFromSettings(int highlightVerseId) {
        var openBookKey = settings.getString("open_book", Consts.Settings.BIBLE_BOOK_DEFAULT);
        var openChapterNumber = settings.getInt("open_chapter", Consts.Settings.BIBLE_CHAPTER_DEFAULT);
        openChapter = bibleService.readChapter(this, openBible.path(), openBookKey, openChapterNumber);
        indexButton.setText(String.valueOf(openChapterNumber));

        // Get book
        openBook = null;
        for (var testament : openBible.testaments()) {
            for (var book : testament.books()) {
                if (book.key().equals(openBookKey)) {
                    openBook = book;
                    break;
                }
            }
        }
        nameButton.setText(openBook != null ? openBook.name() : openBookKey + "?");

        // Show not available page if chapter doesn't exist
        if (openChapter == null) {
            openPage(chapterNotAvailablePage);
            return;
        }

        // Update chapter view
        chapterPage.openChapter(openChapter, highlightVerseId);
        openPage(chapterPage);
    }

    private void openSongFromSettings() {
        var openSongNumber = settings.getString("open_song_number", Consts.Settings.SONG_BUNDLE_NUMBER_DEFAULT);
        openSong = songBundleService.readSong(this, openSongBundle.path(), openSongNumber);
        indexButton.setText(openSongNumber);
        songPage.openSong(openSong);
        openPage(songPage);
    }

    private void openPage(View page) {
        chapterPage.setVisibility(page.equals(chapterPage) ? View.VISIBLE : View.GONE);
        chapterNotAvailablePage.setVisibility(page.equals(chapterNotAvailablePage) ? View.VISIBLE : View.GONE);
        songPage.setVisibility(page.equals(songPage) ? View.VISIBLE : View.GONE);
    }

    private void updateFonts(boolean reopen) {
        // Update chapter view
        chapterPage.setTypeface(Consts.Settings.getFontTypeface(settings.getInt("font", Consts.Settings.FONT_DEFAULT)));
        if (reopen && openType == Consts.Settings.OPEN_TYPE_BIBLE)
            openChapterFromSettings(-1);

        // Update song view
        songPage.setTypeface(Consts.Settings.getFontTypeface(settings.getInt("font", Consts.Settings.FONT_DEFAULT)));
        if (reopen && openType == Consts.Settings.OPEN_TYPE_SONG_BUNDLE)
            openSongFromSettings();
    }

    private void populateDrawer() {
        var density = getResources().getDisplayMetrics().density;

        // Add bible list buttons
        drawerBibles.removeAllViews();
        for (var bible : bibles) {
            var listItemButton = new LinearLayout(this, null, 0, R.style.ListItemButton);
            listItemButton
                    .setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            (int) (56 * density)));
            if (openType == Consts.Settings.OPEN_TYPE_BIBLE && bible.path().equals(openBible.path()))
                listItemButton.setBackgroundResource(R.drawable.list_item_button_selected);
            listItemButton.setOnClickListener(view -> {
                var settingsEditor = settings.edit();
                settingsEditor.putInt("open_type", Consts.Settings.OPEN_TYPE_BIBLE);
                settingsEditor.putString("open_bible", bible.path());
                settingsEditor.apply();
                openFromSettings();
                drawer.close();
            });
            drawerBibles.addView(listItemButton);

            var listItemButtonLabel = new TextView(this, null, 0, R.style.ListItemButtonLabel);
            listItemButtonLabel
                    .setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1));
            listItemButtonLabel.setText(bible.name());
            listItemButton.addView(listItemButtonLabel);

            var listeItemButtonMeta = new TextView(this, null, 0, R.style.ListItemButtonMeta);
            if (bible.language().equals("en"))
                listeItemButtonMeta.setText(R.string.settings_language_english);
            if (bible.language().equals("nl"))
                listeItemButtonMeta.setText(R.string.settings_language_dutch);
            listItemButton.addView(listeItemButtonMeta);
        }

        // Add song bundle list buttons
        drawerSongBundles.removeAllViews();
        for (var songBundle : songBundles) {
            var listItemButton = new LinearLayout(this, null, 0, R.style.ListItemButton);
            listItemButton
                    .setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            (int) (56 * density)));
            if (openType == Consts.Settings.OPEN_TYPE_SONG_BUNDLE && songBundle.path().equals(openSongBundle.path()))
                listItemButton.setBackgroundResource(R.drawable.list_item_button_selected);
            listItemButton.setOnClickListener(view -> {
                var settingsEditor = settings.edit();
                settingsEditor.putInt("open_type", Consts.Settings.OPEN_TYPE_SONG_BUNDLE);
                settingsEditor.putString("open_song_bundle", songBundle.path());
                if (!songBundle.path()
                        .equals(settings.getString("open_song_bundle", Consts.Settings.SONG_BUNDLE_DEFAULT))) {
                    settingsEditor.putString("open_song_number", Consts.Settings.SONG_BUNDLE_NUMBER_DEFAULT);
                }
                settingsEditor.apply();
                openFromSettings();
                drawer.close();
            });
            drawerSongBundles.addView(listItemButton);

            var listItemButtonLabel = new TextView(this, null, 0, R.style.ListItemButtonLabel);
            listItemButtonLabel
                    .setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1));
            listItemButtonLabel.setText(songBundle.name());
            listItemButton.addView(listItemButtonLabel);

            var listeItemButtonMeta = new TextView(this, null, 0, R.style.ListItemButtonMeta);
            if (songBundle.language().equals("en"))
                listeItemButtonMeta.setText(R.string.settings_language_english);
            if (songBundle.language().equals("nl"))
                listeItemButtonMeta.setText(R.string.settings_language_dutch);
            listItemButton.addView(listeItemButtonMeta);
        }
    }
}
