/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.drankcounter;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import nl.plaatsoft.drankcounter.models.Drink;

public class WidgetProvider extends AppWidgetProvider {
    private static final String ACTION_ADD_DRINK = "nl.plaatsoft.drankcounter.ACTION_ADD_DRINK";
    private static final String EXTRA_DRINK_TYPE = "drink_type";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (var id : appWidgetIds) {
            updateWidget(context, appWidgetManager, id);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_ADD_DRINK.equals(intent.getAction())) {
            var type = intent.getIntExtra(EXTRA_DRINK_TYPE, Drink.TYPE_BEER);
            var dbHelper = new DrinkDatabaseHelper(context);
            dbHelper.insertDrink(type, System.currentTimeMillis() / 1000);
            dbHelper.close();
            updateAllWidgets(context);
        }
    }

    public static void updateAllWidgets(Context context) {
        var appWidgetManager = AppWidgetManager.getInstance(context);
        var ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
        for (var id : ids) {
            updateWidget(context, appWidgetManager, id);
        }
    }

    private static void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        var dbHelper = new DrinkDatabaseHelper(context);
        var todayDrinks = dbHelper.getTodaysDrinks();
        dbHelper.close();

        var beerCount = DrinkDatabaseHelper.countByType(todayDrinks, Drink.TYPE_BEER);
        var wineCount = DrinkDatabaseHelper.countByType(todayDrinks, Drink.TYPE_WINE);
        var liqueurCount = DrinkDatabaseHelper.countByType(todayDrinks, Drink.TYPE_LIQUEUR);

        var lightViews = new RemoteViews(context.getPackageName(), R.layout.widget_drankcounter);
        applyCountsAndClicks(context, lightViews, beerCount, wineCount, liqueurCount);

        RemoteViews views;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            var darkViews = new RemoteViews(context.getPackageName(), R.layout.widget_drankcounter);
            applyCountsAndClicks(context, darkViews, beerCount, wineCount, liqueurCount);
            views = new RemoteViews(lightViews, darkViews);
        } else {
            views = lightViews;
        }

        appWidgetManager.updateAppWidget(widgetId, views);
    }

    private static void applyCountsAndClicks(
        Context context, RemoteViews views, int beerCount, int wineCount, int liqueurCount) {
        views.setTextViewText(R.id.widget_beer_count, String.valueOf(beerCount));
        views.setTextViewText(R.id.widget_wine_count, String.valueOf(wineCount));
        views.setTextViewText(R.id.widget_liqueur_count, String.valueOf(liqueurCount));

        views.setOnClickPendingIntent(R.id.widget_beer_button, buildAddDrinkIntent(context, Drink.TYPE_BEER));
        views.setOnClickPendingIntent(R.id.widget_wine_button, buildAddDrinkIntent(context, Drink.TYPE_WINE));
        views.setOnClickPendingIntent(R.id.widget_liqueur_button, buildAddDrinkIntent(context, Drink.TYPE_LIQUEUR));
    }

    private static PendingIntent buildAddDrinkIntent(Context context, int drinkType) {
        var intent = new Intent(context, WidgetProvider.class);
        intent.setAction(ACTION_ADD_DRINK);
        intent.putExtra(EXTRA_DRINK_TYPE, drinkType);
        var flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getBroadcast(context, drinkType, intent, flags);
    }
}
