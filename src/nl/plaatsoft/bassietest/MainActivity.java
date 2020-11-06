package nl.plaatsoft.bassietest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends BaseActivity {
    public static final int SETTINGS_REQUEST_CODE = 1;

    public int oldLanguage = -1;
    public int oldTheme = -1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences settings = getSharedPreferences("settings", Context.MODE_PRIVATE);

        ((ImageView)findViewById(R.id.main_settings_button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                oldLanguage = settings.getInt("language", SettingsActivity.LANGUAGE_DEFAULT);
                oldTheme = settings.getInt("theme", SettingsActivity.THEME_DEFAULT);
                startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), MainActivity.SETTINGS_REQUEST_CODE);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.SETTINGS_REQUEST_CODE) {
            if (oldLanguage != -1 && oldTheme != -1) {
                SharedPreferences settings = getSharedPreferences("settings", Context.MODE_PRIVATE);
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
