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
import nl.plaatsoft.bible.services.BibleService;
import nl.plaatsoft.bible.services.SongBundleService;
import nl.plaatsoft.bible.views.SearchVerseAdapter;
import nl.plaatsoft.bible.views.SongAdapter;
import nl.plaatsoft.bible.Consts;
import nl.plaatsoft.bible.R;

public class SearchActivity extends BaseActivity implements TextWatcher {
    // Views
    private ScrollView startPage;
    private ListView resultsPage;
    private ScrollView emptyPage;
    private EditText searchInput;

    // State
    private BibleService bibleService = BibleService.getInstance();
    private SongBundleService songBundleService = SongBundleService.getInstance();
    private int openType; // Initialized in onCreate
    private SearchVerseAdapter searchVerseAdapter = null;
    private SongAdapter songAdapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        openType = settings.getInt("open_type", Consts.Settings.OPEN_TYPE_DEFAULT);

        if (openType == Consts.Settings.OPEN_TYPE_BIBLE) {
            searchVerseAdapter = new SearchVerseAdapter(this);
            searchVerseAdapter
                    .setVerseTypeface(
                            Consts.Settings.getFontTypeface(settings.getInt("font", Consts.Settings.FONT_DEFAULT)));
            resultsPage.setAdapter(searchVerseAdapter);
            resultsPage.setOnItemClickListener((adapterView, view, position, id) -> {
                var searchVerse = searchVerseAdapter.getItem(position);
                var edit = settings.edit();
                edit.putString("open_book", searchVerse.book().key());
                edit.putInt("open_chapter", searchVerse.chapter().number());
                edit.apply();

                var intent = getIntent();
                intent.putExtra("highlight_verse", searchVerse.verse().id());
                setResult(Activity.RESULT_OK, intent);
                finish();
            });
        }

        if (openType == Consts.Settings.OPEN_TYPE_SONG_BUNDLE) {
            songAdapter = new SongAdapter(this);
            resultsPage.setAdapter(songAdapter);
            resultsPage.setOnItemClickListener((adapterView, view, position, id) -> {
                var song = songAdapter.getItem(position);
                var edit = settings.edit();
                edit.putString("open_song_number", song.number());
                edit.apply();

                setResult(Activity.RESULT_OK, getIntent());
                finish();
            });
        }
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        var searchQuery = charSequence.toString().toLowerCase();
        if (searchQuery.length() < 3) {
            openPage(startPage);
            return;
        }

        if (openType == Consts.Settings.OPEN_TYPE_BIBLE) {
            var verses = bibleService.searchVerses(this,
                    settings.getString("open_bible", Consts.Settings.getBibleDefault(this)), searchQuery, 25);
            if (verses.size() > 0) {
                searchVerseAdapter.setSearchQuery(searchQuery);
                searchVerseAdapter.clear();
                searchVerseAdapter.addAll(verses);
                openPage(resultsPage);
            } else {
                openPage(emptyPage);
            }
        }

        if (openType == Consts.Settings.OPEN_TYPE_SONG_BUNDLE) {
            var songs = songBundleService.searchSongs(this,
                    settings.getString("open_song_bundle", Consts.Settings.SONG_BUNDLE_DEFAULT), searchQuery, 25);
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

    private void openPage(View page) {
        startPage.setVisibility(page.equals(startPage) ? View.VISIBLE : View.GONE);
        resultsPage.setVisibility(page.equals(resultsPage) ? View.VISIBLE : View.GONE);
        emptyPage.setVisibility(page.equals(emptyPage) ? View.VISIBLE : View.GONE);
    }
}
