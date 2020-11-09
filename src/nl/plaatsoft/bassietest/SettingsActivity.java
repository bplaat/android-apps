package nl.plaatsoft.bassietest;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsActivity extends BaseActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set settings back button click listener
        ((ImageButton)findViewById(R.id.settings_back_button)).setOnClickListener((View view) -> {
            finish();
        });

        Resources resources = getResources();

        // Init language switcher button
        String[] languages = resources.getStringArray(R.array.settings_languages);
        int language = settings.getInt("language", Config.SETTINGS_LANGUAGE_DEFAULT);
        ((TextView)findViewById(R.id.settings_language_label)).setText(languages[language]);

        ((LinearLayout)findViewById(R.id.settings_language_button)).setOnClickListener((View view) -> {
            new AlertDialog.Builder(this)
                .setTitle(R.string.settings_language_button)
                .setSingleChoiceItems(languages, language, (DialogInterface dialog, int which) -> {
                    dialog.dismiss();

                    // When different language is selected save and recreate activity
                    if (language != which) {
                        SharedPreferences.Editor settingsEditor = settings.edit();
                        settingsEditor.putInt("language", which);
                        settingsEditor.apply();
                        recreate();
                    }
                })
                .setNegativeButton(R.string.settings_cancel_button, null)
                .show();
        });

        // Init themes switcher button
        String[] themes = resources.getStringArray(R.array.settings_themes);
        int theme = settings.getInt("theme", Config.SETTINGS_THEME_DEFAULT);
        ((TextView)findViewById(R.id.settings_theme_label)).setText(themes[theme]);

        ((LinearLayout)findViewById(R.id.settings_theme_button)).setOnClickListener((View view) -> {
            new AlertDialog.Builder(this)
                .setTitle(R.string.settings_theme_button)
                .setSingleChoiceItems(themes, theme, (DialogInterface dialog, int which) ->  {
                    dialog.dismiss();

                    // When different theme is selected save and recreate activity
                    if (theme != which) {
                        SharedPreferences.Editor settingsEditor = settings.edit();
                        settingsEditor.putInt("theme", which);
                        settingsEditor.apply();
                        recreate();
                    }
                })
                .setNegativeButton(R.string.settings_cancel_button, null)
                .show();
        });

        // Init version button
        try {
            ((TextView)findViewById(R.id.settings_version_label)).setText("v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        // Init rate button
        ((LinearLayout)findViewById(R.id.settings_rate_button)).setOnClickListener((View view) -> {
            // Open the store page for the user to add a rating
            String appPackageName = getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (Exception exception) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        });

        // Init share button
        ((LinearLayout)findViewById(R.id.settings_share_button)).setOnClickListener((View view) -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.settings_share_message) + " https://play.google.com/store/apps/details?id=" + getPackageName());
            startActivity(Intent.createChooser(intent, null));
        });

        // Init about button
        ((LinearLayout)findViewById(R.id.settings_about_button)).setOnClickListener((View view) -> {
            new AlertDialog.Builder(this)
                .setTitle(R.string.settings_about_button)
                .setMessage(R.string.settings_about_message_label)
                .setNegativeButton(R.string.settings_about_website_button, (DialogInterface dialog, int which) ->  {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://bastiaan.ml/")));
                })
                .setPositiveButton(R.string.settings_about_ok_button, null)
                .show();
        });
    }
}
