/*
 * Copyright (c) 2026 Bastiaan van der Plaat
 *
 * SPDX-License-Identifier: MIT
 */

package nl.plaatsoft.drankcounter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DrinkDatabase extends SQLiteOpenHelper {
    private static final String DB_NAME = "drankcounter.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_DRINKS = "drinks";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_CREATED_AT = "created_at";

    public DrinkDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        var createTableSQL = "CREATE TABLE " + TABLE_DRINKS + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_TYPE + " INTEGER NOT NULL, " + COLUMN_CREATED_AT + " INTEGER NOT NULL)";
        db.execSQL(createTableSQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No upgrades for now
    }
}
