package nl.plaatsoft.bassietest.activities;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import org.json.JSONObject;

import nl.plaatsoft.bassietest.components.RatingAlert;
import nl.plaatsoft.bassietest.tasks.FetchDataTask;
import nl.plaatsoft.bassietest.tasks.FetchImageTask;
import nl.plaatsoft.bassietest.Consts;
import nl.plaatsoft.bassietest.R;

public class MainActivity extends BaseActivity implements PopupMenu.OnMenuItemClickListener {
    public static final int SETTINGS_REQUEST_CODE = 1;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private ViewSwitcher pageSwitcher;
    private int oldLanguage = -1;
    private int oldTheme = -1;
    private boolean imageLoaded = false;
    private boolean infoLoaded = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pageSwitcher = findViewById(R.id.main_page_switcher);

        // Options menu button
        findViewById(R.id.main_options_menu_button).setOnClickListener(view -> {
            PopupMenu optionsMenu = new PopupMenu(this, view, Gravity.TOP | Gravity.RIGHT);
            optionsMenu.getMenuInflater().inflate(R.menu.options, optionsMenu.getMenu());
            optionsMenu.setOnMenuItemClickListener(this);
            optionsMenu.show();
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
                        var jsonData = new JSONObject(new String(data, "UTF-8"));
                        TextView locationLabel = findViewById(R.id.main_data_location_label);
                        locationLabel.setText(jsonData.getString("city") + ", " + jsonData.getString("region"));

                        var set = (AnimatorSet)AnimatorInflater.loadAnimator(this, R.animator.text_fade_in);
                        set.setTarget(locationLabel);
                        set.start();
                    } catch (Exception exception) {
                        Log.e(getPackageName(), "Can't parse IP info", exception);
                    }
                }).fetch();
            }
        });

        RatingAlert.updateAndShow(this, Consts.STORE_PAGE_URL);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.menu_options_settings) {
            oldLanguage = settings.getInt("language", Consts.Settings.LANGUAGE_DEFAULT);
            oldTheme = settings.getInt("theme", Consts.Settings.THEME_DEFAULT);
            startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_REQUEST_CODE);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // When settings activity is closed check for restart
        if (requestCode == SETTINGS_REQUEST_CODE) {
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

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (pageSwitcher.getDisplayedChild() == 1) {
            pageSwitcher.showPrevious();
        } else {
            super.onBackPressed();
        }
    }
}
