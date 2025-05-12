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

import nl.plaatsoft.bassiemusic.models.Music;
import org.json.JSONArray;
import org.json.JSONObject;

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
        FetchImageTask imageCoverTask = FetchImageTask.with(context).load(music.getCoverUri());
        if (isFadedIn)
            imageCoverTask.fadeIn();
        imageCoverTask.into(imageView).then(image -> {
            onLoad(image);
        }, exception -> {
            // When an album cover dont exists fetch and cache it from the nice and open
            // Deezer API
            // I know this code is a callback / exception nightmare, I'm working on it
            try {
                FetchDataTask fetchDataTask = FetchDataTask.with(context).load(DEEZER_API_SEARCH_ALBUM + "?q=" +
                        URLEncoder.encode(music.getArtists().get(0) + " - " + music.getAlbum(), "UTF-8") + "&limit=1");
                if (isLoadedFomCache)
                    fetchDataTask.fromCache();
                if (isSavedToCache)
                    fetchDataTask.toCache();
                fetchDataTask.then(data -> {
                    try {
                        JSONArray albumsJson = new JSONObject(data).getJSONArray("data");
                        if (albumsJson.length() > 0) {
                            JSONObject albumJson = albumsJson.getJSONObject(0);
                            FetchImageTask fetchImageTask = FetchImageTask.with(context)
                                    .load(albumJson.getString("cover_medium"));
                            if (isFadedIn)
                                fetchImageTask.fadeIn();
                            if (!isLoadedFomCache)
                                fetchImageTask.notFromCache();
                            if (!isSavedToCache)
                                fetchImageTask.notToCache();
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
