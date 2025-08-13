/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.views;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.Objects;
import javax.annotation.Nullable;

import nl.plaatsoft.android.compat.ContextCompat;
import nl.plaatsoft.bible.models.Song;
import nl.plaatsoft.bible.R;

public class SongAdapter extends ArrayAdapter<Song> {
    private static record ViewHolder(TextView name) {
    }

    private String searchQuery = "";

    public SongAdapter(Context context) {
        super(context, 0);
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @Nullable ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = Objects
                    .requireNonNull(LayoutInflater.from(getContext()).inflate(R.layout.item_song, parent, false));
            viewHolder = new ViewHolder(convertView.findViewById(R.id.song_name));
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        var song = getItem(position);

        var songName = song.number() + ". " + song.title();
        var span = new SpannableString(songName);
        var highlightStart = songName.toLowerCase().indexOf(searchQuery.toLowerCase());
        if (highlightStart != -1)
            span.setSpan(
                    new BackgroundColorSpan(ContextCompat.getColor(getContext(), R.color.highlight_text_color)),
                    highlightStart, highlightStart + searchQuery.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        viewHolder.name.setText(span);

        return convertView;
    }
}
