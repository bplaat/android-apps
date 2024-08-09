package ml.coinlist.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;

import ml.coinlist.android.components.CoinsAdapter;
import ml.coinlist.android.tasks.FetchDataTask;
import ml.coinlist.android.models.Coin;
import ml.coinlist.android.Consts;
import ml.coinlist.android.Utils;
import ml.coinlist.android.R;

public class MainActivity extends BaseActivity implements PopupMenu.OnMenuItemClickListener {
    public static final int SETTINGS_REQUEST_CODE = 1;
    public static final int REFRESH_TIMEOUT = 60 * 1000;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private int oldCurrency = -1;
    private int oldLanguage = -1;
    private int oldTheme = -1;
    private boolean starredOnly;
    private LinearLayout globalInfo;
    private ListView coinsList;
    private CoinsAdapter coinsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Starred button
        var starredButton = (ImageButton)findViewById(R.id.main_starred_button);
        starredOnly = settings.getBoolean("starred_only", Consts.Settings.STARRED_ONLY_DEFAULT);
        starredButton.setImageResource(starredOnly ? R.drawable.ic_star : R.drawable.ic_star_outline);
        starredButton.setOnClickListener(view -> {
            starredOnly = !starredOnly;
            starredButton.setImageResource(starredOnly ? R.drawable.ic_star : R.drawable.ic_star_outline);
            var settingsEditor = settings.edit();
            settingsEditor.putBoolean("starred_only", starredOnly);
            settingsEditor.apply();

            loadCoins(true);
        });

        // Refresh button
        findViewById(R.id.main_refresh_button).setOnClickListener(view -> {
            loadGlobalInfo(false);
            loadCoins(false);
        });

        // Options menu button
        findViewById(R.id.main_options_menu_button).setOnClickListener(view -> {
            PopupMenu optionsMenu = new PopupMenu(this, view, Gravity.TOP | Gravity.RIGHT);
            optionsMenu.getMenuInflater().inflate(R.menu.options, optionsMenu.getMenu());
            optionsMenu.setOnMenuItemClickListener(this);
            optionsMenu.show();
        });

        // Load coins data
        coinsList = (ListView)findViewById(R.id.main_coins_list);

        globalInfo = (LinearLayout)getLayoutInflater().inflate(R.layout.view_coins_header, coinsList, false);
        coinsList.addHeaderView(globalInfo);

        coinsAdapter = new CoinsAdapter(this);
        coinsList.setAdapter(coinsAdapter);

        coinsList.setOnItemClickListener((AdapterView<?> adapterView, View view, int position, long id) -> {
            if (position == 0) {
                loadGlobalInfo(false);
                loadCoins(false);
            } else {
                var coin = coinsAdapter.getItem(position - 1);
                if (coin.getExtraIndex() == 2) {
                    coin.setExtraIndex(0);
                } else {
                    coin.setExtraIndex(coin.getExtraIndex() + 1);
                }

                var coinExtra = (TextView)view.findViewById(R.id.coin_extra);
                if (coin.getExtraIndex() == 0) {
                    coinExtra.setText(getResources().getString(R.string.main_extra_marketcap) + " " +
                        Coin.formatMoney(this, coin.getMarketcap()));
                }
                if (coin.getExtraIndex() == 1) {
                    coinExtra.setText(getResources().getString(R.string.main_extra_volume) + " " +
                        Coin.formatMoney(this, coin.getVolume()));
                }
                if (coin.getExtraIndex() == 2) {
                    coinExtra.setText(getResources().getString(R.string.main_extra_supply) + " " +
                        Coin.formatNumber(this, coin.getSupply()));
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadGlobalInfo(!(System.currentTimeMillis() - settings.getLong("global_load_time", 0) >= REFRESH_TIMEOUT));
        loadCoins(!(System.currentTimeMillis() - settings.getLong("coins_load_time", 0) >= REFRESH_TIMEOUT));
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.menu_options_settings) {
            oldLanguage = settings.getInt("language", Consts.Settings.LANGUAGE_DEFAULT);
            oldTheme = settings.getInt("theme", Consts.Settings.THEME_DEFAULT);
            startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS_REQUEST_CODE);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETTINGS_REQUEST_CODE) {
            if (oldCurrency != -1) {
                if (oldCurrency != settings.getInt("currency", Consts.Settings.CURRENCY_DEFAULT)) {
                    loadGlobalInfo(true);
                    loadCoins(true);
                }
            }
            if (oldLanguage != -1 && oldTheme != -1) {
                if (
                    oldLanguage != settings.getInt("language", Consts.Settings.LANGUAGE_DEFAULT) ||
                    oldTheme != settings.getInt("theme", Consts.Settings.THEME_DEFAULT)
                ) {
                    handler.post(() -> recreate());
                }
            }
        }
    }

    private void loadGlobalInfo(boolean loadFromCache) {
        FetchDataTask.with(this).load("https://api.coingecko.com/api/v3/global").loadFromCache(loadFromCache).saveToCache(true).then(data -> {
            try {
                var settingsEditor = settings.edit();
                settingsEditor.putLong("global_load_time", System.currentTimeMillis());
                settingsEditor.apply();

                var jsonData = new JSONObject(new String(data, "UTF-8")).getJSONObject("data");

                ((TextView)globalInfo.findViewById(R.id.global_info_marketcap)).setText(getResources().getString(R.string.main_global_marketcap) + ": " +
                    Coin.formatMoney(this, jsonData.getJSONObject("total_market_cap").getDouble(Consts.Settings.CURRENCY_NAMES[settings.getInt("currency", Consts.Settings.CURRENCY_DEFAULT)])));

                var marketcapChange = jsonData.getDouble("market_cap_change_percentage_24h_usd");
                var marketcapChangeLabel = (TextView)globalInfo.findViewById(R.id.global_info_marketcap_change);
                if (marketcapChange > 0) {
                    marketcapChangeLabel.setTextColor(Utils.contextGetColor(this, R.color.positive_color));
                } else {
                    if (marketcapChange < 0) {
                        marketcapChangeLabel.setTextColor(Utils.contextGetColor(this, R.color.negative_color));
                    } else {
                        marketcapChangeLabel.setTextColor(Utils.contextGetColor(this, R.color.secondary_text_color));
                    }
                }
                marketcapChangeLabel.setText(Coin.formatChangePercent(marketcapChange));

                ((TextView)globalInfo.findViewById(R.id.global_info_volume)).setText(getResources().getString(R.string.main_global_volume) + ": " +
                    Coin.formatMoney(this, jsonData.getJSONObject("total_volume").getDouble(Consts.Settings.CURRENCY_NAMES[settings.getInt("currency", Consts.Settings.CURRENCY_DEFAULT)])));

                ((TextView)globalInfo.findViewById(R.id.global_info_dominance)).setText(getResources().getString(R.string.main_global_dominance) + ": " +
                    "BTC " + Coin.formatPercent(jsonData.getJSONObject("market_cap_percentage").getDouble("btc")) + "  " +
                    "ETH " + Coin.formatPercent(jsonData.getJSONObject("market_cap_percentage").getDouble("eth")));
            } catch (Exception exception) {
                Log.e(getPackageName(), "Can't parse global data", exception);
            }
        }).fetch();
    }

    private void loadCoins(boolean loadFromCache) {
        String url = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=" + Consts.Settings.CURRENCY_NAMES[settings.getInt("currency", Consts.Settings.CURRENCY_DEFAULT)];
        FetchDataTask.with(this).load(url).loadFromCache(loadFromCache).saveToCache(true).then(data -> {
            try {
                var settingsEditor = settings.edit();
                settingsEditor.putLong("coins_load_time", System.currentTimeMillis());
                settingsEditor.apply();

                coinsAdapter.clear();
                var jsonStarredCoins = new JSONArray(settings.getString("starred_coins", "[]"));
                var jsonCoins = new JSONArray(new String(data, "UTF-8"));
                for (int i = 0; i < jsonCoins.length(); i++) {
                    JSONObject jsonCoin = jsonCoins.getJSONObject(i);

                    var isStarred = false;
                    for (int j = 0; j < jsonStarredCoins.length(); j++) {
                        if (jsonCoin.getString("id").equals(jsonStarredCoins.getString(j))) {
                            isStarred = true;
                            break;
                        }
                    }
                    if (starredOnly && !isStarred) {
                        continue;
                    }

                    coinsAdapter.add(new Coin(
                        jsonCoin.getString("id"),
                        jsonCoin.getInt("market_cap_rank"),
                        jsonCoin.getString("name"),
                        jsonCoin.getString("image"),
                        jsonCoin.getDouble("current_price"),
                        jsonCoin.getDouble("price_change_percentage_24h"),
                        jsonCoin.getDouble("market_cap"),
                        jsonCoin.getDouble("total_volume"),
                        jsonCoin.getDouble("circulating_supply"),
                        isStarred
                    ));
                }
            } catch (Exception exception) {
                Log.e(getPackageName(), "Can't parse coins data", exception);
            }
        }).fetch();
    }
}
