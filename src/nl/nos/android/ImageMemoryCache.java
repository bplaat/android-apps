package nl.nos.android;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.LruCache;

class ImageMemoryCache extends LruCache<String, Bitmap> {
    private static ImageMemoryCache instance = null;

    private ImageMemoryCache(int maxSize) {
        super(maxSize);
    }

    public static ImageMemoryCache getInstance() {
        if (instance == null) {
            instance = new ImageMemoryCache((int)(Runtime.getRuntime().maxMemory() / 4));
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
