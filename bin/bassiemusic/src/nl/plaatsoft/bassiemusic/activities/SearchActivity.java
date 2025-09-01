/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bassiemusic.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import nl.plaatsoft.bassiemusic.R;
import nl.plaatsoft.bassiemusic.components.MusicAdapter;
import nl.plaatsoft.bassiemusic.models.Music;

import org.jspecify.annotations.Nullable;

public class SearchActivity extends BaseActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        findViewById(R.id.search_back_button).setOnClickListener(v -> { finish(); });

        var music = Music.loadMusic(this);

        var startPage = (ScrollView)findViewById(R.id.search_start_page);
        var searchList = (ListView)findViewById(R.id.search_music_page);
        var emptyPage = (ScrollView)findViewById(R.id.search_empty_page);
        useWindowInsets(startPage, searchList, emptyPage);

        var searchAdapter = new MusicAdapter(this);
        searchList.setAdapter(searchAdapter);
        searchList.setOnItemClickListener((AdapterView<?> adapterView, View view, int position, long id) -> {
            Music musicItem = searchAdapter.getItem(position);
            Intent intent = getIntent();
            intent.putExtra("id", musicItem.getId());
            setResult(Activity.RESULT_OK, intent);
            finish();
        });

        var searchInput = (EditText)findViewById(R.id.search_input);

        searchInput.setOnEditorActionListener((TextView view, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH)
                return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
            return false;
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(
                @SuppressWarnings("null") CharSequence charSequence, int start, int before, int count) {
                var searchQuery = charSequence.toString().toLowerCase();

                searchAdapter.clear();

                if (searchQuery.length() >= 1) {
                    for (var musicItem : music) {
                        if (String.join(", ", musicItem.getArtists()).toLowerCase().contains(searchQuery)
                            || musicItem.getAlbum().toLowerCase().contains(searchQuery)
                            || musicItem.getTitle().toLowerCase().contains(searchQuery)) {
                            searchAdapter.add(musicItem);
                        }
                    }

                    if (searchAdapter.getCount() > 0) {
                        startPage.setVisibility(View.GONE);
                        searchList.setVisibility(View.VISIBLE);
                        emptyPage.setVisibility(View.GONE);
                    } else {
                        startPage.setVisibility(View.GONE);
                        searchList.setVisibility(View.GONE);
                        emptyPage.setVisibility(View.VISIBLE);
                    }
                } else {
                    startPage.setVisibility(View.VISIBLE);
                    searchList.setVisibility(View.GONE);
                    emptyPage.setVisibility(View.GONE);
                }

                searchList.setSelectionAfterHeaderView();
            }

            public void beforeTextChanged(
                @SuppressWarnings("null") CharSequence charSequence, int start, int count, int after) {}

            public void afterTextChanged(@SuppressWarnings("null") Editable editable) {}
        });

        View.OnClickListener clearSearchInput = v -> {
            searchInput.setText("");
        };
        findViewById(R.id.search_clear_button).setOnClickListener(clearSearchInput);
        findViewById(R.id.search_empty_hero_button).setOnClickListener(clearSearchInput);
    }
}
