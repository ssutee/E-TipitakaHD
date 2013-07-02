package com.watnapp.etipitaka.model;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;


public final class HistoryTable {
    public static final String TABLE_NAME = "history_table";

    public static class HistoryColumns implements BaseColumns {
        public static final String KEYWORDS = "keywords_column";
        public static final String LANGUAGE = "language_column";
        public static final String SECTION1 = "section1_column";
        public static final String SECTION2 = "section2_column";
        public static final String SECTION3 = "section3_column";
        public static final String RESULT1 = "result1_column";
        public static final String RESULT2 = "result2_column";
        public static final String RESULT3 = "result3_column";
        public static final String CONTENT = "content_column";
    }



    public static void onCreate(SQLiteDatabase db) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE " + HistoryTable.TABLE_NAME + " (");
        sb.append(BaseColumns._ID + " INTEGER PRIMARY KEY, ");
        sb.append(HistoryColumns.KEYWORDS + " TEXT, ");
        sb.append(HistoryColumns.LANGUAGE + " INTEGER, ");
        sb.append(HistoryColumns.SECTION1 + " BOOLEAN, ");
        sb.append(HistoryColumns.SECTION2 + " BOOLEAN, ");
        sb.append(HistoryColumns.SECTION3 + " BOOLEAN, ");
        sb.append(HistoryColumns.RESULT1 + " INTEGER, ");
        sb.append(HistoryColumns.RESULT2 + " INTEGER, ");
        sb.append(HistoryColumns.RESULT3 + " INTEGER, ");
        sb.append(HistoryColumns.CONTENT + " TEXT");
        sb.append(");");
        db.execSQL(sb.toString());
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + HistoryTable.TABLE_NAME);
        HistoryTable.onCreate(db);
    }


}