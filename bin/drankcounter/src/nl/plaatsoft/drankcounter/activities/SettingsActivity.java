/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.drankcounter.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import nl.plaatsoft.drankcounter.R;

import org.jspecify.annotations.Nullable;

@SuppressWarnings("this-escape")
public class SettingsActivity extends BaseActivity {
    public static final String STORE_PAGE_URL = "https://github.com/bplaat/android-apps/tree/master/bin/drankcounter";
    private static final String ABOUT_WEBSITE_URL = "https://bplaat.nl/";

    private int versionButtonClickCounter = 0;

    // MARK: BEGIN
    private ViewGroup root = null;
    public abstract class Container {
        public Container(int orientation, Runnable builder) {
            var linearLayout = new LinearLayout(SettingsActivity.this);
            linearLayout.setOrientation(orientation);
            var previousRoot = root;
            root = linearLayout;
            builder.run();
            if (previousRoot != null) {
                previousRoot.addView(linearLayout);
                root = previousRoot;
            }
        }
    }
    public class Row extends Container {
        public Row(Runnable builder) {
            super(LinearLayout.HORIZONTAL, builder);
        }
    }
    public class Column extends Container {
        public Column(Runnable builder) {
            super(LinearLayout.VERTICAL, builder);
        }
    }
    public class Text {
        public Text(String text) {
            var textView = new TextView(SettingsActivity.this);
            textView.setText(text);
            root.addView(textView);
        }
    }
    public class Button {
        public Button(String text, Runnable onClick) {
            var button = new android.widget.Button(SettingsActivity.this);
            button.setText(text);
            button.setOnClickListener(view -> onClick.run());
            root.addView(button);
        }
    }
    private void update() {
        if (root != null) {
            ((ViewGroup)findViewById(android.R.id.content)).removeView(root);
            root = null;
        }
        build();
        ((ViewGroup)findViewById(android.R.id.content)).addView(root);
    }
    // MARK: END

    public int counter = 0;

    private void build() {
        new Column(() -> {
            new Row(() -> {
                if (counter % 2 == 0)
                    new Text("Is odd!");
                new Button("Counter " + counter, () -> {
                    counter++;
                    update();
                });
                if (counter % 2 == 0)
                    new Text("Is odd!");
            });

            for (int i = 0; i < counter; i++) {
                new Text("Item #" + i);
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        useWindowInsets();
        update();

        // // Back button
        // findViewById(R.id.settings_back_button).setOnClickListener(view -> finish());

        // // Language button
        // var languages = new String[] {getResources().getString(R.string.settings_language_english),
        //     getResources().getString(R.string.settings_language_dutch),
        //     getResources().getString(R.string.settings_language_system)};
        // var language = settings.getLanguage();
        // ((TextView)findViewById(R.id.settings_language_label)).setText(languages[language]);
        // findViewById(R.id.settings_language_button).setOnClickListener(view -> {
        //     var alertDialog = new AlertDialog.Builder(this)
        //                           .setTitle(R.string.settings_language_button)
        //                           .setSingleChoiceItems(languages, language,
        //                               (dialog, which) -> {
        //                                   dialog.dismiss();
        //                                   if (language != which) {
        //                                       settings.setLanguage(which);
        //                                       recreate();
        //                                   }
        //                               })
        //                           .show();
        //     var density = getResources().getDisplayMetrics().density;
        //     alertDialog.getListView().setPadding(0, 0, 0, (int)(16 * density));
        // });

        // // Themes button
        // var themes = new String[] {getResources().getString(R.string.settings_theme_light),
        //     getResources().getString(R.string.settings_theme_dark),
        //     Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
        //         ? getResources().getString(R.string.settings_theme_battery_saver)
        //         : getResources().getString(R.string.settings_theme_system)};
        // var theme = settings.getTheme();
        // ((TextView)findViewById(R.id.settings_theme_label)).setText(themes[theme]);
        // findViewById(R.id.settings_theme_button).setOnClickListener(view -> {
        //     var alertDialog = new AlertDialog.Builder(this)
        //                           .setTitle(R.string.settings_theme_button)
        //                           .setSingleChoiceItems(themes, theme,
        //                               (dialog, which) -> {
        //                                   dialog.dismiss();
        //                                   if (theme != which) {
        //                                       settings.setTheme(which);
        //                                       recreate();
        //                                   }
        //                               })
        //                           .show();
        //     var density = getResources().getDisplayMetrics().density;
        //     alertDialog.getListView().setPadding(0, 0, 0, (int)(16 * density));
        // });

        // // Version button easter egg
        // try {
        //     ((TextView)findViewById(R.id.settings_version_label))
        //         .setText("v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        // } catch (NameNotFoundException exception) {
        //     Log.e(getPackageName(), "Can't get app version", exception);
        // }
        // findViewById(R.id.settings_version_button).setOnClickListener(view -> {
        //     versionButtonClickCounter++;
        //     if (versionButtonClickCounter == 8) {
        //         versionButtonClickCounter = 0;
        //         Toast.makeText(this, R.string.settings_version_message, Toast.LENGTH_SHORT).show();
        //         startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/dQw4w9WgXcQ?t=43")));
        //     }
        // });

        // // Rate button
        // findViewById(R.id.settings_rate_button).setOnClickListener(view -> {
        //     startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(STORE_PAGE_URL)));
        // });

        // // Share button
        // findViewById(R.id.settings_share_button).setOnClickListener(view -> {
        //     var intent = new Intent();
        //     intent.setAction(Intent.ACTION_SEND);
        //     intent.setType("text/plain");
        //     intent.putExtra(
        //         Intent.EXTRA_TEXT, getResources().getString(R.string.settings_share_message, STORE_PAGE_URL));
        //     startActivity(Intent.createChooser(intent, null));
        // });

        // // About button
        // findViewById(R.id.settings_about_button).setOnClickListener(view -> {
        //     new AlertDialog.Builder(this)
        //         .setTitle(R.string.settings_about_alert_title_label)
        //         .setMessage(R.string.settings_about_alert_message_label)
        //         .setNegativeButton(R.string.settings_about_alert_website_button,
        //             (dialog, which) -> { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ABOUT_WEBSITE_URL)));
        //             })
        //         .setPositiveButton(R.string.settings_about_alert_ok_button, null)
        //         .show();
        // });

        // // Footer button
        // findViewById(R.id.settings_footer_button).setOnClickListener(view -> {
        //     startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ABOUT_WEBSITE_URL)));
        // });
    }
}
