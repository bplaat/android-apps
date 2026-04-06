/*
 * Copyright (c) 2024-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package com.ycombinator.news.components;

import static nl.plaatsoft.android.react.Unit.*;
import static com.ycombinator.news.components.Styles.*;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import nl.plaatsoft.android.react.*;

import org.jspecify.annotations.Nullable;

import com.ycombinator.news.R;
import com.ycombinator.news.Settings;

public class SettingsScreen extends Component {
    public static final String STORE_PAGE_URL =
        "https://github.com/bplaat/android-apps/tree/master/bin/hackernews";
    private static final String ABOUT_WEBSITE_URL = "https://bplaat.nl/";

    private final Settings settings;
    private @Nullable String versionName;
    private int language;
    private int theme;
    private int versionClickCounter = 0;

    public SettingsScreen(Context context) {
        super(context);
        settings = new Settings(context);
        language = settings.getLanguage();
        theme = settings.getTheme();

        try {
            versionName = "v" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e(context.getPackageName(), "Can't get app version", e);
        }
    }

    @Override
    public void render() {
        var context = getContext();
        var languages = new String[] {context.getString(R.string.settings_language_english),
            context.getString(R.string.settings_language_dutch),
            context.getString(R.string.settings_language_system)};
        var themes = new String[] {context.getString(R.string.settings_theme_light),
            context.getString(R.string.settings_theme_dark),
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ? context.getString(R.string.settings_theme_battery_saver)
                                                          : context.getString(R.string.settings_theme_system)};

        new Column(() -> {
            new Row(() -> {
                new ImageButton(R.drawable.ic_arrow_left)
                    .onClick(() -> ((Activity)context).finish())
                    .modifier(actionBarIconButton());
                new Text(R.string.settings_title_label).modifier(actionBarTitle());
            }).modifier(actionBar());

            new Column(() -> {
                new Text(R.string.settings_display_label).modifier(listItemSubtitle());
                settingsRow(R.drawable.ic_translate, R.string.settings_language_button, languages[language],
                    () -> showLanguageDialog(languages));
                settingsRow(R.drawable.ic_palette, R.string.settings_theme_button, themes[theme],
                    () -> showThemeDialog(themes));

                new Text(R.string.settings_information_label).modifier(listItemSubtitle());
                settingsRow(R.drawable.ic_comment, R.string.settings_version_button, versionName, () -> {
                    versionClickCounter++;
                    if (versionClickCounter == 8) {
                        versionClickCounter = 0;
                        Toast.makeText(context, R.string.settings_version_message, Toast.LENGTH_SHORT).show();
                        context.startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/dQw4w9WgXcQ?t=43")));
                    }
                });
                settingsRow(R.drawable.ic_star, R.string.settings_rate_button, null,
                    () -> context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(STORE_PAGE_URL))));
                settingsRow(R.drawable.ic_share_variant, R.string.settings_share_button, null, () -> {
                    var intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT,
                        context.getString(R.string.settings_share_message, STORE_PAGE_URL));
                    context.startActivity(Intent.createChooser(intent, null));
                });
                settingsRow(R.drawable.ic_information, R.string.settings_about_button, null,
                    this::showAboutDialog);

                new Text(R.string.settings_footer_button)
                    .modifier(listItemFooter())
                    .onClick(
                        () -> context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ABOUT_WEBSITE_URL))));
            }).modifier(Modifier.of().width(matchParent()).height(matchParent()).scrollVertical());
        }).modifier(Modifier.of().width(matchParent()).height(matchParent()));
    }

    private void settingsRow(int iconRes, int labelRes, @Nullable String meta, Runnable onClick) {
        new Row(() -> {
            new Image(iconRes).modifier(listItemButtonIcon());
            new Text(labelRes).modifier(listItemButtonLabel());
            if (meta != null) {
                new Text(meta).modifier(listItemButtonMeta());
            }
        })
            .modifier(listItemButton())
            .onClick(onClick);
    }

    private void showLanguageDialog(String[] languages) {
        var dialog = new AlertDialog.Builder(getContext())
                         .setTitle(R.string.settings_language_button)
                         .setSingleChoiceItems(languages, language,
                             (d, which) -> {
                                 d.dismiss();
                                 if (language != which) {
                                     settings.setLanguage(which);
                                     language = settings.getLanguage();
                                     if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
                                         ((Activity)getContext()).recreate();
                                     else
                                         rebuild();
                                 }
                             })
                         .show();
        var density = getContext().getResources().getDisplayMetrics().density;
        dialog.getListView().setPadding(0, 0, 0, (int)(16 * density));
    }

    private void showThemeDialog(String[] themes) {
        var dialog = new AlertDialog.Builder(getContext())
                         .setTitle(R.string.settings_theme_button)
                         .setSingleChoiceItems(themes, theme,
                             (d, which) -> {
                                 d.dismiss();
                                 if (theme != which) {
                                     settings.setTheme(which);
                                     ((Activity)getContext()).recreate();
                                 }
                             })
                         .show();
        var density = getContext().getResources().getDisplayMetrics().density;
        dialog.getListView().setPadding(0, 0, 0, (int)(16 * density));
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(getContext())
            .setTitle(R.string.settings_about_alert_title_label)
            .setMessage(R.string.settings_about_alert_message_label)
            .setNegativeButton(R.string.settings_about_alert_website_button,
                (d, which)
                    -> getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ABOUT_WEBSITE_URL))))
            .setPositiveButton(R.string.settings_about_alert_ok_button, null)
            .show();
    }
}
