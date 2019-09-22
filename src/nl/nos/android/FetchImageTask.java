package nl.nos.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Base64;

public class FetchImageTask extends AsyncTask<Void, Void, Bitmap> {
    private Context context;
    private ImageView imageView;
    private String url;

    private FetchImageTask(Context context, ImageView imageView, String url) {
        this.context = context;
        this.imageView = imageView;
        this.url = url;
        imageView.setTag(url);
        imageView.setImageBitmap(null);
    }

    public static void fetchImage(Context context, ImageView imageView, String url) {
        Bitmap bitmap = ImageMemoryCache.getInstance().get(url);
        if (bitmap != null) {
            imageView.setTag(url);
            imageView.setImageBitmap(bitmap);
        } else {
            new FetchImageTask(context, imageView, url).execute();
        }
    }

    public Bitmap doInBackground(Void... voids) {
        try {
            File file = new File(context.getCacheDir(), new String(Base64.getUrlEncoder().encode(url.getBytes())));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), options);
                ImageMemoryCache.getInstance().put(url, bitmap);
                return bitmap;
            } else {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(new URL(url).openStream());
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int nRead = 0;
                while ((nRead = bufferedInputStream.read(buffer, 0, buffer.length)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, nRead);
                }
                byteArrayOutputStream.close();
                bufferedInputStream.close();

                byte[] image = byteArrayOutputStream.toByteArray();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(image);
                fileOutputStream.close();

                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length, options);
                ImageMemoryCache.getInstance().put(url, bitmap);
                return bitmap;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public void onPostExecute(Bitmap image) {
        if (!isCancelled() && (String)imageView.getTag() == url) {
            imageView.setImageBitmap(image);
        }
    }
}
