package nl.nos.android;

import android.content.Context;
import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.Base64;

public class FetchDataTask extends AsyncTask<Void, Void, String> {
    public interface OnLoadListener {
        public abstract void onLoad(String data);
    }

    private Context context;
    private String url;
    private boolean loadFomCache;
    private boolean saveToCache;
    private OnLoadListener onLoadListener;

    public FetchDataTask(Context context, String url, boolean loadFomCache, boolean saveToCache, OnLoadListener onLoadListener) {
        this.context = context;
        this.url = url;
        this.loadFomCache = loadFomCache;
        this.saveToCache = saveToCache;
        this.onLoadListener = onLoadListener;
    }

    public String doInBackground(Void... voids) {
        try {
            File file = new File(context.getCacheDir(), new String(Base64.getUrlEncoder().encode(url.getBytes())));
            if (loadFomCache && file.exists()) {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append(System.lineSeparator());
                }
                bufferedReader.close();
                return stringBuilder.toString();
            } else {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append(System.lineSeparator());
                }
                bufferedReader.close();
                String data = stringBuilder.toString();
                if (saveToCache) {
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(data);
                    fileWriter.close();
                }
                return data;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public void onPostExecute(String data) {
        if (!isCancelled()) {
            onLoadListener.onLoad(data);
        }
    }
}
