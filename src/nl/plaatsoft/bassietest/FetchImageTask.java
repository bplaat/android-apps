package nl.plaatsoft.bassietest;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.os.Handler;
import android.widget.ImageView;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FetchImageTask {
    private static final Executor executor = Executors.newFixedThreadPool(8);
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static interface OnLoadListener {
        public abstract void onLoad(Bitmap image);
    }

    private final Context context;
    private final ImageView imageView;
    private final String url;
    private boolean fadeIn = true;
    private boolean loadFomCache = true;
    private boolean saveToCache = true;
    private OnLoadListener onLoadListener = null;
    private boolean finished = false;
    private boolean canceled = false;

    public FetchImageTask(Context context, ImageView imageView, String url) {
        this.context = context;
        this.imageView = imageView;
        this.url = url;
        execute();
    }

    public FetchImageTask(Context context, ImageView imageView, String url, boolean fadeIn, boolean loadFomCache, boolean saveToCache) {
        this.context = context;
        this.imageView = imageView;
        this.url = url;
        this.fadeIn = fadeIn;
        this.loadFomCache = loadFomCache;
        this.saveToCache = saveToCache;
        execute();
    }

    public FetchImageTask(Context context, ImageView imageView, String url, OnLoadListener onLoadListener) {
        this.context = context;
        this.imageView = imageView;
        this.url = url;
        this.onLoadListener = onLoadListener;
        execute();
    }

    public FetchImageTask(Context context, ImageView imageView, String url, boolean fadeIn, boolean loadFomCache, boolean saveToCache, OnLoadListener onLoadListener) {
        this.context = context;
        this.imageView = imageView;
        this.url = url;
        this.fadeIn = fadeIn;
        this.loadFomCache = loadFomCache;
        this.saveToCache = saveToCache;
        this.onLoadListener = onLoadListener;
        execute();
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void cancel() {
        canceled = true;
    }

    private void execute() {
        if (!(imageView.getTag() != null ? (String)imageView.getTag() : "").equals(url)) {
            imageView.setTag(url);
            imageView.setImageBitmap(null);

            // Fetch the image an another thread
            executor.execute(() -> {
                Bitmap image = fetchImage();

                // Set the image on the UI thread when not canceled and run send onLoad event
                if (!canceled) {
                    handler.post(() -> {
                        finished = true;

                        if ((imageView.getTag() != null ? (String)imageView.getTag() : "").equals(url)) {
                            // When fading in set image alpha to zero
                            if (fadeIn) {
                                imageView.setImageAlpha(0);
                            }

                            imageView.setImageBitmap(image);

                            // And animate the property to full opaque
                            if (fadeIn) {
                                ValueAnimator animation = ValueAnimator.ofObject(new IntEvaluator(), 0, 255);
                                animation.addUpdateListener((ValueAnimator animator) -> {
                                    imageView.setImageAlpha((int)animator.getAnimatedValue());
                                });
                                animation.setDuration(Config.ANIMATION_FADE_IN_DURATION);
                                animation.start();
                            }
                        }

                        if (onLoadListener != null) {
                            onLoadListener.onLoad(image);
                        }
                    });
                }
            });
        } else {
            finished = true;
        }
    }

    private Bitmap fetchImage() {
        try {
            // Check if the file exists in the cache
            File file = new File(context.getCacheDir(), Utils.md5(url));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            if (loadFomCache && file.exists()) {
                // The read it
                return BitmapFactory.decodeFile(file.getPath(), options);
            }

            // Or fetch the image from the internet in to a byte array buffer
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new URL(url).openStream());
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int number_read = 0;
            while ((number_read = bufferedInputStream.read(buffer, 0, buffer.length)) != -1) {
                byteArrayOutputStream.write(buffer, 0, number_read);
            }
            byteArrayOutputStream.close();
            bufferedInputStream.close();

            byte[] image = byteArrayOutputStream.toByteArray();

            // When needed save the image to a cache file
            if (saveToCache) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(image);
                fileOutputStream.close();
            }

            return BitmapFactory.decodeByteArray(image, 0, image.length, options);
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }
}
