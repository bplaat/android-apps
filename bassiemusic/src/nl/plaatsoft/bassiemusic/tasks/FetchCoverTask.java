/*
 * Copyright (c) 2021-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.bassiemusic.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import nl.plaatsoft.android.fetch.FetchDataTask;
import nl.plaatsoft.android.fetch.FetchImageTask;
import nl.plaatsoft.bassiemusic.models.Music;
import nl.plaatsoft.bassiemusic.R;
import nl.plaatsoft.bassiemusic.Utils;

public class FetchCoverTask {
    private static final String DEEZER_API_URL = "https://api.deezer.com";
    private static final String DEEZER_API_SEARCH_ALBUM = DEEZER_API_URL + "/search/album";

    public static interface OnLoadListener {
        public abstract void onLoad(Bitmap image);
    }

    public static interface OnErrorListener {
        public abstract void onError(Exception exception);
    }

    public class NoCoverFound extends Exception {
        private static final long serialVersionUID = 1;

        public NoCoverFound(String message) {
            super(message);
        }
    }

    private Context context;
    private Music music;
    private boolean isFadedIn;
    private boolean isLoadedFomCache = true;
    private boolean isSavedToCache = true;
    private OnLoadListener onLoadListener;
    private OnErrorListener onErrorListener;
    private ImageView imageView;

    private FetchCoverTask(Context context) {
        this.context = context;
    }

    public Music getMusic() {
        return music;
    }

    public static FetchCoverTask with(Context context) {
        return new FetchCoverTask(context);
    }

    public FetchCoverTask fromMusic(Music music) {
        this.music = music;
        return this;
    }

    public FetchCoverTask fadeIn() {
        isFadedIn = true;
        return this;
    }

    public FetchCoverTask noCache() {
        isLoadedFomCache = false;
        isSavedToCache = false;
        return this;
    }

    public FetchCoverTask notFromCache() {
        isLoadedFomCache = false;
        return this;
    }

    public FetchCoverTask notToCache() {
        isSavedToCache = false;
        return this;
    }

    public FetchCoverTask then(OnLoadListener onLoadListener) {
        this.onLoadListener = onLoadListener;
        return this;
    }

    public FetchCoverTask then(OnLoadListener onLoadListener, OnErrorListener onErrorListener) {
        this.onLoadListener = onLoadListener;
        this.onErrorListener = onErrorListener;
        return this;
    }

    public FetchCoverTask into(ImageView imageView) {
        this.imageView = imageView;
        return this;
    }

    public FetchCoverTask fetch() {
        var imageCoverTask = FetchImageTask.with(context).load(music.getCoverUri().toString())
                .loadingColor(Utils.contextGetColor(context, R.color.loading_background_color));
        if (isFadedIn)
            imageCoverTask.fadeIn();
        imageCoverTask.into(imageView).then(image -> {
            onLoad(image);
        }, exception -> {
            // When an album cover don't exists fetch and cache it from the nice and open
            // Deezer API. I know this code is a callback / exception nightmare, I'm not
            // working on it :^)
            try {
                var fetchDataTask = FetchDataTask.with(context)
                        .load(DEEZER_API_SEARCH_ALBUM + "?q="
                                + URLEncoder.encode(music.getArtists().get(0) + " - " + music.getAlbum(),
                                        StandardCharsets.UTF_8.name())
                                + "&limit=1");
                if (isLoadedFomCache)
                    fetchDataTask.loadFromCache(true);
                if (isSavedToCache)
                    fetchDataTask.saveToCache(true);
                fetchDataTask.then(data -> {
                    try {
                        var albumsJson = new JSONObject(new String(data, StandardCharsets.UTF_8)).getJSONArray("data");
                        if (albumsJson.length() > 0) {
                            var albumJson = albumsJson.getJSONObject(0);
                            var fetchImageTask = FetchImageTask.with(context).load(albumJson.getString("cover_medium"))
                                    .loadingColor(Utils.contextGetColor(context, R.color.loading_background_color));
                            if (isFadedIn)
                                fetchImageTask.fadeIn();
                            if (!isLoadedFomCache)
                                fetchImageTask.loadFromCache(false);
                            if (!isSavedToCache)
                                fetchImageTask.saveToCache(false);
                            fetchImageTask.into(imageView).then(image -> {
                                onLoad(image);
                            }, exception2 -> {
                                onException(exception2);
                            }).fetch();
                        } else {
                            onException(new NoCoverFound("No album cover was found via the Deezer API"));
                        }
                    } catch (Exception exception2) {
                        onException(exception2);
                    }
                }, exception2 -> {
                    onException(exception2);
                }).fetch();
            } catch (Exception exception2) {
                onException(exception2);
            }
        }).fetch();
        return this;
    }

    private void onLoad(Bitmap image) {
        if (onLoadListener != null) {
            onLoadListener.onLoad(image);
        }
    }

    private void onException(Exception exception) {
        imageView.setImageBitmap(null);

        if (onErrorListener != null) {
            onErrorListener.onError(exception);
        } else {
            Log.e(context.getPackageName(), "An exception catched!", exception);
        }
    }
}
