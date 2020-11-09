package nl.plaatsoft.bassietest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
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

        // Init settings button
        ((ImageButton)findViewById(R.id.main_settings_button)).setOnClickListener((View view) -> {
            oldLanguage = settings.getInt("language", Config.SETTINGS_LANGUAGE_DEFAULT);
            oldTheme = settings.getInt("theme", Config.SETTINGS_THEME_DEFAULT);
            startActivityForResult(new Intent(this, SettingsActivity.class), MainActivity.SETTINGS_REQUEST_CODE);
        });

        // Fetch random Unsplash image
        new FetchImageTask(this, (ImageView)findViewById(R.id.main_random_image), "https://source.unsplash.com/random", false, false);

        // Fetch IP information
        new FetchDataTask(this, "https://ipinfo.io/json", false, false, (String data) -> {
            try {
                JSONObject jsondata = new JSONObject(data);

                ((TextView)findViewById(R.id.main_location_label)).setText(jsondata.getString("city") + ", " + jsondata.getString("region"));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    // Everytime the main activity opens update / show rate alert
    public void onResume() {
        super.onResume();
        RatingAlert.show(this);
    }

    // When come back of the settings activity check for restart
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.SETTINGS_REQUEST_CODE) {
            if (oldLanguage != -1 && oldTheme != -1) {
                if (
                    oldLanguage != settings.getInt("language", Config.SETTINGS_LANGUAGE_DEFAULT) ||
                    oldTheme != settings.getInt("theme", Config.SETTINGS_THEME_DEFAULT)
                ) {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> {
                        recreate();
                    });
                }
            }
        }
    }
}
