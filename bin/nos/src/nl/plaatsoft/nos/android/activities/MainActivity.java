/*
 * Copyright (c) 2019-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.nos.android.activities;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import nl.plaatsoft.android.fetch.FetchDataTask;
import nl.plaatsoft.nos.android.Config;
import nl.plaatsoft.nos.android.R;
import nl.plaatsoft.nos.android.components.ArticlesAdapter;
import nl.plaatsoft.nos.android.models.Article;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends BaseActivity implements PopupMenu.OnMenuItemClickListener {
    private static final int SETTINGS_REQUEST_CODE = 1;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private int oldLanguage = -1;
    private int oldTheme = -1;
    private LinearLayout[] tabs;
    private LinearLayout[] buttons;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        useWindowInsets();

        // News tabs
        initNewsTab((ListView)findViewById(R.id.latest_articles_list), "http://feeds.nos.nl/nosnieuwsalgemeen",
            (ImageButton)findViewById(R.id.latest_refresh_button),
            (ImageButton)findViewById(R.id.latest_options_button));
        initNewsTab((ListView)findViewById(R.id.economy_articles_list), "http://feeds.nos.nl/nosnieuwseconomie",
            (ImageButton)findViewById(R.id.economy_refresh_button),
            (ImageButton)findViewById(R.id.economy_options_button));
        initNewsTab((ListView)findViewById(R.id.politics_articles_list), "http://feeds.nos.nl/nosnieuwspolitiek",
            (ImageButton)findViewById(R.id.politics_refresh_button),
            (ImageButton)findViewById(R.id.politics_options_button));
        initNewsTab((ListView)findViewById(R.id.tech_articles_list), "http://feeds.nos.nl/nosnieuwstech",
            (ImageButton)findViewById(R.id.tech_refresh_button), (ImageButton)findViewById(R.id.tech_options_button));
        initNewsTab((ListView)findViewById(R.id.sports_articles_list), "http://feeds.nos.nl/nossportalgemeen",
            (ImageButton)findViewById(R.id.sports_refresh_button),
            (ImageButton)findViewById(R.id.sports_options_button));

        // Bottom bar
        tabs = new LinearLayout[5];
        tabs[0] = (LinearLayout)findViewById(R.id.latest_tab);
        tabs[1] = (LinearLayout)findViewById(R.id.economy_tab);
        tabs[2] = (LinearLayout)findViewById(R.id.politics_tab);
        tabs[3] = (LinearLayout)findViewById(R.id.tech_tab);
        tabs[4] = (LinearLayout)findViewById(R.id.sports_tab);

        for (var i = 0; i < tabs.length; i++) {
            tabs[i].setTag(i);
        }

        buttons = new LinearLayout[5];
        buttons[0] = (LinearLayout)findViewById(R.id.latest_button);
        buttons[1] = (LinearLayout)findViewById(R.id.economy_button);
        buttons[2] = (LinearLayout)findViewById(R.id.politics_button);
        buttons[3] = (LinearLayout)findViewById(R.id.tech_button);
        buttons[4] = (LinearLayout)findViewById(R.id.sports_button);

        for (var i = 0; i < buttons.length; i++) {
            buttons[i].setTag(i);
            buttons[i].setOnClickListener((View view) -> { openTab((int)view.getTag(), true); });
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        openTab(savedInstanceState.getInt("openTab"), false);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        for (var tab : tabs) {
            if (tab.getVisibility() == View.VISIBLE) {
                savedInstanceState.putInt("openTab", (int)tab.getTag());
                break;
            }
        }
    }

    @Override
    public boolean onMenuItemClick(@SuppressWarnings("null") MenuItem item) {
        if (item.getItemId() == R.id.menu_options_settings) {
            oldLanguage = settings.getLanguage();
            oldTheme = settings.getTheme();
            startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_REQUEST_CODE);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @SuppressWarnings("null") Intent data) {
        // When settings activity is closed check for restart
        if (requestCode == SETTINGS_REQUEST_CODE) {
            if (oldLanguage != -1 && oldTheme != -1) {
                if (oldLanguage != settings.getLanguage() || oldTheme != settings.getTheme()) {
                    handler.post(() -> recreate());
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        if (tabs[0].getVisibility() != View.VISIBLE) {
            openTab(0, true);
        } else {
            super.onBackPressed();
        }
    }

    private void openTab(int index, boolean withAnimation) {
        for (var tab : tabs) {
            if ((int)tab.getTag() == index) {
                if (tab.getVisibility() != View.VISIBLE) {
                    tab.setVisibility(View.VISIBLE);

                    if (withAnimation) {
                        var tabContent = tab.getChildAt(1);
                        tabContent.setAlpha(0f);
                        tabContent.setScaleX(0.98f);
                        tabContent.setScaleY(0.98f);
                        tabContent.animate().alpha(1).scaleX(1).scaleY(1).setDuration(150);
                    }
                }
            } else {
                tab.setVisibility(View.GONE);
            }
        }

        for (var button : buttons) {
            if ((int)button.getTag() == index) {
                button.animate().alpha(1f).setDuration(150);
            } else {
                var buttonInactiveAlphaValue = new TypedValue();
                getResources().getValue(R.dimen.bottom_bar_button_inactive_alpha, buttonInactiveAlphaValue, true);
                button.animate().alpha(buttonInactiveAlphaValue.getFloat()).setDuration(150);
            }
        }
    }

    private void initNewsTab(ListView listView, String rssUrl, ImageButton refreshButton, ImageButton optionsButton) {
        var articlesAdapter = new ArticlesAdapter(this);
        listView.setAdapter(articlesAdapter);
        listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            Intent intent = new Intent(this, ArticleActivity.class);
            intent.putExtra("article", articlesAdapter.getItem(position));
            startActivity(intent);
        });

        refreshButton.setOnClickListener((View view) -> {
            articlesAdapter.clear();
            fetchNewsData(articlesAdapter, rssUrl, false);
        });

        optionsButton.setOnClickListener(view -> {
            var optionsMenu = new PopupMenu(this, view, Gravity.TOP | Gravity.RIGHT);
            optionsMenu.getMenuInflater().inflate(R.menu.options, optionsMenu.getMenu());
            optionsMenu.setOnMenuItemClickListener(this);
            optionsMenu.show();
        });

        fetchNewsData(articlesAdapter, rssUrl, true);
    }

    private void fetchNewsData(ArticlesAdapter articlesAdapter, String rssUrl, boolean loadFromCache) {
        try {
            FetchDataTask.with(this)
                .load("https://api.rss2json.com/v1/api.json?rss_url=" + URLEncoder.encode(rssUrl, "UTF-8"))
                .loadFromCache(loadFromCache)
                .saveToCache(true)
                .then(data -> {
                    try {
                        var feed = new JSONObject(new String(data, StandardCharsets.UTF_8));
                        var articles = feed.getJSONArray("items");
                        for (int i = 0; i < articles.length(); i++) {
                            var article = articles.getJSONObject(i);
                            articlesAdapter.add(new Article(article.getString("title"),
                                article.getJSONObject("enclosure").getString("link"), article.getString("pubDate"),
                                article.getString("content")));
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                })
                .fetch();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
