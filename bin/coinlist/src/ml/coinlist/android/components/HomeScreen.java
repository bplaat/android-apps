/*
 * Copyright (c) 2021-2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package ml.coinlist.android.components;

import static nl.plaatsoft.android.react.Unit.*;

import static ml.coinlist.android.components.Styles.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import nl.plaatsoft.android.fetch.FetchDataTask;
import nl.plaatsoft.android.react.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jspecify.annotations.Nullable;

import ml.coinlist.android.R;
import ml.coinlist.android.Settings;
import ml.coinlist.android.models.Coin;
import ml.coinlist.android.models.GlobalData;

public class HomeScreen extends Component {
    private static final int REFRESH_TIMEOUT = 60 * 1000;

    private final Runnable openSettings;
    private final Settings settings;
    private @Nullable FetchDataTask globalInfoTask = null;
    private @Nullable FetchDataTask coinsTask = null;
    private @Nullable GlobalData globalData = null;
    private List<Coin> coins = new ArrayList<>();
    private boolean starredOnly;
    private int oldCurrency = -1;
    private int oldLanguage = -1;
    private int oldTheme = -1;

    public HomeScreen(Context context, Runnable openSettings) {
        super(context);
        this.openSettings = openSettings;
        settings = new Settings(context);
        starredOnly = settings.getStarredOnly();
        for (var i = 0; i < 100; i++) coins.add(Coin.PLACEHOLDER);
    }

    public void onResume() {
        if (oldCurrency != -1 && oldCurrency != settings.getCurrency()) {
            if (globalInfoTask != null)
                globalInfoTask.cancel();
            if (coinsTask != null)
                coinsTask.cancel();
            globalData = null;
            coins = new ArrayList<>();
            for (var i = 0; i < 100; i++) coins.add(Coin.PLACEHOLDER);
            rebuild();
            loadGlobalInfo(false);
            loadCoins(false);
        } else {
            loadGlobalInfo(!(System.currentTimeMillis() - settings.getGlobalLoadTime() >= REFRESH_TIMEOUT));
            loadCoins(!(System.currentTimeMillis() - settings.getCoinsLoadTime() >= REFRESH_TIMEOUT));
        }
        oldCurrency = -1;

        if (oldLanguage != -1 && oldTheme != -1) {
            var languageChanged =
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && oldLanguage != settings.getLanguage();
            var themeChanged = oldTheme != settings.getTheme();
            if (languageChanged || themeChanged) {
                ((Activity)getContext()).recreate();
            }
            oldLanguage = -1;
            oldTheme = -1;
        }
    }

    @Override
    public void render() {
        new Column(() -> {
            new Row(() -> {
                new Text(R.string.app_name).modifier(actionBarTitle());

                new ImageButton(starredOnly ? R.drawable.ic_star : R.drawable.ic_star_outline)
                    .onClick(() -> {
                        starredOnly = !starredOnly;
                        settings.setStarredOnly(starredOnly);
                        rebuild();
                        loadCoins(true);
                    })
                    .modifier(actionBarIconButton());

                new ImageButton(R.drawable.ic_refresh)
                    .onClick(() -> {
                        loadGlobalInfo(false);
                        loadCoins(false);
                    })
                    .modifier(actionBarIconButton());

                new ImageButton(R.drawable.ic_dots_vertical)
                    .onClick((view) -> {
                        new PopupMenu(getContext(), view)
                            .item(R.string.menu_options_settings,
                                () -> {
                                    oldCurrency = settings.getCurrency();
                                    oldLanguage = settings.getLanguage();
                                    oldTheme = settings.getTheme();
                                    openSettings.run();
                                })
                            .show();
                    })
                    .modifier(actionBarIconButton());
            }).modifier(actionBar());

            if (starredOnly && coins.isEmpty()) {
                new GlobalInfoBar(globalData, settings, this::onRefresh);
                new Box(() -> {
                    new Text(R.string.main_coins_empty)
                        .modifier(
                            Modifier.of().align(android.view.Gravity.CENTER).textColor(R.color.secondary_text_color));
                }).modifier(Modifier.of().width(matchParent()).weight(1));
            } else {
                new LazyColumn<>(coins, Coin::id,
                    ()
                        -> new GlobalInfoBar(globalData, settings, this::onRefresh),
                    coin -> new CoinItem(coin, settings, this::onToggleStar, () -> onCycleStat(coin)))
                    .modifier(Modifier.of().width(matchParent()).weight(1).useWindowInsets());
            }
        }).modifier(Modifier.of().width(matchParent()).height(matchParent()));
    }

    private void onRefresh() {
        loadGlobalInfo(false);
        loadCoins(false);
    }

    private void onToggleStar(Coin updated) {
        var iter = coins.listIterator();
        while (iter.hasNext()) {
            var c = iter.next();
            if (c.id().equals(updated.id())) {
                if (!starredOnly || updated.starred())
                    iter.set(updated);
                else
                    iter.remove();
                break;
            }
        }
        rebuild();
    }

    private void onCycleStat(Coin coin) {
        coins.replaceAll(c -> c.id().equals(coin.id()) ? c.withNextVisibleStat() : c);
        rebuild();
    }

    private void loadGlobalInfo(boolean loadFromCache) {
        if (globalInfoTask != null)
            globalInfoTask.cancel();
        globalInfoTask =
            FetchDataTask.with(getContext())
                .load("https://api.coingecko.com/api/v3/global")
                .loadFromCache(loadFromCache)
                .saveToCache(true)
                .then(
                    data
                    -> {
                        try {
                            settings.setGlobalLoadTime(System.currentTimeMillis());
                            var jsonData =
                                new JSONObject(new String(data, StandardCharsets.UTF_8)).getJSONObject("data");
                            globalData = GlobalData.fromJSON(jsonData, settings.getCurrencyName());
                            rebuild();
                        } catch (JSONException exception) {
                            Log.e(getContext().getPackageName(), "Can't parse global data", exception);
                        }
                    },
                    exception -> Log.e(getContext().getPackageName(), "Can't fetch global data", exception))
                .fetch();
    }

    private void loadCoins(boolean loadFromCache) {
        if (coinsTask != null)
            coinsTask.cancel();
        var url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=" + settings.getCurrencyName();
        coinsTask = FetchDataTask.with(getContext())
                        .load(url)
                        .loadFromCache(loadFromCache)
                        .saveToCache(true)
                        .then(
                            data
                            -> {
                                try {
                                    settings.setCoinsLoadTime(System.currentTimeMillis());
                                    var jsonStarredCoins = settings.getStarredCoins();
                                    var jsonCoins = new JSONArray(new String(data, StandardCharsets.UTF_8));
                                    coins = new ArrayList<>();
                                    for (var i = 0; i < jsonCoins.length(); i++) {
                                        var jsonCoin = jsonCoins.getJSONObject(i);
                                        var isStarred = false;
                                        for (var j = 0; j < jsonStarredCoins.length(); j++) {
                                            if (jsonCoin.getString("id").equals(jsonStarredCoins.getString(j))) {
                                                isStarred = true;
                                                break;
                                            }
                                        }
                                        if (starredOnly && !isStarred)
                                            continue;
                                        coins.add(Coin.fromJSON(jsonCoin, isStarred));
                                    }
                                    rebuild();
                                } catch (JSONException exception) {
                                    Log.e(getContext().getPackageName(), "Can't parse coins data", exception);
                                }
                            },
                            exception -> Log.e(getContext().getPackageName(), "Can't fetch coins data", exception))
                        .fetch();
    }
}
