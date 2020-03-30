package nl.plaatsoft.redsquare.android;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class FetchDataTask extends AsyncTask<Void, Void, String> {
    public interface OnLoadListener {
        public void onLoad(String data);
    }

    private String url;
    private OnLoadListener onLoadListener;

    public FetchDataTask(String url, OnLoadListener onLoadListener) {
        this.url = url;
        this.onLoadListener = onLoadListener;
    }

    public String doInBackground(Void... voids) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(System.lineSeparator());
            }
            bufferedReader.close();
            return stringBuilder.toString();
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public void onPostExecute(String data) {
        if (!isCancelled() && onLoadListener != null) {
            onLoadListener.onLoad(data);
        }
    }
}
