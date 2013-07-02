package com.watnapp.etipitaka.model;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;


public final class HistoryItemTable {
    public static final String TABLE_NAME = "history_item_table";

    public static class HistoryItemColumns implements BaseColumns {
        public static final String HISTORY_ID = "history_id_column";
        public static final String VOLUME = "volume_column";
        public static final String PAGE = "page_column";
        public static final String STATUS = "status_column";
    }



    public static void onCreate(SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE " + HistoryItemTable.TABLE_NAME + " (");
        sb.append(BaseColumns._ID + " INTEGER PRIMARY KEY, ");
        sb.append(HistoryItemColumns.HISTORY_ID + " INTEGER, ");
        sb.append(HistoryItemColumns.VOLUME + " INTEGER, ");
        sb.append(HistoryItemColumns.PAGE + " INTEGER, ");
        sb.append(HistoryItemColumns.STATUS + " INTEGER");
        sb.append(");");
        db.execSQL(sb.toString());
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + HistoryItemTable.TABLE_NAME);
        HistoryItemTable.onCreate(db);
    }


}