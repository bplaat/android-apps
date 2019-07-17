package nl.nos.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import java.net.URL;

public class FetchImageTask extends AsyncTask<Void, Void, Bitmap> {
    private ImageView imageView;
    private String url;

    public FetchImageTask(ImageView imageView, String url) {
        this.imageView = imageView;
        this.url = url;
        imageView.setTag(url);
        imageView.setImageBitmap(null);
    }

    public Bitmap doInBackground(Void... voids) {
        try {
            ImageCache imageCache = ImageCache.getInstance();
            Bitmap bitmap = imageCache.get(url);
            if (bitmap == null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                bitmap = BitmapFactory.decodeStream(new URL(url).openStream(), null, options);
                imageCache.put(url, bitmap);
            }
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    public void onPostExecute(Bitmap image) {
        if ((String)imageView.getTag() == url) {
            imageView.setImageBitmap(image);
        }
    }
}
