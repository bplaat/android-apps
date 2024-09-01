/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.activities;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import nl.plaatsoft.bible.services.BibleService;
import nl.plaatsoft.bible.views.SearchVerseAdapter;
import nl.plaatsoft.bible.Consts;
import nl.plaatsoft.bible.R;

public class SearchActivity extends BaseActivity implements TextWatcher {
    private BibleService bibleService = BibleService.getInstance();
    private ScrollView startPage;
    private ListView resultsPage;
    private SearchVerseAdapter searchVerseAdapter;
    private ScrollView emptyPage;
    private EditText searchInput;

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
        searchVerseAdapter = new SearchVerseAdapter(this);
        if (settings.getInt("font", Consts.Settings.FONT_DEFAULT) == Consts.Settings.FONT_SERIF)
            searchVerseAdapter.setVerseTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        if (settings.getInt("font", Consts.Settings.FONT_DEFAULT) == Consts.Settings.FONT_SANS_SERIF)
            searchVerseAdapter.setVerseTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        if (settings.getInt("font", Consts.Settings.FONT_DEFAULT) == Consts.Settings.FONT_MONOSPACE)
            searchVerseAdapter.setVerseTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));

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

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        var searchQuery = charSequence.toString().toLowerCase();
        if (searchQuery.length() >= 3) {
            searchVerseAdapter.setSearchQuery(searchQuery);
            searchVerseAdapter.clear();
            searchVerseAdapter.addAll(bibleService.searchVerses(this,
                    settings.getString("open_bible", Consts.Settings.getBibleDefault(this)), searchQuery, 25));

            if (searchVerseAdapter.getCount() > 0) {
                openPage(resultsPage);
            } else {
                openPage(emptyPage);
            }
        } else {
            openPage(startPage);
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
