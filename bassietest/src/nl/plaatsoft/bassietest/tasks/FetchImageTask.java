package nl.plaatsoft.bassietest.tasks;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Looper;
import android.os.Handler;
import android.util.Log;
import android.util.LruCache;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import nl.plaatsoft.bassietest.R;

public class FetchImageTask {
    public static interface OnLoadListener {
        public abstract void onLoad(Bitmap image);
    }
    public static interface OnErrorListener {
        public abstract void onError(Exception exception);
    }

    private static final int ANIMATION_IMAGE_LOADING_TIMEOUT = 50;

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static final Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static final LruCache<String, Bitmap> bitmapCache = new LruCache<String, Bitmap>((int)(Runtime.getRuntime().freeMemory() / 4)) {
        @Override
        protected int sizeOf(String url, Bitmap bitmap) {
            return bitmap.getByteCount();
        }
    };

    private final Context context;
    private String url;
    private boolean isTransparent = false;
    private boolean isFadedIn = false;
    private boolean isLoadedFomCache = true;
    private boolean isSavedToCache = true;
    private OnLoadListener onLoadListener = null;
    private OnErrorListener onErrorListener = null;
    private ImageView imageView;
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

    public FetchImageTask loadFromCache() {
        isLoadedFomCache = true;
        return this;
    }

    public FetchImageTask saveToCache() {
        isSavedToCache = true;
        return this;
    }

    public FetchImageTask noCache() {
        isLoadedFomCache = false;
        isSavedToCache = false;
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

    public String getUrl() {
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
            imageView.setImageBitmap(null);
        }

        long startTime = System.currentTimeMillis();
        if (bitmapCache.get(url) != null) {
            onLoad(bitmapCache.get(url), startTime);
            return this;
        }

        executor.execute(() -> {
            try {
                var data = FetchDataTask.fetchData(context, url, isLoadedFomCache, isSavedToCache);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = isTransparent ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
                var image = BitmapFactory.decodeByteArray(data, 0, data.length, options);
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

    public void onLoad(Bitmap image, long startTime) {
        isPending = false;
        isLoaded = true;
        if (imageView != null) {
            FetchImageTask imageViewTask = (FetchImageTask)imageView.getTag();
            if (url.equals(imageViewTask.getUrl())) {
                if (isTransparent)
                    imageView.setBackgroundColor(Color.TRANSPARENT);

                boolean fadeIn = isFadedIn && (System.currentTimeMillis() - startTime) > ANIMATION_IMAGE_LOADING_TIMEOUT;
                if (fadeIn)
                    imageView.setImageAlpha(0);
                imageView.setImageBitmap(image);

                if (fadeIn) {
                    ValueAnimator animation = ValueAnimator.ofInt(0, 255);
                    animation.setDuration(context.getResources().getInteger(R.integer.animation_duration));
                    animation.setInterpolator(new AccelerateDecelerateInterpolator());
                    animation.addUpdateListener(animator -> {
                        imageView.setImageAlpha((int)animator.getAnimatedValue());
                    });
                    animation.start();
                }
            }
        }
        if (onLoadListener != null)
            onLoadListener.onLoad(image);
    }
}
