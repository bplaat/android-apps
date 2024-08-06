package nl.plaatsoft.bassietest.activities;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import org.json.JSONObject;

import nl.plaatsoft.bassietest.components.RatingAlert;
import nl.plaatsoft.bassietest.tasks.FetchDataTask;
import nl.plaatsoft.bassietest.tasks.FetchImageTask;
import nl.plaatsoft.bassietest.Consts;
import nl.plaatsoft.bassietest.R;

public class MainActivity extends BaseActivity {
    public static final int SETTINGS_REQUEST_CODE = 1;

    private Handler handler = new Handler(Looper.getMainLooper());
    private ViewSwitcher pageSwitcher;
    private int oldLanguage = -1;
    private int oldTheme = -1;
    private boolean imageLoaded = false;
    private boolean infoLoaded = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pageSwitcher = findViewById(R.id.main_page_switcher);

        // Settings button
        findViewById(R.id.main_settings_button).setOnClickListener(view -> {
            oldLanguage = settings.getInt("language", Consts.Settings.LANGUAGE_DEFAULT);
            oldTheme = settings.getInt("theme", Consts.Settings.THEME_DEFAULT);
            startActivityForResult(new Intent(this, SettingsActivity.class), MainActivity.SETTINGS_REQUEST_CODE);
        });

        // Landing action button
        findViewById(R.id.main_landing_hero_button).setOnClickListener(view -> {
            pageSwitcher.showNext();

            // Fetch random Unsplash image
            if (!imageLoaded) {
                imageLoaded = true;
                var scale = getResources().getDisplayMetrics().density;
                FetchImageTask.with(this)
                    .load("https://picsum.photos/" + (int)(320 * scale) + "/" + (int)(240 * scale))
                    .noCache()
                    .fadeIn()
                    .into(findViewById(R.id.main_data_random_image))
                    .fetch();
            }

            // Fetch IP info
            if (!infoLoaded) {
                infoLoaded = true;
                FetchDataTask.with(this).load("https://ipinfo.io/json").then(data -> {
                    try {
                        var jsonData = new JSONObject(new String(data));
                        TextView locationLabel = findViewById(R.id.main_data_location_label);
                        locationLabel.setText(jsonData.getString("city") + ", " + jsonData.getString("region"));

                        var set = (AnimatorSet)AnimatorInflater.loadAnimator(this, R.animator.text_fade_in);
                        set.setTarget(locationLabel);
                        set.start();
                    } catch (Exception exception) {
                        Log.e(Consts.LOG_TAG, "Can't parse IP info", exception);
                    }
                }).fetch();
            }
        });

        RatingAlert.updateAndShow(this);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // When settings activity is closed check for restart
        if (requestCode == MainActivity.SETTINGS_REQUEST_CODE) {
            if (oldLanguage != -1 && oldTheme != -1) {
                if (
                    oldLanguage != settings.getInt("language", Consts.Settings.LANGUAGE_DEFAULT) ||
                    oldTheme != settings.getInt("theme", Consts.Settings.THEME_DEFAULT)
                ) {
                    handler.post(() -> recreate());
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (pageSwitcher.getDisplayedChild() == 1) {
            pageSwitcher.showPrevious();
        } else {
            super.onBackPressed();
        }
    }
}
