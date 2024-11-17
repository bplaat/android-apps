/*
 * Copyright (c) 2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.views;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import nl.plaatsoft.bible.models.SearchVerse;
import nl.plaatsoft.bible.R;
import nl.plaatsoft.bible.Utils;

@ParametersAreNonnullByDefault
public class SearchVerseAdapter extends ArrayAdapter<SearchVerse> {
    private static record ViewHolder(TextView contents, TextView bookChapter) {
    }

    private String searchQuery = "";
    private Typeface verseTypeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL);

    public SearchVerseAdapter(Context context) {
        super(context, 0);
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public void setVerseTypeface(Typeface versTypeface) {
        this.verseTypeface = versTypeface;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @Nullable ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_search_verse, parent, false);
            Objects.requireNonNull(convertView);
            viewHolder = new ViewHolder(convertView.findViewById(R.id.search_verse_contents),
                    convertView.findViewById(R.id.search_verse_book_chapter));
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        var searchVerse = getItem(position);

        var verseContents = searchVerse.verse().isSubtitle()
                ? searchVerse.verse().text()
                : searchVerse.verse().number() + " " + searchVerse.verse().text();
        var span = new SpannableString(verseContents);
        if (!searchVerse.verse().isSubtitle()) {
            span.setSpan(
                    new ForegroundColorSpan(Utils.contextGetColor(getContext(), R.color.secondary_text_color)), 0,
                    searchVerse.verse().number().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new RelativeSizeSpan(0.75f), 0, searchVerse.verse().number().length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        var highlightStart = verseContents.toLowerCase().indexOf(searchQuery.toLowerCase());
        span.setSpan(
                new BackgroundColorSpan(Utils.contextGetColor(getContext(), R.color.highlight_text_color)),
                highlightStart, highlightStart + searchQuery.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        viewHolder.contents.setTypeface(
                searchVerse.verse().isSubtitle() ? Typeface.create(verseTypeface, Typeface.BOLD) : verseTypeface);
        viewHolder.contents.setLineSpacing(0, searchVerse.verse().isSubtitle() ? 1.0f : 1.2f);
        viewHolder.contents.setText(span);

        viewHolder.bookChapter
                .setText(searchVerse.book().name() + " " + searchVerse.chapter().number());

        return convertView;
    }
}
