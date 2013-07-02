package com.watnapp.etipitaka.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "data.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        HistoryTable.onCreate(db);
        FavoriteTable.onCreate(db);
        HistoryItemTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        HistoryTable.onUpgrade(db, oldVersion, newVersion);
        FavoriteTable.onUpgrade(db, oldVersion, newVersion);
        HistoryItemTable.onUpgrade(db, oldVersion, newVersion);
    }


}