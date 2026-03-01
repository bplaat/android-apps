/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.drankcounter.components;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import nl.plaatsoft.drankcounter.DrinkDatabaseHelper;
import nl.plaatsoft.drankcounter.R;
import nl.plaatsoft.drankcounter.Settings;
import nl.plaatsoft.drankcounter.models.Drink;

import org.jspecify.annotations.Nullable;

public class DrinkAdapter extends BaseAdapter {
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_DRINK = 1;

    public interface OnDeleteListener {
        void onDrinkDeleted();
    }

    private static class DrinkViewHolder {
        public @SuppressWarnings("null") ImageView drinkIcon;
        public @SuppressWarnings("null") TextView drinkType;
        public @SuppressWarnings("null") TextView drinkTime;
        public @SuppressWarnings("null") ImageButton drinkMenuButton;
    }

    private final Context context;
    private final DrinkDatabaseHelper dbHelper;
    private final SimpleDateFormat dayKeyFormat;
    private final SimpleDateFormat dayDisplayFormat;
    private final SimpleDateFormat timeFormat;
    private final String todayLabel;
    private final String yesterdayLabel;
    private final List<Object> items = new ArrayList<>();
    private @Nullable OnDeleteListener onDeleteListener;

    public DrinkAdapter(Context context, DrinkDatabaseHelper dbHelper) {
        this.context = context;
        this.dbHelper = dbHelper;
        var settings = new Settings(context);
        var locale = getLocaleFromSettings(settings);
        this.dayKeyFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        this.dayDisplayFormat = new SimpleDateFormat("d MMMM", locale);
        this.timeFormat = new SimpleDateFormat("HH:mm", locale);
        this.todayLabel = context.getString(R.string.day_header_today);
        this.yesterdayLabel = context.getString(R.string.day_header_yesterday);
    }

    private Locale getLocaleFromSettings(Settings settings) {
        return switch (settings.getLanguage()) {
            case Settings.LANGUAGE_ENGLISH -> Locale.forLanguageTag("en");
            case Settings.LANGUAGE_DUTCH -> Locale.forLanguageTag("nl");
            default -> Locale.getDefault();
        };
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.onDeleteListener = listener;
    }

    public void setDrinks(List<Drink> drinks) {
        items.clear();
        var today = dayKeyFormat.format(new Date());
        var cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        var yesterday = dayKeyFormat.format(cal.getTime());

        String lastDayKey = null;
        for (var drink : drinks) {
            var date = new Date(drink.createdAt() * 1000);
            var dayKey = dayKeyFormat.format(date);
            if (!dayKey.equals(lastDayKey)) {
                if (dayKey.equals(today)) {
                    items.add(todayLabel);
                } else if (dayKey.equals(yesterday)) {
                    items.add(yesterdayLabel);
                } else {
                    items.add(dayDisplayFormat.format(date));
                }
                lastDayKey = dayKey;
            }
            items.add(drink);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof Drink ? VIEW_TYPE_DRINK : VIEW_TYPE_HEADER;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    private int getIconResource(int type) {
        return switch (type) {
            case Drink.TYPE_BEER -> R.drawable.ic_glass_mug;
            case Drink.TYPE_WINE -> R.drawable.ic_glass_wine;
            case Drink.TYPE_LIQUEUR -> R.drawable.ic_cup;
            default -> 0;
        };
    }

    private String getTypeString(int type) {
        return switch (type) {
            case Drink.TYPE_BEER -> context.getString(R.string.drink_type_beer);
            case Drink.TYPE_WINE -> context.getString(R.string.drink_type_wine);
            case Drink.TYPE_LIQUEUR -> context.getString(R.string.drink_type_liqueur);
            default -> "Unknown";
        };
    }

    @Override
    public View getView(int position, @Nullable View convertView, @SuppressWarnings("null") ViewGroup parent) {
        if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_drink_section_header, parent, false);
            }
            ((TextView)Objects.requireNonNull(convertView)).setText((String)items.get(position));
            return convertView;
        }

        DrinkViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_drink, parent, false);
            viewHolder = new DrinkViewHolder();
            viewHolder.drinkIcon = Objects.requireNonNull(convertView).findViewById(R.id.drink_icon);
            viewHolder.drinkType = convertView.findViewById(R.id.drink_type);
            viewHolder.drinkTime = convertView.findViewById(R.id.drink_datetime);
            viewHolder.drinkMenuButton = convertView.findViewById(R.id.drink_menu_button);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (DrinkViewHolder)convertView.getTag();
        }

        var drink = (Drink)items.get(position);
        viewHolder.drinkIcon.setImageResource(getIconResource(drink.type()));
        viewHolder.drinkType.setText(getTypeString(drink.type()));
        viewHolder.drinkTime.setText(timeFormat.format(new Date(drink.createdAt() * 1000)));

        viewHolder.drinkMenuButton.setOnClickListener(view -> {
            var menu = new PopupMenu(context, view, Gravity.END);
            menu.getMenuInflater().inflate(R.menu.item_drink_options, menu.getMenu());
            menu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_item_drink_delete) {
                    dbHelper.deleteDrink(drink.id());
                    if (onDeleteListener != null) {
                        onDeleteListener.onDrinkDeleted();
                    }
                    return true;
                }
                return false;
            });
            menu.show();
        });

        return convertView;
    }
}
