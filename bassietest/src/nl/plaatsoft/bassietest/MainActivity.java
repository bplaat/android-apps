package nl.plaatsoft.bassietest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ScrollView;
import org.json.JSONObject;

public class MainActivity extends BaseActivity {
    public static final int SETTINGS_REQUEST_CODE = 1;

    private Handler handler = new Handler(Looper.getMainLooper());

    private ScrollView landingPage;
    private ScrollView dataPage;

    private int oldLanguage = -1;
    private int oldTheme = -1;
    private boolean imageLoaded = false;
    private boolean infoLoaded = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init settings button
        ((ImageButton)findViewById(R.id.main_settings_button)).setOnClickListener(view -> {
            oldLanguage = settings.getInt("language", Config.SETTINGS_LANGUAGE_DEFAULT);
            oldTheme = settings.getInt("theme", Config.SETTINGS_THEME_DEFAULT);
            startActivityForResult(new Intent(this, SettingsActivity.class), MainActivity.SETTINGS_REQUEST_CODE);
        });

        landingPage = (ScrollView)findViewById(R.id.main_landing_page);
        dataPage = (ScrollView)findViewById(R.id.main_data_page);

        // Init landing action button
        ((Button)findViewById(R.id.main_landing_hero_button)).setOnClickListener(view -> {
            Utils.fadeInOut(landingPage, dataPage);

            // Fetch random Unsplash image
            if (!imageLoaded) {
                FetchImageTask.with(this)
                    .load("https://source.unsplash.com/collection/141706/" + Utils.convertDpToPixel(this, 320) + "x" + Utils.convertDpToPixel(this, 240))
                    .noCache()
                    .then(image -> {
                        imageLoaded = true;
                    })
                    .fadeIn()
                    .into((ImageView)findViewById(R.id.main_data_random_image))
                    .fetch();
            }

            // Fetch IP information
            if (!infoLoaded) {
                FetchDataTask.with(this).load("https://ipinfo.io/json").then(data -> {
                    try {
                        JSONObject jsondata = new JSONObject(data);

                        TextView locationLabel = (TextView)findViewById(R.id.main_data_location_label);
                        Utils.fadeInTextView(this, locationLabel);
                        locationLabel.setText(jsondata.getString("city") + ", " + jsondata.getString("region"));

                        infoLoaded = true;
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }).fetch();
            }
        });

        // Everytime the main activity is created update / show rating alert
        RatingAlert.updateAndShow(this);
    }

    // When come back of the settings activity check for restart
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.SETTINGS_REQUEST_CODE) {
            if (oldLanguage != -1 && oldTheme != -1) {
                if (
                    oldLanguage != settings.getInt("language", Config.SETTINGS_LANGUAGE_DEFAULT) ||
                    oldTheme != settings.getInt("theme", Config.SETTINGS_THEME_DEFAULT)
                ) {
                    handler.post(() -> {
                        recreate();
                    });
                }
            }
        }
    }

    // When back button press go back to landing page
    public void onBackPressed() {
        if (dataPage.getVisibility() == View.VISIBLE) {
            Utils.fadeInOut(dataPage, landingPage);
        } else {
            super.onBackPressed();
        }
    }
}
