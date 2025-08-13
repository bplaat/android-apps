/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bassiemusic.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

public class Music {
    private long id;
    private @SuppressWarnings("null") List<String> artists;
    private @SuppressWarnings("null") String album;
    private @SuppressWarnings("null") Uri CoverUri;
    private @SuppressWarnings("null") String title;
    private int position;
    private long duration;
    private @SuppressWarnings("null") Uri contentUri;

    public long getId() {
        return id;
    }

    public List<String> getArtists() {
        return artists;
    }

    public String getAlbum() {
        return album;
    }

    public Uri getCoverUri() {
        return CoverUri;
    }

    public String getTitle() {
        return title;
    }

    public int getPosition() {
        return position;
    }

    public long getDuration() {
        return duration;
    }

    public Uri getContentUri() {
        return contentUri;
    }

    public static String formatDuration(long ms) {
        var s = ms / 1000;
        if (s >= 3600) {
            return String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60);
        } else {
            return String.format("%d:%02d", s / 60, s % 60);
        }
    }

    public static List<Music> loadMusic(Context context) {
        var musicList = new ArrayList<Music>();

        var columns = new ArrayList<>(List.of(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            columns.add(MediaStore.Audio.Media.CD_TRACK_NUMBER);

        var musicCursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                columns.toArray(new String[columns.size()]),
                null, null, null);

        if (musicCursor != null) {
            while (musicCursor.moveToNext()) {
                var music = new Music();
                music.id = musicCursor.getLong(musicCursor.getColumnIndex(MediaStore.Audio.Media._ID));
                music.contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, music.id);
                var artist = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                        .trim();
                music.album = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)).trim();
                music.title = musicCursor.getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)).trim();

                var trackNumber = "1";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    trackNumber = musicCursor
                            .getString(musicCursor.getColumnIndex(MediaStore.Audio.Media.CD_TRACK_NUMBER));
                }

                music.duration = musicCursor.getLong(musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                long albumId = musicCursor.getLong(musicCursor.getColumnIndex(
                        MediaStore.Audio.Media.ALBUM_ID));
                music.CoverUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),
                        albumId);

                music.artists = new ArrayList<String>();
                music.position = 1;
                if (artist != null && !artist.equals("<unknown>")) {
                    var artistParts = artist.split(",");
                    for (var artistPart : artistParts) {
                        music.artists.add(artistPart.trim());
                    }

                    if (trackNumber != null) {
                        var trackNumberParts = trackNumber.split("/");
                        if (trackNumberParts.length >= 1) {
                            music.position = Integer.parseInt(trackNumberParts[0]);
                        }
                    }
                } else {
                    var titleParts = music.title.split("-");
                    if (titleParts.length >= 2) {
                        music.artists.add(titleParts[0].trim());

                        var position = 2;
                        try {
                            music.position = Integer.parseInt(titleParts[1].trim());
                            music.album = titleParts[0].trim();
                        } catch (Exception exception) {
                            music.album = titleParts[1].trim();
                            try {
                                music.position = Integer.parseInt(titleParts[2].trim());
                                position = 3;
                            } catch (Exception exception2) {
                            }
                        }

                        var titleBuilder = new StringBuilder();
                        for (; position < titleParts.length; position++) {
                            titleBuilder.append(titleParts[position].trim());
                            if (position != titleParts.length - 1) {
                                titleBuilder.append(" ");
                            }
                        }
                        music.title = titleBuilder.toString();
                    }
                }

                if (music.title.equals("")) {
                    music.title = music.album;
                }

                if (music.artists.size() == 0) {
                    music.artists.add("Unkown artist");
                }

                musicList.add(music);
            }
            musicCursor.close();
        }

        Collections.sort(musicList,
                (a, b) -> a.getPosition() - b.getPosition());
        Collections.sort(musicList,
                (a, b) -> a.getAlbum().compareToIgnoreCase(b.getAlbum()));
        Collections.sort(musicList,
                (a, b) -> a.getArtists().get(0).compareToIgnoreCase(b.getArtists().get(0)));

        return musicList;
    }
}
