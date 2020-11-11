package nl.plaatsoft.bassietest;

import android.content.Context;
import android.os.Looper;
import android.os.Handler;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FetchDataTask {
    private static final Executor executor = Executors.newFixedThreadPool(8);
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static interface OnLoadListener {
        public abstract void onLoad(String data);
    }

    private final Context context;
    private final String url;
    private boolean loadFomCache = false;
    private boolean saveToCache = false;
    private OnLoadListener onLoadListener = null;
    private boolean finished = false;
    private boolean canceled = false;

    public FetchDataTask(Context context, String url) {
        this.context = context;
        this.url = url;
        execute();
    }

    public FetchDataTask(Context context, String url, boolean loadFomCache, boolean saveToCache) {
        this.context = context;
        this.url = url;
        this.loadFomCache = loadFomCache;
        this.saveToCache = saveToCache;
        execute();
    }

    public FetchDataTask(Context context, String url, OnLoadListener onLoadListener) {
        this.context = context;
        this.url = url;
        this.onLoadListener = onLoadListener;
        execute();
    }

    public FetchDataTask(Context context, String url, boolean loadFomCache, boolean saveToCache, OnLoadListener onLoadListener) {
        this.context = context;
        this.url = url;
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
        // Fetch the data in another thread
        executor.execute(() -> {
            String data = fetchData();

            // Send the onLoad event on the UI thread when not canceled
            if (!canceled) {
                handler.post(() -> {
                    finished = true;
                    if (onLoadListener != null) {
                        onLoadListener.onLoad(data);
                    }
                });
            }
        });
    }

    private String fetchData() {
        try {
            // Check if the url is already cached
            File file = new File(context.getCacheDir(), Utils.md5(url));
            if (loadFomCache && file.exists()) {
                // Then read the cached file
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append(System.lineSeparator());
                }
                bufferedReader.close();
                return stringBuilder.toString();
            }

            // Or fetch the data from the internet
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(System.lineSeparator());
            }
            bufferedReader.close();

            // And write to a cache file when needed
            String data = stringBuilder.toString();
            if (saveToCache) {
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(data);
                fileWriter.close();
            }
            return data;
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }
}
