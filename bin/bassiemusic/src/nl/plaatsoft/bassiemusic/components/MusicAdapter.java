/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bassiemusic.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import nl.plaatsoft.bassiemusic.R;
import nl.plaatsoft.bassiemusic.models.Music;
import nl.plaatsoft.bassiemusic.tasks.FetchCoverTask;

import org.jspecify.annotations.Nullable;

public class MusicAdapter extends ArrayAdapter<Music> implements SectionIndexer {
    private static class ViewHolder {
        public @SuppressWarnings("null") TextView musicPosition;
        public @SuppressWarnings("null") ImageView musicCover;
        public @SuppressWarnings("null") TextView musicTitle;
        public @SuppressWarnings("null") TextView musicArtists;
        public @SuppressWarnings("null") TextView musicAlbum;
        public @SuppressWarnings("null") TextView musicDuration;
    }

    private static class Section {
        public char character;
        public int position;
    }

    private @Nullable List<Section> sections;
    private int selectedPosition = -1;

    public MusicAdapter(Context context) {
        super(context, 0);
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @SuppressWarnings("null") ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_music, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.musicPosition = Objects.requireNonNull(convertView).findViewById(R.id.music_position);
            viewHolder.musicCover = convertView.findViewById(R.id.music_cover);
            viewHolder.musicTitle = convertView.findViewById(R.id.music_title);
            viewHolder.musicArtists = convertView.findViewById(R.id.music_artists);
            viewHolder.musicAlbum = convertView.findViewById(R.id.music_album);
            viewHolder.musicDuration = convertView.findViewById(R.id.music_duration);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        if (position == selectedPosition) {
            convertView.setBackgroundResource(R.color.selected_background_color);
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        var music = getItem(position);

        viewHolder.musicPosition.setText(String.valueOf(music.getPosition()));

        FetchCoverTask.with(getContext()).fromMusic(music).fadeIn().into(viewHolder.musicCover).fetch();

        viewHolder.musicTitle.setText(music.getTitle());

        if (position == selectedPosition) {
            viewHolder.musicTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            viewHolder.musicTitle.setSelected(true);
        } else {
            viewHolder.musicTitle.setEllipsize(null);
            viewHolder.musicTitle.setSelected(false);
        }

        var displayMetrics = getContext().getResources().getDisplayMetrics();
        if (displayMetrics.widthPixels / displayMetrics.density < 600) {
            viewHolder.musicArtists.setText(String.join(", ", music.getArtists()) + " - " + music.getAlbum());
        } else {
            viewHolder.musicArtists.setText(String.join(", ", music.getArtists()));
            viewHolder.musicAlbum.setText(music.getAlbum());
        }

        if (position == selectedPosition) {
            viewHolder.musicArtists.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            viewHolder.musicArtists.setSelected(true);
        } else {
            viewHolder.musicArtists.setEllipsize(null);
            viewHolder.musicArtists.setSelected(false);
        }

        viewHolder.musicDuration.setText(Music.formatDuration(music.getDuration()));

        return convertView;
    }

    public void refreshSections() {
        sections = null;
    }

    @Override
    public Object[] getSections() {
        if (sections == null) {
            sections = new ArrayList<Section>();

            for (int position = 0; position < getCount(); position++) {
                var music = getItem(position);
                var firstCharacter = Character.toUpperCase(music.getArtists().get(0).charAt(0));

                var isCharacterFound = false;
                for (var section : Objects.requireNonNull(sections)) {
                    if (section.character == firstCharacter) {
                        isCharacterFound = true;
                        break;
                    }
                }

                if (!isCharacterFound) {
                    var section = new Section();
                    section.character = firstCharacter;
                    section.position = position;
                    Objects.requireNonNull(sections).add(section);
                }
            }
        }

        var sectionsArray = new String[Objects.requireNonNull(sections).size()];
        for (var i = 0; i < Objects.requireNonNull(sections).size(); i++) {
            sectionsArray[i] = String.valueOf(Objects.requireNonNull(sections).get(i).character);
        }
        return sectionsArray;
    }

    @Override
    public int getPositionForSection(int section) {
        return Objects.requireNonNull(sections).get(section).position;
    }

    @Override
    public int getSectionForPosition(int position) {
        var music = getItem(position);
        var firstCharacter = Character.toUpperCase(music.getArtists().get(0).charAt(0));
        for (var i = 0; i < Objects.requireNonNull(sections).size(); i++) {
            if (Objects.requireNonNull(sections).get(i).character == firstCharacter) {
                return i;
            }
        }
        return 0;
    }
}
