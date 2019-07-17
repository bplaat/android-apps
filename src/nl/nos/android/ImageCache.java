package nl.nos.android;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.LruCache;

class ImageCache extends LruCache<String, Bitmap> {
    private static ImageCache instance = null;

    private ImageCache(int maxSize) {
        super(maxSize);
    }

    public static ImageCache getInstance() {
        if (instance == null) {
            instance = new ImageCache(32 * 1024 * 1024);
        }
        return instance;
    }

    protected int sizeOf(String url, Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= 19) {
            return bitmap.getAllocationByteCount();
        } else {
            return bitmap.getByteCount();
        }
    }
}
