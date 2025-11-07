/*
 * Copyright (c) 2019-2025 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.nos.android.activities;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

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
import android.widget.ViewFlipper;

import nl.plaatsoft.android.alerts.UpdateAlert;
import nl.plaatsoft.android.fetch.FetchDataTask;
import nl.plaatsoft.nos.android.Config;
import nl.plaatsoft.nos.android.R;
import nl.plaatsoft.nos.android.components.ArticlesAdapter;
import nl.plaatsoft.nos.android.models.Article;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends BaseActivity implements PopupMenu.OnMenuItemClickListener {
    private static record Tab(String url, String title, ListView listView, LinearLayout button) {}

    private static final int SETTINGS_REQUEST_CODE = 1;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private int oldLanguage = -1;
    private int oldTheme = -1;

    private TextView titleLabel;
    private ViewFlipper viewFlipper;
    private ArrayList<Tab> tabs;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        useWindowInsets(findViewById(R.id.main_bottom_bar));

        // App bar
        titleLabel = findViewById(R.id.main_title_label);
        viewFlipper = findViewById(R.id.main_view_flipper);

        findViewById(R.id.main_refresh_button).setOnClickListener(view -> {
            for (var tab : tabs) {
                var articlesAdapter = (ArticlesAdapter)tab.listView.getAdapter();
                fetchNewsData(tab.url, false, articlesAdapter);
            }
        });

        findViewById(R.id.main_options_button).setOnClickListener(view -> {
            var optionsMenu = new PopupMenu(this, view, Gravity.TOP | Gravity.RIGHT);
            optionsMenu.getMenuInflater().inflate(R.menu.options, optionsMenu.getMenu());
            optionsMenu.setOnMenuItemClickListener(this);
            optionsMenu.show();
        });

        // Init tabs
        tabs = new ArrayList<>();
        tabs.add(new Tab("http://feeds.nos.nl/nosnieuwsalgemeen", getString(R.string.main_latest_title),
            (ListView)findViewById(R.id.main_latest_articles_list),
            (LinearLayout)findViewById(R.id.main_latest_button)));
        tabs.add(new Tab("http://feeds.nos.nl/nosnieuwseconomie", getString(R.string.main_economy_title),
            (ListView)findViewById(R.id.main_economy_articles_list),
            (LinearLayout)findViewById(R.id.main_economy_button)));
        tabs.add(new Tab("http://feeds.nos.nl/nosnieuwspolitiek", getString(R.string.main_politics_title),
            (ListView)findViewById(R.id.main_politics_articles_list),
            (LinearLayout)findViewById(R.id.main_politics_button)));
        tabs.add(new Tab("http://feeds.nos.nl/nosnieuwstech", getString(R.string.main_tech_title),
            (ListView)findViewById(R.id.main_tech_articles_list), (LinearLayout)findViewById(R.id.main_tech_button)));
        tabs.add(new Tab("http://feeds.nos.nl/nossportalgemeen", getString(R.string.main_sports_title),
            (ListView)findViewById(R.id.main_sports_articles_list),
            (LinearLayout)findViewById(R.id.main_sports_button)));

        for (var i = 0; i < tabs.size(); i++) {
            var tab = tabs.get(i);
            var articlesAdapter = new ArticlesAdapter(this);
            tab.listView.setAdapter(articlesAdapter);
            tab.listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
                Intent intent = new Intent(this, ArticleActivity.class);
                intent.putExtra("article", articlesAdapter.getItem(position));
                startActivity(intent);
            });
            tab.button.setTag(i);
            tab.button.setOnClickListener(view -> openTab((int)view.getTag(), true));
            fetchNewsData(tab.url, true, articlesAdapter);
        }

        openTab(0, false);

        // Show update alert
        UpdateAlert.checkAndShow(this,
            "https://raw.githubusercontent.com/bplaat/android-apps/refs/heads/master/bin/nos/bob.toml",
            SettingsActivity.STORE_PAGE_URL);
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

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        openTab(savedInstanceState.getInt("openTab"), false);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("openTab", viewFlipper.getDisplayedChild());
    }

    @Override
    protected boolean shouldBackOverride() {
        return viewFlipper.getDisplayedChild() != 0;
    }

    @Override
    protected void onBack() {
        if (viewFlipper.getDisplayedChild() != 0) {
            openTab(0, true);
        }
    }

    private void openTab(int index, boolean withAnimation) {
        var tab = tabs.get(index);
        titleLabel.setText(tab.title);
        viewFlipper.setDisplayedChild(index);

        for (var i = 0; i < tabs.size(); i++) {
            var button = tabs.get(i).button;
            if (i == index) {
                button.animate().alpha(1f).setDuration(150);
            } else {
                var buttonInactiveAlphaValue = new TypedValue();
                getResources().getValue(R.dimen.bottom_bar_button_inactive_alpha, buttonInactiveAlphaValue, true);
                button.animate().alpha(buttonInactiveAlphaValue.getFloat()).setDuration(150);
            }
        }

        updateBackListener();
    }

    private void fetchNewsData(String rssUrl, boolean loadFromCache, ArticlesAdapter articlesAdapter) {
        try {
            FetchDataTask.with(this)
                .load("https://api.rss2json.com/v1/api.json?rss_url=" + URLEncoder.encode(rssUrl, "UTF-8"))
                .loadFromCache(loadFromCache)
                .saveToCache(true)
                .then(data -> {
                    try {
                        var feed = new JSONObject(new String(data, StandardCharsets.UTF_8));
                        var articles = feed.getJSONArray("items");
                        articlesAdapter.clear();
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
