/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bible.views;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ScrollView;
import android.widget.TextView;

import nl.plaatsoft.bible.R;
import nl.plaatsoft.bible.models.Song;

public class SongsDialogBuilder extends AlertDialog.Builder {
    public static interface OnResultListener {
        void onResult(Song song);
    }

    private Handler handler = new Handler(Looper.getMainLooper());

    @SuppressWarnings("this-escape")
    public SongsDialogBuilder(
        Context context, ArrayList<Song> songs, String currentSongNumber, OnResultListener onResultListener) {
        super(context);

        setTitle(R.string.main_song_alert_title_label);
        var root = new ScrollView(context);
        setView(root);

        var grid = new FixedGridLayout(context, null, 0, R.style.IndexDialog);
        grid.setColumnCount(6);
        root.addView(grid);

        for (var song : songs) {
            var songButton = new TextView(context, null, 0,
                song.number().equals(currentSongNumber) ? R.style.IndexDialogButtonActive : R.style.IndexDialogButton);
            songButton.setText(String.valueOf(song.number()));
            songButton.setOnClickListener(view -> onResultListener.onResult(song));
            grid.addView(songButton);

            if (song.number().equals(currentSongNumber))
                handler.post(() -> root.scrollTo(0, songButton.getTop()));
        }
    }
}
