package nl.plaatsoft.bassietest.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import nl.plaatsoft.bassietest.Consts;
import nl.plaatsoft.bassietest.R;

public class SettingsActivity extends BaseActivity {
    private int versionButtonClickCounter = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Back button
        findViewById(R.id.settings_back_button).setOnClickListener(view -> finish());

        // Language button
        var languages = new String[] {
            getResources().getString(R.string.settings_language_english),
            getResources().getString(R.string.settings_language_dutch),
            getResources().getString(R.string.settings_language_system)
        };
        var language = settings.getInt("language", Consts.Settings.LANGUAGE_DEFAULT);
        ((TextView)findViewById(R.id.settings_language_label)).setText(languages[language]);
        findViewById(R.id.settings_language_button).setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                .setTitle(R.string.settings_language_alert_title_label)
                .setSingleChoiceItems(languages, language, (dialog, which) -> {
                    dialog.dismiss();
                    if (language != which) {
                        var settingsEditor = settings.edit();
                        settingsEditor.putInt("language", which);
                        settingsEditor.apply();
                        recreate();
                    }
                })
                .setNegativeButton(R.string.settings_language_alert_cancel_button, null)
                .show();
        });

        // Themes button
        var themes = new String[] {
            getResources().getString(R.string.settings_theme_light),
            getResources().getString(R.string.settings_theme_dark),
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                ? getResources().getString(R.string.settings_theme_battery_saver)
                : getResources().getString(R.string.settings_theme_system)
        };
        var theme = settings.getInt("theme", Consts.Settings.THEME_DEFAULT);
        ((TextView)findViewById(R.id.settings_theme_label)).setText(themes[theme]);
        findViewById(R.id.settings_theme_button).setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                .setTitle(R.string.settings_theme_alert_title_label)
                .setSingleChoiceItems(themes, theme, (dialog, which) ->  {
                    dialog.dismiss();
                    if (theme != which) {
                        var settingsEditor = settings.edit();
                        settingsEditor.putInt("theme", which);
                        settingsEditor.apply();
                        recreate();
                    }
                })
                .setNegativeButton(R.string.settings_theme_alert_cancel_button, null)
                .show();
        });

        // Version button easter egg
        try {
            ((TextView)findViewById(R.id.settings_version_label)).setText("v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (Exception exception) {
            Log.e(Consts.LOG_TAG, "Can't get app version", exception);
        }
        findViewById(R.id.settings_version_button).setOnClickListener(view -> {
            versionButtonClickCounter++;
            if (versionButtonClickCounter == 8) {
                versionButtonClickCounter = 0;
                Toast.makeText(this, R.string.settings_version_message, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/dQw4w9WgXcQ?t=43")));
            }
        });

        // Rate button
        findViewById(R.id.settings_rate_button).setOnClickListener(view -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Consts.STORE_PAGE_URL)));
        });

        // Share button
        findViewById(R.id.settings_share_button).setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.settings_share_message) + " " + Consts.STORE_PAGE_URL);
            startActivity(Intent.createChooser(intent, null));
        });

        // About button
        findViewById(R.id.settings_about_button).setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                .setTitle(R.string.settings_about_alert_title_label)
                .setMessage(R.string.settings_about_alert_message_label)
                .setNegativeButton(R.string.settings_about_alert_website_button, (dialog, which) ->  {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Consts.Settings.ABOUT_WEBSITE_URL)));
                })
                .setPositiveButton(R.string.settings_about_alert_ok_button, null)
                .show();
        });

        // Footer button
        findViewById(R.id.settings_footer_button).setOnClickListener(view -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Consts.Settings.ABOUT_WEBSITE_URL)));
        });
    }
}
