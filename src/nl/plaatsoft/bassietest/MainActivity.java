package nl.plaatsoft.bassietest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONObject;

public class MainActivity extends BaseActivity {
    public static final int SETTINGS_REQUEST_CODE = 1;

    private int oldLanguage = -1;
    private int oldTheme = -1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((ImageView)findViewById(R.id.main_settings_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                oldLanguage = settings.getInt("language", SettingsActivity.LANGUAGE_DEFAULT);
                oldTheme = settings.getInt("theme", SettingsActivity.THEME_DEFAULT);
                startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), MainActivity.SETTINGS_REQUEST_CODE);
            }
        });

        new FetchImageTask(this, (ImageView)findViewById(R.id.main_random_image), "https://source.unsplash.com/random", false, false);

        new FetchDataTask(this, "https://ipinfo.io/json", false, false, new FetchDataTask.OnLoadListener() {
            public void onLoad(String data) {
                try {
                    JSONObject jsondata = new JSONObject(data);

                    ((TextView)findViewById(R.id.main_location_label)).setText(jsondata.getString("city") + ", " + jsondata.getString("region"));
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.SETTINGS_REQUEST_CODE) {
            if (oldLanguage != -1 && oldTheme != -1) {
                if (
                    oldLanguage != settings.getInt("language", SettingsActivity.LANGUAGE_DEFAULT) ||
                    oldTheme != settings.getInt("theme", SettingsActivity.THEME_DEFAULT)
                ) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            recreate();
                        }
                    });
                }
            }
        }
    }
}
