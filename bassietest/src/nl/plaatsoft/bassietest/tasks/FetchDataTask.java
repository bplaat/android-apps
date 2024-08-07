package nl.plaatsoft.bassietest.tasks;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FetchDataTask {
    public static interface OnLoadListener {
        public abstract void onLoad(byte[] data);
    }
    public static interface OnErrorListener {
        public abstract void onError(Exception exception);
    }

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static final Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final Context context;
    private String url;
    private boolean isLoadedFomCache = false;
    private boolean isSavedToCache = false;
    private OnLoadListener onLoadListener = null;
    private OnErrorListener onErrorListener = null;
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

    public FetchDataTask loadFromCache() {
        isLoadedFomCache = true;
        return this;
    }

    public FetchDataTask saveToCache() {
        isSavedToCache = true;
        return this;
    }

    public FetchDataTask noCache() {
        isLoadedFomCache = false;
        isSavedToCache = false;
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
        return isLoaded;
    }

    public FetchDataTask fetch() {
        executor.execute(() -> {
            try {
                isPending = true;
                var data = fetchData(context, url, isLoadedFomCache, isSavedToCache);
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

    public static byte[] fetchData(Context context, String url, boolean isLoadedFomCache, boolean isSavedToCache) throws Exception {
        // Get MD5 hash of url
        var messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update(url.getBytes());
        var hashBytes = messageDigest.digest();
        var hashStringBuilder = new StringBuilder();
        for (int i = 0; i < hashBytes.length; i++)
            hashStringBuilder.append(String.format("%02x", hashBytes[i]));
        var urlHash = hashStringBuilder.toString();

        // Try to load url from cache
        File cacheFile = new File(context.getCacheDir(), urlHash);
        if (isLoadedFomCache && cacheFile.exists()) {
            var fileInputStream = new FileInputStream(cacheFile);
            var byteArrayOutputStream = new ByteArrayOutputStream();
            var buffer = new byte[1024];
            int bytesRead = 0;
            while ((bytesRead = fileInputStream.read(buffer, 0, buffer.length)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            byteArrayOutputStream.close();
            fileInputStream.close();
            return byteArrayOutputStream.toByteArray();
        }

        // Load url from network
        var bufferedInputStream = new BufferedInputStream(new URL(url).openStream());
        var byteArrayOutputStream = new ByteArrayOutputStream();
        var buffer = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = bufferedInputStream.read(buffer, 0, buffer.length)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        byteArrayOutputStream.close();
        bufferedInputStream.close();
        var data = byteArrayOutputStream.toByteArray();

        // Save data to cache
        if (isSavedToCache) {
            var fileOutputStream = new FileOutputStream(cacheFile);
            fileOutputStream.write(data);
            fileOutputStream.close();
        }
        return data;
    }
}
