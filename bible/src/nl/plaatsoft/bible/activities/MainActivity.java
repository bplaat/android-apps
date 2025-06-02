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
import nl.plaatsoft.bible.models.ChapterWithVerses;
import nl.plaatsoft.bible.models.Song;
import nl.plaatsoft.bible.models.SongBundle;
import nl.plaatsoft.bible.models.SongWithText;
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
import nl.plaatsoft.bible.Utils;
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
    private @Nullable Bible openBible;
    private @Nullable Book openBook;
    private @Nullable ChapterWithVerses openChapter;
    private @Nullable String lastBookKey;
    private int lastChapterNumber;
    private int lastChapterScroll;
    private @Nullable SongBundle openSongBundle;
    private @Nullable SongWithText openSong;
    private @Nullable AlertDialog dialog;

    private int oldFont;
    private int oldLanguage;
    private int oldTheme;

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
        useWindowInsets(findViewById(R.id.main_drawer_scroll), chapterPage, chapterNotAvailablePage, songPage);

        // Menu button
        findViewById(R.id.main_menu_button).setOnClickListener(view -> {
            populateDrawer();
            drawer.open();
            updateBackListener();
        });

        // Name button
        nameButton.setOnClickListener(view -> {
            if (openType == Settings.OPEN_TYPE_BIBLE) {
                dialog = new BooksDialogBuilder(this, Objects.requireNonNull(openBible).testaments(),
                        openBook != null ? openBook.key() : settings.getOpenBook(),
                        book -> {
                            Objects.requireNonNull(dialog).dismiss();
                            openChapter(book, book.chapters().get(0));
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
                            Objects.requireNonNull(dialog).dismiss();
                            openChapter(Objects.requireNonNull(openBook), chapter);
                        }).show();
            }
            if (openType == Settings.OPEN_TYPE_SONG_BUNDLE) {
                dialog = new SongsDialogBuilder(this, Objects.requireNonNull(openSongBundle).songs(),
                        Objects.requireNonNull(openSong).number(),
                        song -> {
                            Objects.requireNonNull(dialog).dismiss();
                            openSong(song);
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
            if (openType == Settings.OPEN_TYPE_BIBLE) {
                optionsMenu.getMenuInflater().inflate(R.menu.options_bible, optionsMenu.getMenu());
                optionsMenu.getMenu().findItem(R.id.menu_options_last_open_chapter).setVisible(lastBookKey != null);
            }
            if (openType == Settings.OPEN_TYPE_SONG_BUNDLE)
                optionsMenu.getMenuInflater().inflate(R.menu.options_song_bundle, optionsMenu.getMenu());
            optionsMenu.setOnMenuItemClickListener(this);
            optionsMenu.show();
        });

        // Drawer
        drawer.setOnCloseListener(() -> updateBackListener());

        // Chapter view
        chapterPage.setTypeface(settings.getFontTypeface());
        chapterPage.setOnPreviousListener(() -> {
            if (Objects.requireNonNull(openChapter).number() == 1) {
                // Find previous book
                var allBooks = new ArrayList<Book>();
                for (var testament : Objects.requireNonNull(openBible).testaments())
                    allBooks.addAll(testament.books());
                for (var i = 0; i < allBooks.size(); i++) {
                    if (allBooks.get(i).key().equals(Objects.requireNonNull(openBook).key())) {
                        var previousBook = allBooks.get(i == 0 ? allBooks.size() - 1 : i - 1);
                        openChapter(previousBook, previousBook.chapters().get(previousBook.chapters().size() - 1));
                        return;
                    }
                }
            }

            // Find previous chapter
            for (var chapter : Objects.requireNonNull(openBook).chapters()) {
                if (chapter.number() == Objects.requireNonNull(openChapter).number() - 1) {
                    openChapter(Objects.requireNonNull(openBook), chapter);
                    return;
                }
            }
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
                        openChapter(nextBook, nextBook.chapters().get(0));
                        return;
                    }
                }
            }

            // Find next chapter
            for (var chapter : Objects.requireNonNull(openBook).chapters()) {
                if (chapter.number() == Objects.requireNonNull(openChapter).number() + 1) {
                    openChapter(Objects.requireNonNull(openBook), chapter);
                    return;
                }
            }
        });

        // Song view
        songPage.setTypeface(settings.getFontTypeface());
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

            openSong(songs.get(openSongIndex == 0 ? songs.size() - 1 : openSongIndex - 1));
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

            openSong(songs.get(openSongIndex == songs.size() - 1 ? 0 : openSongIndex + 1));
        });

        // Install bibles from assets and open last opened bible
        try {
            appVersionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException exception) {
            Log.e(getPackageName(), "Can't get app version", exception);
        }
        Runnable installAssetsAndOpen = () -> {
            var skipExisting = settings.getInstalledAssetsVersion().equals(appVersionName);
            settings.setInstalledAssetsVersion(appVersionName);
            bibleService.installBiblesFromAssets(this, skipExisting);
            songBundleService.installSongBundlesFromAssets(this, skipExisting);

            bibles = bibleService.getInstalledBibles(this);
            songBundles = songBundleService.getInstalledSongBundles(this);
            handler.post(() -> openFromSettings());
        };
        installAssetsAndOpen.run();
    }

    @Override
    public boolean onMenuItemClick(@SuppressWarnings("null") MenuItem item) {
        if (item.getItemId() == R.id.menu_options_last_open_chapter) {
            if (lastBookKey != null) {
                var currentLastChapterScrollY = lastChapterScroll;
                lastChapterScroll = chapterPage.getScrollY();
                openBookAndChapter(Objects.requireNonNull(lastBookKey), lastChapterNumber, currentLastChapterScrollY);
            }
            return true;
        }

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
            openChapter(randomBook, randomChapter, 0, randomVerse.id());
            return true;
        }

        if (item.getItemId() == R.id.menu_options_random_song) {
            var openSongBundleSongs = Objects.requireNonNull(openSongBundle).songs();
            var randomSong = openSongBundleSongs.get((int) (Math.random() * openSongBundleSongs.size()));
            openSong(randomSong);
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
                if (openType == Settings.OPEN_TYPE_BIBLE) {
                    var book = Utils.intentGetSerializableExtra(data, SearchActivity.BOOK, Book.class);
                    var chapter = Utils.intentGetSerializableExtra(data, SearchActivity.CHAPTER, Chapter.class);
                    openChapter(book, chapter, 0, data.getIntExtra(SearchActivity.HIGHLIGHT_VERSE, -1));
                }
                if (openType == Settings.OPEN_TYPE_SONG_BUNDLE)
                    openSong(Utils.intentGetSerializableExtra(data, SearchActivity.SONG, Song.class));
            }
            return;
        }

        // When settings activity is closed check for restarts
        if (requestCode == SETTINGS_REQUEST_CODE) {
            if (oldFont != settings.getFont()) {
                chapterPage.setTypeface(settings.getFontTypeface());
                songPage.setTypeface(settings.getFontTypeface());
                openFromSettings();
            }
            if (oldLanguage != settings.getLanguage() || oldTheme != settings.getTheme())
                handler.post(() -> recreate());
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
            openBookAndChapter(settings.getOpenBook(), settings.getOpenChapter(), settings.getOpenChapterScroll());
        }

        if (openType == Settings.OPEN_TYPE_SONG_BUNDLE) {
            openSongBundle = songBundleService.readSongBundle(this, settings.getOpenSongBundle());
            openSong(Objects.requireNonNull(
                    songBundleService.readSong(this, openSongBundle.path(), settings.getOpenSongNumber())));
        }
    }

    private void openBookAndChapter(String bookKey, int chapterNumber, int scrollY) {
        // Find open book
        Book book = null;
        for (var testament : Objects.requireNonNull(openBible).testaments()) {
            for (var otherBook : testament.books()) {
                if (otherBook.key().equals(bookKey)) {
                    book = otherBook;
                    break;
                }
            }
            if (book != null)
                break;
        }

        // Find open chapter
        if (book != null) {
            var chapter = bibleService.readChapter(this, Objects.requireNonNull(openBible).path(),
                    Objects.requireNonNull(book).key(), chapterNumber);
            if (chapter != null) {
                openChapter(book, chapter, scrollY, -1);
                return;
            }
        }

        // When book or chapter not available, show not available page
        openBook = null;
        openChapter = null;
        nameButton.setText(book != null ? book.name() : bookKey + "?");
        indexButton.setText(String.valueOf(chapterNumber));
        openPage(chapterNotAvailablePage);
    }

    private void openChapter(Book book, Chapter chapter) {
        openChapter(book, chapter, 0, -1);
    }

    private void openChapter(Book book, Chapter chapter, int scrollY, int highlightVerseId) {
        openChapter(book,
                Objects.requireNonNull(bibleService.readChapter(this, Objects.requireNonNull(openBible).path(),
                        book.key(), chapter.number())),
                scrollY, highlightVerseId);
    }

    private void openChapter(Book book, ChapterWithVerses chapter, int scrollY, int highlightVerseId) {
        if (openBook != null && openChapter != null && !(openBook.key().equals(book.key())
                && Objects.requireNonNull(openChapter).number() == chapter.number())) {
            lastBookKey = Objects.requireNonNull(openBook).key();
            lastChapterNumber = Objects.requireNonNull(openChapter).number();
            lastChapterScroll = chapterPage.getScrollY();
        }

        openBook = book;
        openChapter = chapter;
        settings.setOpenBook(Objects.requireNonNull(openBook).key());
        settings.setOpenChapter(Objects.requireNonNull(openChapter).number());

        nameButton.setText(openBook != null ? openBook.name() : settings.getOpenBook() + "?");
        indexButton.setText(String.valueOf(Objects.requireNonNull(openChapter).number()));
        chapterPage.openChapter(Objects.requireNonNull(openChapter), scrollY, highlightVerseId);
        openPage(chapterPage);
    }

    private void openSong(Song song) {
        openSong(Objects.requireNonNull(
                songBundleService.readSong(this, Objects.requireNonNull(openSongBundle).path(), song.number())));
    }

    private void openSong(SongWithText song) {
        openSong = song;
        settings.setOpenSongNumber(song.number());
        nameButton.setText(Objects.requireNonNull(openSongBundle).name());
        indexButton.setText(song.number());
        songPage.openSong(song, settings.getOpenSongScroll());
        openPage(songPage);
    }

    private void openPage(View page) {
        nameButton.setClickable(!page.equals(songPage));
        chapterPage.setVisibility(page.equals(chapterPage) ? View.VISIBLE : View.GONE);
        chapterNotAvailablePage.setVisibility(page.equals(chapterNotAvailablePage) ? View.VISIBLE : View.GONE);
        songPage.setVisibility(page.equals(songPage) ? View.VISIBLE : View.GONE);
    }

    private void saveScroll() {
        settings.setOpenChapterScroll(chapterPage.getScrollY());
        settings.setOpenSongScroll(songPage.getScrollY());
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
            if (openType == Settings.OPEN_TYPE_BIBLE && openBible != null
                    && bible.path().equals(Objects.requireNonNull(openBible).path()))
                listItemButton.setBackgroundResource(R.drawable.list_item_button_selected);
            listItemButton.setOnClickListener(view -> {
                saveScroll();
                settings.setOpenType(Settings.OPEN_TYPE_BIBLE);
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
            if (openType == Settings.OPEN_TYPE_SONG_BUNDLE && openSongBundle != null
                    && songBundle.path().equals(Objects.requireNonNull(openSongBundle).path()))
                listItemButton.setBackgroundResource(R.drawable.list_item_button_selected);
            listItemButton.setOnClickListener(view -> {
                saveScroll();
                settings.setOpenType(Settings.OPEN_TYPE_SONG_BUNDLE);
                settings.setOpenSongBundle(songBundle.path());
                if (openSongBundle != null
                        && !songBundle.path().equals(Objects.requireNonNull(openSongBundle).path())) {
                    settings.setOpenSongNumber("1");
                }
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
