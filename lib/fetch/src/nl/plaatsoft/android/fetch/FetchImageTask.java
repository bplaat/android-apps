/*
 * Copyright (c) 2020-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.android.fetch;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LruCache;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import org.jspecify.annotations.Nullable;

public class FetchImageTask {
    public static interface OnLoadListener {
        void onLoad(Bitmap image);
    }

    public static interface OnErrorListener {
        void onError(Exception exception);
    }

    private static final int ANIMATION_IMAGE_LOADING_TIMEOUT = 50;

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static final Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static final LruCache<String, Bitmap> bitmapCache = new LruCache<String, Bitmap>(
            (int) (Runtime.getRuntime().freeMemory() / 4)) {
        @Override
        protected int sizeOf(@SuppressWarnings("null") String url, @SuppressWarnings("null") Bitmap bitmap) {
            return bitmap.getByteCount();
        }
    };

    private final Context context;
    private @Nullable String url;
    private boolean isTransparent = false;
    private boolean isFadedIn = false;
    private int loadingColor = Color.TRANSPARENT;
    private int animationDuration = 200;
    private boolean isLoadedFomCache = true;
    private boolean isSavedToCache = true;
    private @Nullable OnLoadListener onLoadListener;
    private @Nullable OnErrorListener onErrorListener;
    private @Nullable ImageView imageView;
    private boolean isPending = false;
    private boolean isLoaded = false;
    private boolean isError = false;

    private FetchImageTask(Context context) {
        this.context = context;
    }

    public static FetchImageTask with(Context context) {
        return new FetchImageTask(context);
    }

    public FetchImageTask load(String url) {
        this.url = url;
        return this;
    }

    public FetchImageTask transparent() {
        this.isTransparent = true;
        return this;
    }

    public FetchImageTask fadeIn() {
        this.isFadedIn = true;
        return this;
    }

    public FetchImageTask withCache() {
        isLoadedFomCache = true;
        isSavedToCache = true;
        return this;
    }

    public FetchImageTask noCache() {
        isLoadedFomCache = false;
        isSavedToCache = false;
        return this;
    }

    public FetchImageTask loadFromCache(boolean isLoadedFomCache) {
        this.isLoadedFomCache = isLoadedFomCache;
        return this;
    }

    public FetchImageTask saveToCache(boolean isSavedToCache) {
        this.isSavedToCache = isSavedToCache;
        return this;
    }

    public FetchImageTask loadingColor(int loadingColor) {
        this.loadingColor = loadingColor;
        return this;
    }

    public FetchImageTask animationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
        return this;
    }

    public FetchImageTask then(OnLoadListener onLoadListener) {
        this.onLoadListener = onLoadListener;
        return this;
    }

    public FetchImageTask then(OnLoadListener onLoadListener, OnErrorListener onErrorListener) {
        this.onLoadListener = onLoadListener;
        this.onErrorListener = onErrorListener;
        return this;
    }

    public FetchImageTask into(ImageView imageView) {
        this.imageView = imageView;
        return this;
    }

    public @Nullable String getUrl() {
        return url;
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

    public FetchImageTask fetch() {
        if (imageView != null) {
            imageView.setTag(this);
            Objects.requireNonNull(imageView).setImageBitmap(null);
        }

        var startTime = System.currentTimeMillis();
        if (bitmapCache.get(url) != null) {
            onLoad(bitmapCache.get(url), startTime);
            return this;
        }

        if (imageView != null && isFadedIn && isTransparent && loadingColor != Color.TRANSPARENT)
            imageView.setBackgroundColor(loadingColor);

        executor.execute(() -> {
            try {
                var image = fetchImage(new URI(Objects.requireNonNull(url)));
                if (bitmapCache.get(url) == null)
                    bitmapCache.put(url, image);
                handler.post(() -> onLoad(image, startTime));
            } catch (Exception exception) {
                handler.post(() -> {
                    isPending = false;
                    isError = true;
                    if (onErrorListener != null) {
                        onErrorListener.onError(exception);
                    } else {
                        Log.e(context.getPackageName(), "Can't fetch or decode image", exception);
                    }
                });
            }
        });
        return this;
    }

    public Bitmap fetchImage(URI uri) throws Exception {
        var options = new BitmapFactory.Options();
        options.inPreferredConfig = isTransparent ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        if (uri.getScheme().equals("content")) {
            return BitmapFactory.decodeStream(
                    context.getContentResolver().openInputStream(Uri.parse(uri.toString())),
                    null, options);
        } else {
            var data = FetchDataTask.fetchData(context, uri, isLoadedFomCache, isSavedToCache);
            return BitmapFactory.decodeByteArray(data, 0, data.length, options);
        }
    }

    public void onLoad(Bitmap image, long startTime) {
        isPending = false;
        isLoaded = true;
        if (imageView != null) {
            var imageViewTask = (FetchImageTask) imageView.getTag();
            if (Objects.requireNonNull(url).equals(imageViewTask.getUrl())) {
                Objects.requireNonNull(imageView).setImageBitmap(image);
                if (isFadedIn && (System.currentTimeMillis() - startTime) > ANIMATION_IMAGE_LOADING_TIMEOUT) {
                    if (isTransparent && loadingColor != Color.TRANSPARENT) {
                        var backgroundColorAnimation = ValueAnimator
                                .ofArgb(((ColorDrawable) Objects.requireNonNull(imageView).getBackground()).getColor(),
                                        Color.TRANSPARENT);
                        backgroundColorAnimation.setDuration(animationDuration);
                        backgroundColorAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                        backgroundColorAnimation.addUpdateListener(animator -> {
                            Objects.requireNonNull(imageView)
                                    .setBackgroundColor((int) backgroundColorAnimation.getAnimatedValue());
                        });
                        backgroundColorAnimation.start();
                    }

                    var alphaAnimation = ValueAnimator.ofInt(0, 255);
                    alphaAnimation.setDuration(animationDuration);
                    alphaAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
                    alphaAnimation.addUpdateListener(animator -> {
                        Objects.requireNonNull(imageView).setImageAlpha((int) alphaAnimation.getAnimatedValue());
                    });
                    Objects.requireNonNull(imageView).setImageAlpha(0);
                    alphaAnimation.start();
                } else if (isTransparent && loadingColor != Color.TRANSPARENT) {
                    Objects.requireNonNull(imageView).setBackgroundColor(Color.TRANSPARENT);
                }
            }
        }
        if (onLoadListener != null)
            onLoadListener.onLoad(image);
    }
}
