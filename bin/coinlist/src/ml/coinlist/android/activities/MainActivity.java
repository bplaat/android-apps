/*
 * Copyright (c) 2021-2024 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package ml.coinlist.android.activities;

import java.nio.charset.StandardCharsets;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import nl.plaatsoft.android.alerts.UpdateAlert;
import nl.plaatsoft.android.compat.ContextCompat;
import nl.plaatsoft.android.fetch.FetchDataTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jspecify.annotations.Nullable;

import ml.coinlist.android.Formatters;
import ml.coinlist.android.R;
import ml.coinlist.android.components.CoinsAdapter;
import ml.coinlist.android.models.Coin;

public class MainActivity extends BaseActivity implements PopupMenu.OnMenuItemClickListener {
    private static final int SETTINGS_REQUEST_CODE = 1;
    private static final int REFRESH_TIMEOUT = 60 * 1000;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private int oldCurrency = -1;
    private int oldLanguage = -1;
    private int oldTheme = -1;
    private boolean starredOnly;
    private @SuppressWarnings("null") ListView coinsList;
    private @SuppressWarnings("null") LinearLayout globalInfo;
    private @SuppressWarnings("null") CoinsAdapter coinsAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        coinsList = findViewById(R.id.main_coins_list);
        useWindowInsets(coinsList);

        // Starred button
        var starredButton = (ImageButton)findViewById(R.id.main_starred_button);
        starredOnly = settings.getStarredOnly();
        starredButton.setImageResource(starredOnly ? R.drawable.ic_star : R.drawable.ic_star_outline);
        starredButton.setOnClickListener(view -> {
            starredOnly = !starredOnly;
            settings.setStarredOnly(starredOnly);
            starredButton.setImageResource(starredOnly ? R.drawable.ic_star : R.drawable.ic_star_outline);
            loadCoins(true);
        });

        // Refresh button
        findViewById(R.id.main_refresh_button).setOnClickListener(view -> {
            loadGlobalInfo(false);
            loadCoins(false);
        });

        // Options menu button
        findViewById(R.id.main_options_menu_button).setOnClickListener(view -> {
            var optionsMenu = new PopupMenu(this, view, Gravity.TOP | Gravity.RIGHT);
            optionsMenu.getMenuInflater().inflate(R.menu.options, optionsMenu.getMenu());
            optionsMenu.setOnMenuItemClickListener(this);
            optionsMenu.show();
        });

        // Load coins data
        globalInfo = (LinearLayout)getLayoutInflater().inflate(R.layout.view_coins_header, coinsList, false);
        coinsList.addHeaderView(globalInfo);
        coinsList.addFooterView(new View(this));

        coinsAdapter = new CoinsAdapter(this, settings);
        coinsList.setAdapter(coinsAdapter);
        for (var i = 0; i < 100; i++) coinsAdapter.add(Coin.PLACEHOLDER);

        coinsList.setOnItemClickListener((adapterView, view, position, id) -> {
            if (position == 0) {
                loadGlobalInfo(false);
                loadCoins(false);
                return;
            }

            var index = position - 1;
            var coin = coinsAdapter.getItem(index);
            if (coin.isPlaceholder())
                return;

            coinsAdapter.remove(coin);
            coinsAdapter.insert(coin.withNextVisibleStat(), index);
        });

        // Show update alert
        UpdateAlert.checkAndShow(this,
            "https://raw.githubusercontent.com/bplaat/android-apps/refs/heads/master/bin/coinlist/bob.toml",
            SettingsActivity.STORE_PAGE_URL);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadGlobalInfo(!(System.currentTimeMillis() - settings.getGlobalLoadTime() >= REFRESH_TIMEOUT));
        loadCoins(!(System.currentTimeMillis() - settings.getCoinsLoadTime() >= REFRESH_TIMEOUT));
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
            if (oldCurrency != -1) {
                if (oldCurrency != settings.getCurrency()) {
                    loadGlobalInfo(true);
                    loadCoins(true);
                }
            }
            if (oldLanguage != -1 && oldTheme != -1) {
                if (oldLanguage != settings.getLanguage() || oldTheme != settings.getTheme()) {
                    handler.post(() -> recreate());
                }
            }
        }
    }

    private void loadGlobalInfo(boolean loadFromCache) {
        FetchDataTask.with(this)
            .load("https://api.coingecko.com/api/v3/global")
            .loadFromCache(loadFromCache)
            .saveToCache(true)
            .then(data -> {
                try {
                    settings.setGlobalLoadTime(System.currentTimeMillis());

                    var jsonData = new JSONObject(new String(data, StandardCharsets.UTF_8)).getJSONObject("data");

                    ((TextView)globalInfo.findViewById(R.id.global_info_market_cap_text))
                        .setText(getResources().getString(R.string.main_global_market_cap,
                            Formatters.money(settings,
                                jsonData.getJSONObject("total_market_cap").getDouble(settings.getCurrencyName()))));
                    var marketCapChange = jsonData.getDouble("market_cap_change_percentage_24h_usd");
                    var marketCapChangeLabel = (TextView)globalInfo.findViewById(R.id.global_info_market_cap_change);
                    marketCapChangeLabel.setText(Formatters.changePercent(marketCapChange));
                    if (marketCapChange > 0) {
                        marketCapChangeLabel.setTextColor(ContextCompat.getColor(this, R.color.positive_color));
                    } else if (marketCapChange < 0) {
                        marketCapChangeLabel.setTextColor(ContextCompat.getColor(this, R.color.negative_color));
                    } else {
                        marketCapChangeLabel.setTextColor(ContextCompat.getColor(this, R.color.secondary_text_color));
                    }
                    var marketCapLine = globalInfo.findViewById(R.id.global_info_market_cap_line);
                    if (((ColorDrawable)marketCapLine.getBackground()).getColor() != Color.TRANSPARENT) {
                        var set = (AnimatorSet)AnimatorInflater.loadAnimator(this, R.animator.fade_in);
                        set.setTarget(marketCapLine);
                        set.start();
                    }

                    var volumeLabel = (TextView)globalInfo.findViewById(R.id.global_info_volume);
                    volumeLabel.setText(getResources().getString(R.string.main_global_volume,
                        Formatters.money(
                            settings, jsonData.getJSONObject("total_volume").getDouble(settings.getCurrencyName()))));
                    if (((ColorDrawable)volumeLabel.getBackground()).getColor() != Color.TRANSPARENT) {
                        var set = (AnimatorSet)AnimatorInflater.loadAnimator(this, R.animator.text_fade_in);
                        set.setTarget(volumeLabel);
                        set.start();
                    }

                    var dominanceLabel = (TextView)globalInfo.findViewById(R.id.global_info_dominance);
                    dominanceLabel.setText(getResources().getString(R.string.main_global_dominance,
                        Formatters.percent(jsonData.getJSONObject("market_cap_percentage").getDouble("btc")),
                        Formatters.percent(jsonData.getJSONObject("market_cap_percentage").getDouble("eth"))));
                    if (((ColorDrawable)dominanceLabel.getBackground()).getColor() != Color.TRANSPARENT) {
                        var set = (AnimatorSet)AnimatorInflater.loadAnimator(this, R.animator.text_fade_in);
                        set.setTarget(dominanceLabel);
                        set.start();
                    }
                } catch (JSONException exception) {
                    Log.e(getPackageName(), "Can't parse global data", exception);
                }
            })
            .fetch();
    }

    private void loadCoins(boolean loadFromCache) {
        var url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=" + settings.getCurrencyName();
        FetchDataTask.with(this)
            .load(url)
            .loadFromCache(loadFromCache)
            .saveToCache(true)
            .then(data -> {
                try {
                    settings.setCoinsLoadTime(System.currentTimeMillis());

                    coinsAdapter.clear();
                    var jsonStarredCoins = settings.getStarredCoins();
                    var jsonCoins = new JSONArray(new String(data, StandardCharsets.UTF_8));
                    for (var i = 0; i < jsonCoins.length(); i++) {
                        var jsonCoin = jsonCoins.getJSONObject(i);

                        var isStarred = false;
                        for (var j = 0; j < jsonStarredCoins.length(); j++) {
                            if (jsonCoin.getString("id").equals(jsonStarredCoins.getString(j))) {
                                isStarred = true;
                                break;
                            }
                        }
                        if (starredOnly && !isStarred) {
                            continue;
                        }

                        coinsAdapter.add(Coin.fromJSON(jsonCoin, isStarred));
                    }
                } catch (JSONException exception) {
                    Log.e(getPackageName(), "Can't parse coins data", exception);
                }
            })
            .fetch();
    }
}
