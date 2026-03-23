/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.drankcounter.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import nl.plaatsoft.android.alerts.RatingAlert;
import nl.plaatsoft.android.alerts.UpdateAlert;
import nl.plaatsoft.android.compat.ContextCompat;
import nl.plaatsoft.android.fetch.FetchDataTask;
import nl.plaatsoft.android.fetch.FetchImageTask;
import nl.plaatsoft.drankcounter.DrinkDatabaseHelper;
import nl.plaatsoft.drankcounter.R;
import nl.plaatsoft.drankcounter.WidgetProvider;
import nl.plaatsoft.drankcounter.components.DrinkAdapter;
import nl.plaatsoft.drankcounter.models.Drink;

import org.jspecify.annotations.Nullable;

public class MainActivity extends BaseActivity implements PopupMenu.OnMenuItemClickListener {
    private static final int SETTINGS_REQUEST_CODE = 1;

    private Handler handler = new Handler(Looper.getMainLooper());
    private int oldLanguage = -1;
    private int oldTheme = -1;

    private DrinkDatabaseHelper dbHelper;
    private DrinkAdapter drinkAdapter;
    private ListView drinkList;
    private TextView emptyText;
    private TextView beerCountView;
    private TextView wineCountView;
    private TextView liqueurCountView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database
        dbHelper = new DrinkDatabaseHelper(this);

        // Setup list with drink buttons as scrollable header
        drinkList = findViewById(R.id.main_drink_list);
        useWindowInsets(drinkList);
        emptyText = findViewById(R.id.main_empty_list);

        var headerView = LayoutInflater.from(this).inflate(R.layout.main_drink_header, drinkList, false);
        drinkList.addHeaderView(headerView, null, false);

        drinkAdapter = new DrinkAdapter(this, dbHelper);
        drinkAdapter.setOnDeleteListener(this::refreshDrinkList);
        drinkList.setAdapter(drinkAdapter);

        beerCountView = headerView.findViewById(R.id.main_beer_count);
        wineCountView = headerView.findViewById(R.id.main_wine_count);
        liqueurCountView = headerView.findViewById(R.id.main_liqueur_count);

        // Options menu button
        findViewById(R.id.main_options_menu_button).setOnClickListener(view -> {
            var optionsMenu = new PopupMenu(this, view, Gravity.TOP | Gravity.RIGHT);
            optionsMenu.getMenuInflater().inflate(R.menu.options, optionsMenu.getMenu());
            optionsMenu.setOnMenuItemClickListener(this);
            optionsMenu.show();
        });

        // Add drink buttons (in scrollable header)
        headerView.findViewById(R.id.main_add_beer_button).setOnClickListener(view -> addDrink(Drink.TYPE_BEER));
        headerView.findViewById(R.id.main_add_wine_button).setOnClickListener(view -> addDrink(Drink.TYPE_WINE));
        headerView.findViewById(R.id.main_add_liqueur_button).setOnClickListener(view -> addDrink(Drink.TYPE_LIQUEUR));

        // Show update alert
        UpdateAlert.checkAndShow(this,
            "https://raw.githubusercontent.com/bplaat/android-apps/refs/heads/master/bin/drankcounter/bob.toml",
            SettingsActivity.STORE_PAGE_URL);
    }

    private void addDrink(int type) {
        dbHelper.insertDrink(type, System.currentTimeMillis() / 1000);
        refreshDrinkList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDrinkList();
    }

    private void refreshDrinkList() {
        var drinks = dbHelper.getAllDrinks();
        var todayDrinks = dbHelper.getTodaysDrinks();

        emptyText.setVisibility(drinks.isEmpty() ? View.VISIBLE : View.GONE);
        drinkAdapter.setDrinks(drinks);

        beerCountView.setText(String.valueOf(DrinkDatabaseHelper.countByType(todayDrinks, Drink.TYPE_BEER)));
        wineCountView.setText(String.valueOf(DrinkDatabaseHelper.countByType(todayDrinks, Drink.TYPE_WINE)));
        liqueurCountView.setText(String.valueOf(DrinkDatabaseHelper.countByType(todayDrinks, Drink.TYPE_LIQUEUR)));

        WidgetProvider.updateAllWidgets(this);
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
                var languageChanged =
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && oldLanguage != settings.getLanguage();
                var themeChanged = oldTheme != settings.getTheme();
                if (languageChanged || themeChanged) {
                    handler.post(() -> recreate());
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
