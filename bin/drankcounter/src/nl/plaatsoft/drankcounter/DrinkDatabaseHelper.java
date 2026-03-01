/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.drankcounter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import nl.plaatsoft.drankcounter.models.Drink;

public class DrinkDatabaseHelper {
    private final DrinkDatabase dbHelper;

    public DrinkDatabaseHelper(Context context) {
        this.dbHelper = new DrinkDatabase(context);
    }

    public long insertDrink(int type, long createdAt) {
        var db = dbHelper.getWritableDatabase();
        var values = new ContentValues();
        values.put(DrinkDatabase.COLUMN_TYPE, type);
        values.put(DrinkDatabase.COLUMN_CREATED_AT, createdAt);
        return db.insert(DrinkDatabase.TABLE_DRINKS, null, values);
    }

    public List<Drink> getAllDrinks() {
        var db = dbHelper.getReadableDatabase();
        var drinks = new ArrayList<Drink>();
        var cursor = db.query(
            DrinkDatabase.TABLE_DRINKS, null, null, null, null, null, DrinkDatabase.COLUMN_CREATED_AT + " DESC");
        if (cursor.moveToFirst()) {
            do {
                var id = cursor.getLong(cursor.getColumnIndexOrThrow(DrinkDatabase.COLUMN_ID));
                var type = cursor.getInt(cursor.getColumnIndexOrThrow(DrinkDatabase.COLUMN_TYPE));
                var createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(DrinkDatabase.COLUMN_CREATED_AT));
                drinks.add(new Drink(id, type, createdAt));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return drinks;
    }

    public List<Drink> getTodaysDrinks() {
        var cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        var startOfDay = cal.getTimeInMillis() / 1000;
        var db = dbHelper.getReadableDatabase();
        var drinks = new ArrayList<Drink>();
        var cursor = db.query(DrinkDatabase.TABLE_DRINKS, null, DrinkDatabase.COLUMN_CREATED_AT + " >= ?",
            new String[] {String.valueOf(startOfDay)}, null, null, DrinkDatabase.COLUMN_CREATED_AT + " DESC");
        if (cursor.moveToFirst()) {
            do {
                var id = cursor.getLong(cursor.getColumnIndexOrThrow(DrinkDatabase.COLUMN_ID));
                var type = cursor.getInt(cursor.getColumnIndexOrThrow(DrinkDatabase.COLUMN_TYPE));
                var createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(DrinkDatabase.COLUMN_CREATED_AT));
                drinks.add(new Drink(id, type, createdAt));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return drinks;
    }

    public static int countByType(List<Drink> drinks, int type) {
        var count = 0;
        for (var drink : drinks) {
            if (drink.type() == type)
                count++;
        }
        return count;
    }

    public void deleteDrink(long id) {
        var db = dbHelper.getWritableDatabase();
        db.delete(DrinkDatabase.TABLE_DRINKS, DrinkDatabase.COLUMN_ID + " = ?", new String[] {String.valueOf(id)});
    }

    public void close() {
        dbHelper.close();
    }
}
