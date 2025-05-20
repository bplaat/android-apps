/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import nl.plaatsoft.bible.services.BibleService;
import nl.plaatsoft.bible.services.SongBundleService;
import nl.plaatsoft.bible.views.SearchVerseAdapter;
import nl.plaatsoft.bible.views.SongAdapter;
import nl.plaatsoft.bible.R;
import nl.plaatsoft.bible.Settings;

public class SearchActivity extends BaseActivity implements TextWatcher {
    // Views initialized in onCreate
    private ScrollView startPage;
    private ListView resultsPage;
    private ScrollView emptyPage;
    private EditText searchInput;

    // State
    private @Nonnull BibleService bibleService = BibleService.getInstance();
    private @Nonnull SongBundleService songBundleService = SongBundleService.getInstance();
    private @Nullable SearchVerseAdapter searchVerseAdapter = null;
    private @Nullable SongAdapter songAdapter = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        startPage = findViewById(R.id.search_start_page);
        resultsPage = findViewById(R.id.search_results_page);
        emptyPage = findViewById(R.id.search_empty_page);
        searchInput = findViewById(R.id.search_input);
        useWindowInsets(startPage, resultsPage, emptyPage);

        // Back button
        findViewById(R.id.search_back_button).setOnClickListener(view -> finish());

        // Clear button
        findViewById(R.id.search_clear_button).setOnClickListener(view -> searchInput.setText(""));
        findViewById(R.id.search_empty_hero_button).setOnClickListener(view -> searchInput.setText(""));

        // Search input
        searchInput.addTextChangedListener(this);

        // Results list
        var openType = settings.getOpenType();

        if (openType == Settings.OPEN_TYPE_BIBLE) {
            searchVerseAdapter = new SearchVerseAdapter(this);
            searchVerseAdapter
                    .setVerseTypeface(settings.getFontTypeface());
            resultsPage.setAdapter(searchVerseAdapter);
            resultsPage.setOnItemClickListener((adapterView, view, position, id) -> {
                Objects.requireNonNull(searchVerseAdapter);
                var searchVerse = searchVerseAdapter.getItem(position);
                settings.setOpenBook(searchVerse.book().key());
                settings.setOpenChapter(searchVerse.chapter().number());

                var intent = getIntent();
                intent.putExtra(Settings.HIGHLIGHT_VERSE, searchVerse.verse().id());
                setResult(Activity.RESULT_OK, intent);
                finish();
            });
        }

        if (openType == Settings.OPEN_TYPE_SONG_BUNDLE) {
            songAdapter = new SongAdapter(this);
            resultsPage.setAdapter(songAdapter);
            resultsPage.setOnItemClickListener((adapterView, view, position, id) -> {
                Objects.requireNonNull(songAdapter);
                var song = songAdapter.getItem(position);
                settings.setOpenSongNumber(song.number());

                setResult(Activity.RESULT_OK, getIntent());
                finish();
            });
        }
    }

    @Override
    @SuppressWarnings("null") // FIXME: Null analysis is incorrect, so we suppress it
    public void onTextChanged(@Nonnull CharSequence charSequence, int start, int before, int count) {
        var searchQuery = charSequence.toString().toLowerCase();
        if (searchQuery.length() < 3) {
            openPage(startPage);
            return;
        }

        if (searchVerseAdapter != null) {
            var verses = bibleService.searchVerses(this, settings.getOpenBible(), searchQuery, 25);
            if (verses.size() > 0) {
                searchVerseAdapter.setSearchQuery(searchQuery);
                searchVerseAdapter.clear();
                searchVerseAdapter.addAll(verses);
                openPage(resultsPage);
            } else {
                openPage(emptyPage);
            }
        }

        if (songAdapter != null) {
            var songs = songBundleService.searchSongs(this, settings.getOpenSongBundle(), searchQuery, 25);
            if (songs.size() > 0) {
                songAdapter.setSearchQuery(searchQuery);
                songAdapter.clear();
                songAdapter.addAll(songs);
                openPage(resultsPage);
            } else {
                openPage(emptyPage);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    private void openPage(@Nonnull View page) {
        startPage.setVisibility(page.equals(startPage) ? View.VISIBLE : View.GONE);
        resultsPage.setVisibility(page.equals(resultsPage) ? View.VISIBLE : View.GONE);
        emptyPage.setVisibility(page.equals(emptyPage) ? View.VISIBLE : View.GONE);
    }
}
