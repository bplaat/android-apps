/*
 * Copyright (c) 2020-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.fetch;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.annotation.Nullable;

public class FetchDataTask {
    public static interface OnLoadListener {
        void onLoad(byte[] data);
    }

    public static interface OnErrorListener {
        void onError(Exception exception);
    }

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static final Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final Context context;
    private @Nullable String url;
    private boolean isLoadedFomCache = false;
    private boolean isSavedToCache = false;
    private @Nullable OnLoadListener onLoadListener;
    private @Nullable OnErrorListener onErrorListener;
    private boolean isPending = false;
    private boolean isLoaded = false;
    private boolean isError = false;

    private FetchDataTask(Context context) {
        this.context = context;
    }

    public static FetchDataTask with(Context context) {
        return new FetchDataTask(context);
    }

    public FetchDataTask load(String url) {
        this.url = url;
        return this;
    }

    public FetchDataTask withCache() {
        isLoadedFomCache = true;
        isSavedToCache = true;
        return this;
    }

    public FetchDataTask noCache() {
        isLoadedFomCache = false;
        isSavedToCache = false;
        return this;
    }

    public FetchDataTask loadFromCache(boolean isLoadedFomCache) {
        this.isLoadedFomCache = isLoadedFomCache;
        return this;
    }

    public FetchDataTask saveToCache(boolean isSavedToCache) {
        this.isSavedToCache = isSavedToCache;
        return this;
    }

    public FetchDataTask then(OnLoadListener onLoadListener) {
        this.onLoadListener = onLoadListener;
        return this;
    }

    public FetchDataTask then(OnLoadListener onLoadListener, OnErrorListener onErrorListener) {
        this.onLoadListener = onLoadListener;
        this.onErrorListener = onErrorListener;
        return this;
    }

    public boolean isPending() {
        return isPending;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public boolean isError() {
        return isError;
    }

    public FetchDataTask fetch() {
        executor.execute(() -> {
            try {
                isPending = true;
                var data = fetchData(context, new URI(Objects.requireNonNull(url)), isLoadedFomCache, isSavedToCache);
                handler.post(() -> {
                    isPending = false;
                    isLoaded = true;
                    if (onLoadListener != null)
                        onLoadListener.onLoad(data);
                });
            } catch (Exception exception) {
                handler.post(() -> {
                    isPending = false;
                    isError = true;
                    if (onErrorListener != null) {
                        onErrorListener.onError(exception);
                    } else {
                        Log.e(context.getPackageName(), "Can't fetch data", exception);
                    }
                });
            }
        });
        return this;
    }

    public static byte[] fetchData(Context context, URI uri, boolean isLoadedFomCache, boolean isSavedToCache)
            throws Exception {
        // Get MD5 hash of url
        var messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update(uri.toString().getBytes());
        var hashBytes = messageDigest.digest();
        var hashStringBuilder = new StringBuilder();
        for (var i = 0; i < hashBytes.length; i++)
            hashStringBuilder.append(String.format("%02x", hashBytes[i]));
        var urlHash = hashStringBuilder.toString();

        // Try to load url from cache
        var cacheFile = new File(context.getCacheDir(), urlHash);
        if (isLoadedFomCache && cacheFile.exists()) {
            var fileInputStream = new FileInputStream(cacheFile);
            var byteArrayOutputStream = new ByteArrayOutputStream();
            try (fileInputStream; byteArrayOutputStream) {
                var buffer = new byte[1024];
                var bytesRead = 0;
                while ((bytesRead = fileInputStream.read(buffer, 0, buffer.length)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
            }
            return byteArrayOutputStream.toByteArray();
        }

        // Load url from network
        var bufferedInputStream = new BufferedInputStream(uri.toURL().openStream());
        var byteArrayOutputStream = new ByteArrayOutputStream();
        try (bufferedInputStream; byteArrayOutputStream) {
            var buffer = new byte[1024];
            var bytesRead = 0;
            while ((bytesRead = bufferedInputStream.read(buffer, 0, buffer.length)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
        }
        var data = byteArrayOutputStream.toByteArray();

        // Save data to cache
        if (isSavedToCache) {
            try (var fileOutputStream = new FileOutputStream(cacheFile)) {
                fileOutputStream.write(data);
            }
        }
        return data;
    }
}
