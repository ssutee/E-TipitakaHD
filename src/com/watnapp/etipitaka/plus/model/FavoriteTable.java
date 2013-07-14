package com.watnapp.etipitaka.plus.model;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;


public final class FavoriteTable {
    public static final String TABLE_NAME = "favorite_table";

    public static class FavoriteColumns implements BaseColumns {
        public static final String NOTE = "note_column";
        public static final String LANGUAGE = "language_column";
        public static final String VOLUME = "volume_column";
        public static final String PAGE = "page_column";
        public static final String ITEM = "item_column";
    }



    public static void onCreate(SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE " + FavoriteTable.TABLE_NAME + " (");
        sb.append(BaseColumns._ID + " INTEGER PRIMARY KEY, ");
        sb.append(FavoriteColumns.NOTE + " TEXT, ");
        sb.append(FavoriteColumns.LANGUAGE + " INTEGER, ");
        sb.append(FavoriteColumns.VOLUME + " INTEGER, ");
        sb.append(FavoriteColumns.PAGE + " INTEGER, ");
        sb.append(FavoriteColumns.ITEM + " INTEGER");
        sb.append(");");
        db.execSQL(sb.toString());
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavoriteTable.TABLE_NAME);
        FavoriteTable.onCreate(db);
    }


}