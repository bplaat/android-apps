/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
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
import java.util.Objects;
import javax.annotation.Nullable;

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
import nl.plaatsoft.bible.Settings;
import nl.plaatsoft.bible.R;

public class MainActivity extends BaseActivity implements PopupMenu.OnMenuItemClickListener {
    private static final int SEARCH_REQUEST_CODE = 0;
    private static final int SETTINGS_REQUEST_CODE = 1;

    // Views
    private @SuppressWarnings("null") DrawerLayout drawer;
    private @SuppressWarnings("null") LinearLayout drawerBibles;
    private @SuppressWarnings("null") LinearLayout drawerSongBundles;
    private @SuppressWarnings("null") TextView nameButton;
    private @SuppressWarnings("null") TextView indexButton;
    private @SuppressWarnings("null") ChapterView chapterPage;
    private @SuppressWarnings("null") ScrollView chapterNotAvailablePage;
    private @SuppressWarnings("null") SongView songPage;

    // State
    private BibleService bibleService = BibleService.getInstance();
    private SongBundleService songBundleService = SongBundleService.getInstance();
    private Handler handler = new Handler(Looper.getMainLooper());
    private @SuppressWarnings("null") String appVersionName;
    private @SuppressWarnings("null") ArrayList<Bible> bibles;
    private @SuppressWarnings("null") ArrayList<SongBundle> songBundles;
    private int openType = -1;
    private @Nullable AlertDialog dialog;
    private @Nullable Bible openBible;
    private @Nullable Book openBook;
    private @Nullable Chapter openChapter;
    private @Nullable SongBundle openSongBundle;
    private @Nullable Song openSong;
    private int oldFont = -1;
    private int oldLanguage = -1;
    private int oldTheme = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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
            if (openType == Settings.OPEN_TYPE_BIBLE) {
                var openBookKey = settings.getOpenBook();
                dialog = new BooksDialogBuilder(this, Objects.requireNonNull(openBible).testaments(), openBookKey,
                        book -> {
                            Objects.requireNonNull(dialog).dismiss();
                            if (!book.key().equals(openBookKey)) {
                                settings.setOpenBook(book.key());
                                openChapterFromSettings(-1);
                            }
                        }).show();
            }
        });

        // Index button
        indexButton.setOnClickListener(view -> {
            if (openType == Settings.OPEN_TYPE_BIBLE) {
                if (openChapter == null)
                    return;
                dialog = new ChaptersDialogBuilder(this, Objects.requireNonNull(openBook).chapters(),
                        Objects.requireNonNull(openChapter).number(), chapter -> {
                            Objects.requireNonNull(dialog);
                            dialog.dismiss();
                            if (chapter.number() != Objects.requireNonNull(openChapter).number()) {
                                settings.setOpenChapter(chapter.number());
                                openChapterFromSettings(-1);
                            }
                        }).show();
            }

            if (openType == Settings.OPEN_TYPE_SONG_BUNDLE) {
                dialog = new SongsDialogBuilder(this, Objects.requireNonNull(openSongBundle).songs(),
                        Objects.requireNonNull(openSong).number(),
                        song -> {
                            Objects.requireNonNull(dialog).dismiss();
                            if (!song.number().equals(Objects.requireNonNull(openSong).number())) {
                                settings.setOpenSongNumber(song.number());
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
            if (openType == Settings.OPEN_TYPE_BIBLE)
                optionsMenu.getMenuInflater().inflate(R.menu.options_bible, optionsMenu.getMenu());
            if (openType == Settings.OPEN_TYPE_SONG_BUNDLE)
                optionsMenu.getMenuInflater().inflate(R.menu.options_song_bundle, optionsMenu.getMenu());
            optionsMenu.setOnMenuItemClickListener(this);
            optionsMenu.show();
        });

        // Drawer
        drawer.setOnCloseListener(() -> updateBackListener());

        // Chapter view
        updateFonts(false);
        chapterPage.setOnPreviousListener(() -> {
            if (Objects.requireNonNull(openChapter).number() == 1) {
                // Find previous book
                var allBooks = new ArrayList<Book>();
                for (var testament : Objects.requireNonNull(openBible).testaments())
                    allBooks.addAll(testament.books());
                for (var i = 0; i < allBooks.size(); i++) {
                    if (allBooks.get(i).key().equals(Objects.requireNonNull(openBook).key())) {
                        var previousBook = allBooks.get(i == 0 ? allBooks.size() - 1 : i - 1);
                        settings.setOpenBook(previousBook.key());
                        settings.setOpenChapter(previousBook.chapters().size());
                        break;
                    }
                }
            } else {
                settings.setOpenChapter(Objects.requireNonNull(openChapter).number() - 1);
            }
            openChapterFromSettings(-1);
        });
        chapterPage.setOnNextListener(() -> {
            if (Objects.requireNonNull(openChapter).number() == Objects.requireNonNull(openBook).chapters().size()) {
                // Find next book
                var allBooks = new ArrayList<Book>();
                for (var testament : Objects.requireNonNull(openBible).testaments())
                    allBooks.addAll(testament.books());
                for (var i = 0; i < allBooks.size(); i++) {
                    if (allBooks.get(i).key().equals(Objects.requireNonNull(openBook).key())) {
                        var nextBook = allBooks.get(i == allBooks.size() - 1 ? 0 : i + 1);
                        settings.setOpenBook(nextBook.key());
                        settings.setOpenChapter(1);
                        break;
                    }
                }
            } else {
                settings.setOpenChapter(Objects.requireNonNull(openChapter).number() + 1);
            }
            openChapterFromSettings(-1);
        });

        // Song view
        songPage.setOnPreviousListener(() -> {
            var songs = Objects.requireNonNull(openSongBundle).songs();

            // Get index of openSong
            var openSongIndex = -1;
            for (int i = 0; i < songs.size(); i++) {
                if (songs.get(i).number().equals(Objects.requireNonNull(openSong).number())) {
                    openSongIndex = i;
                    break;
                }
            }

            settings.setOpenSongNumber(songs.get(openSongIndex == 0 ? songs.size() - 1 : openSongIndex - 1).number());
            openSongFromSettings();
        });
        songPage.setOnNextListener(() -> {
            var songs = Objects.requireNonNull(openSongBundle).songs();

            // Get index of openSong
            var openSongIndex = -1;
            for (int i = 0; i < songs.size(); i++) {
                if (songs.get(i).number().equals(Objects.requireNonNull(openSong).number())) {
                    openSongIndex = i;
                    break;
                }
            }

            settings.setOpenSongNumber(songs.get(openSongIndex == songs.size() - 1 ? 0 : openSongIndex + 1).number());
            openSongFromSettings();
        });

        // Install bibles from assets and open last opened bible
        try {
            appVersionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException exception) {
            Log.e(getPackageName(), "Can't get app version", exception);
        }
        Runnable installAssetsAndOpen = () -> {
            if (!settings.getInstalledAssetsVersion().equals(appVersionName)) {
                settings.setInstalledAssetsVersion(appVersionName);
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
    public boolean onMenuItemClick(@SuppressWarnings("null") MenuItem item) {
        if (item.getItemId() == R.id.menu_options_random_verse) {
            var openBibleTestaments = Objects.requireNonNull(openBible).testaments();
            var randomTestament = openBibleTestaments.get((int) (Math.random() * openBibleTestaments.size()));
            var randomBook = randomTestament.books().get((int) (Math.random() * randomTestament.books().size()));
            var randomChapter = Objects
                    .requireNonNull(bibleService.readChapter(this, Objects.requireNonNull(openBible).path(),
                            randomBook.key(),
                            (int) (Math.random() * randomBook.chapters().size()) + 1));

            var realVerses = new ArrayList<Verse>();
            for (var verse : randomChapter.verses()) {
                if (!verse.isSubtitle())
                    realVerses.add(verse);
            }
            var randomVerse = realVerses.get((int) (Math.random() * realVerses.size()));

            settings.setOpenBook(randomBook.key());
            settings.setOpenChapter(randomChapter.number());
            openChapterFromSettings(randomVerse.id());
            return true;
        }

        if (item.getItemId() == R.id.menu_options_random_song) {
            var openSongBundleSongs = Objects.requireNonNull(openSongBundle).songs();
            var randomSong = openSongBundleSongs.get((int) (Math.random() * openSongBundleSongs.size()));
            settings.setOpenSongNumber(randomSong.number());
            openSongFromSettings();
            return true;
        }

        if (item.getItemId() == R.id.menu_options_settings) {
            oldFont = settings.getFont();
            oldLanguage = settings.getLanguage();
            oldTheme = settings.getTheme();
            startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_REQUEST_CODE);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @SuppressWarnings("null") Intent data) {
        // When search activity is closed check open selected book / chapter verse
        if (requestCode == SEARCH_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (openType == Settings.OPEN_TYPE_BIBLE)
                    openChapterFromSettings(data.getIntExtra(Settings.HIGHLIGHT_VERSE, -1));
                if (openType == Settings.OPEN_TYPE_SONG_BUNDLE)
                    openSongFromSettings();
            }
            return;
        }

        // When settings activity is closed check for restarts
        if (requestCode == SETTINGS_REQUEST_CODE) {
            if (oldFont != -1) {
                if (oldFont != settings.getFont())
                    updateFonts(true);
            }
            if (oldLanguage != -1 && oldTheme != -1) {
                if (oldLanguage != settings.getLanguage() || oldTheme != settings.getTheme()) {
                    handler.post(() -> recreate());
                }
            }
        }
    }

    @Override
    public void onPause() {
        saveScroll();
        super.onPause();
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
        openType = settings.getOpenType();

        if (openType == Settings.OPEN_TYPE_BIBLE) {
            openBible = bibleService.readBible(this, settings.getOpenBible(), true);
            nameButton.setClickable(true);
            openChapterFromSettings(-1);
        }

        if (openType == Settings.OPEN_TYPE_SONG_BUNDLE) {
            openSongBundle = songBundleService.readSongBundle(this, settings.getOpenSongBundle());
            nameButton.setClickable(false);
            nameButton.setText(Objects.requireNonNull(openSongBundle).name());
            openSongFromSettings();
        }
    }

    private void openChapterFromSettings(int highlightVerseId) {
        var openBookKey = settings.getOpenBook();
        var openChapterNumber = settings.getOpenChapter();
        openChapter = bibleService.readChapter(this, Objects.requireNonNull(openBible).path(), openBookKey,
                openChapterNumber);
        indexButton.setText(String.valueOf(openChapterNumber));

        // Get book
        openBook = null;
        for (var testament : Objects.requireNonNull(openBible).testaments()) {
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
        chapterPage.openChapter(Objects.requireNonNull(openChapter), settings.getOpenChapterScroll(),
                highlightVerseId);
        openPage(chapterPage);
    }

    private void openSongFromSettings() {
        var openSongNumber = settings.getOpenSongNumber();
        openSong = songBundleService.readSong(this, Objects.requireNonNull(openSongBundle).path(), openSongNumber);
        indexButton.setText(openSongNumber);
        songPage.openSong(Objects.requireNonNull(openSong), settings.getOpenSongScroll());
        openPage(songPage);
    }

    private void openPage(View page) {
        chapterPage.setVisibility(page.equals(chapterPage) ? View.VISIBLE : View.GONE);
        chapterNotAvailablePage.setVisibility(page.equals(chapterNotAvailablePage) ? View.VISIBLE : View.GONE);
        songPage.setVisibility(page.equals(songPage) ? View.VISIBLE : View.GONE);
    }

    private void saveScroll() {
        if (openType == Settings.OPEN_TYPE_BIBLE)
            settings.setOpenChapterScroll(chapterPage.getScrollY());
        if (openType == Settings.OPEN_TYPE_SONG_BUNDLE)
            settings.setOpenSongScroll(songPage.getScrollY());
    }

    private void updateFonts(boolean reopen) {
        // Update chapter view
        chapterPage.setTypeface(settings.getFontTypeface());
        if (reopen && openType == Settings.OPEN_TYPE_BIBLE)
            openChapterFromSettings(-1);

        // Update song view
        songPage.setTypeface(settings.getFontTypeface());
        if (reopen && openType == Settings.OPEN_TYPE_SONG_BUNDLE)
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
            if (openType == Settings.OPEN_TYPE_BIBLE && bible.path().equals(Objects.requireNonNull(openBible).path()))
                listItemButton.setBackgroundResource(R.drawable.list_item_button_selected);
            listItemButton.setOnClickListener(view -> {
                saveScroll();
                settings.setOpenType(Settings.OPEN_TYPE_BIBLE);
                if (!bible.path().equals(settings.getOpenBible()))
                    settings.setOpenBible(bible.path());
                openFromSettings();
                drawer.close();
            });
            drawerBibles.addView(listItemButton);

            var listItemButtonLabel = new TextView(this, null, 0, R.style.ListItemButtonLabel);
            listItemButtonLabel
                    .setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1));
            listItemButtonLabel.setText(bible.name());
            listItemButton.addView(listItemButtonLabel);

            var listItemButtonMeta = new TextView(this, null, 0, R.style.ListItemButtonMeta);
            if (bible.language().equals("en"))
                listItemButtonMeta.setText(R.string.settings_language_english);
            if (bible.language().equals("nl"))
                listItemButtonMeta.setText(R.string.settings_language_dutch);
            if (bible.language().equals("de"))
                listItemButtonMeta.setText(R.string.settings_language_german);
            listItemButton.addView(listItemButtonMeta);
        }

        // Add song bundle list buttons
        drawerSongBundles.removeAllViews();
        for (var songBundle : songBundles) {
            var listItemButton = new LinearLayout(this, null, 0, R.style.ListItemButton);
            listItemButton
                    .setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            (int) (56 * density)));
            if (openType == Settings.OPEN_TYPE_SONG_BUNDLE
                    && songBundle.path().equals(Objects.requireNonNull(openSongBundle).path()))
                listItemButton.setBackgroundResource(R.drawable.list_item_button_selected);
            listItemButton.setOnClickListener(view -> {
                saveScroll();
                settings.setOpenType(Settings.OPEN_TYPE_SONG_BUNDLE);
                if (!songBundle.path().equals(settings.getOpenSongBundle()))
                    settings.setOpenSongBundle(songBundle.path());
                openFromSettings();
                drawer.close();
            });
            drawerSongBundles.addView(listItemButton);

            var listItemButtonLabel = new TextView(this, null, 0, R.style.ListItemButtonLabel);
            listItemButtonLabel
                    .setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1));
            listItemButtonLabel.setText(songBundle.name());
            listItemButton.addView(listItemButtonLabel);

            var listItemButtonMeta = new TextView(this, null, 0, R.style.ListItemButtonMeta);
            if (songBundle.language().equals("en"))
                listItemButtonMeta.setText(R.string.settings_language_english);
            if (songBundle.language().equals("nl"))
                listItemButtonMeta.setText(R.string.settings_language_dutch);
            if (songBundle.language().equals("de"))
                listItemButtonMeta.setText(R.string.settings_language_german);
            listItemButton.addView(listItemButtonMeta);
        }
    }
}
