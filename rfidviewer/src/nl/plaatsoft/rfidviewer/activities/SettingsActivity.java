package nl.plaatsoft.rfidviewer.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import nl.plaatsoft.rfidviewer.Consts;
import nl.plaatsoft.rfidviewer.R;

public class SettingsActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set back button click listener
        ((ImageButton)findViewById(R.id.settings_back_button)).setOnClickListener(view -> {
            finish();
        });

        // Init language switcher button
        String[] languages = getResources().getStringArray(R.array.settings_languages);
        int language = settings.getInt("language", Consts.Settings.LANGUAGE_DEFAULT);
        ((TextView)findViewById(R.id.settings_language_label)).setText(languages[language]);

        ((LinearLayout)findViewById(R.id.settings_language_button)).setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                .setTitle(R.string.settings_language_alert_title_label)
                .setSingleChoiceItems(languages, language, (dialog, which) -> {
                    dialog.dismiss();
                    if (language != which) {
                        SharedPreferences.Editor settingsEditor = settings.edit();
                        settingsEditor.putInt("language", which);
                        settingsEditor.apply();
                        recreate();
                    }
                })
                .setNegativeButton(R.string.settings_language_alert_cancel_button, null)
                .show();
        });

        // Init themes switcher button
        String[] themes = getResources().getStringArray(R.array.settings_themes);
        int theme = settings.getInt("theme", Consts.Settings.THEME_DEFAULT);
        ((TextView)findViewById(R.id.settings_theme_label)).setText(themes[theme]);

        ((LinearLayout)findViewById(R.id.settings_theme_button)).setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                .setTitle(R.string.settings_theme_alert_title_label)
                .setSingleChoiceItems(themes, theme, (dialog, which) ->  {
                    dialog.dismiss();
                    if (theme != which) {
                        SharedPreferences.Editor settingsEditor = settings.edit();
                        settingsEditor.putInt("theme", which);
                        settingsEditor.apply();
                        recreate();
                    }
                })
                .setNegativeButton(R.string.settings_theme_alert_cancel_button, null)
                .show();
        });

        // Init version button easter egg
        try {
            ((TextView)findViewById(R.id.settings_version_label)).setText("v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (Exception exception) {
            Log.e(Consts.LOG_TAG, "Can't read app version", exception);
        }

        int versionButtonClickCounterHolder[] = { 0 };
        ((LinearLayout)findViewById(R.id.settings_version_button)).setOnClickListener(view -> {
            versionButtonClickCounterHolder[0]++;
            if (versionButtonClickCounterHolder[0] == 8) {
                versionButtonClickCounterHolder[0] = 0;
                Toast.makeText(this, R.string.settings_version_message, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/dQw4w9WgXcQ?t=43")));
            }
        });

        // Init rate button
        ((LinearLayout)findViewById(R.id.settings_rate_button)).setOnClickListener(view -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Consts.STORE_PAGE_URL)));
        });

        // Init share button
        ((LinearLayout)findViewById(R.id.settings_share_button)).setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.settings_share_message) + " " + Consts.STORE_PAGE_URL);
            startActivity(Intent.createChooser(intent, null));
        });

        // Init about button
        ((LinearLayout)findViewById(R.id.settings_about_button)).setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                .setTitle(R.string.settings_about_alert_title_label)
                .setMessage(R.string.settings_about_alert_message_label)
                .setNegativeButton(R.string.settings_about_alert_website_button, (dialog, which) ->  {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Consts.Settings.ABOUT_WEBSITE_URL)));
                })
                .setPositiveButton(R.string.settings_about_alert_ok_button, null)
                .show();
        });

        // Init footer button
        ((TextView)findViewById(R.id.settings_footer_button)).setOnClickListener(view -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Consts.Settings.ABOUT_WEBSITE_URL)));
        });
    }
}
