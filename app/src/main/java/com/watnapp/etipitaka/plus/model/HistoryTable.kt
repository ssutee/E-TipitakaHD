package com.watnapp.etipitaka.plus.model

import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns

object HistoryTable {
    const val TABLE_NAME = "history_table"

    object HistoryColumns : BaseColumns {
        const val KEYWORDS = "keywords_column"
        const val LANGUAGE = "language_column"
        const val SECTION1 = "section1_column"
        const val SECTION2 = "section2_column"
        const val SECTION3 = "section3_column"
        const val RESULT1 = "result1_column"
        const val RESULT2 = "result2_column"
        const val RESULT3 = "result3_column"
        const val SCORE = "score_column"
        const val CONTENT = "content_column"
        const val BUDDHAWAJ = "buddhawaj_column"
    }

    @JvmStatic
    fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_NAME (
            ${BaseColumns._ID} INTEGER PRIMARY KEY,
            ${HistoryColumns.KEYWORDS} TEXT,
            ${HistoryColumns.LANGUAGE} INTEGER,
            ${HistoryColumns.SECTION1} BOOLEAN,
            ${HistoryColumns.SECTION2} BOOLEAN,
            ${HistoryColumns.SECTION3} BOOLEAN,
            ${HistoryColumns.RESULT1} INTEGER,
            ${HistoryColumns.RESULT2} INTEGER,
            ${HistoryColumns.RESULT3} INTEGER,
            ${HistoryColumns.SCORE} INTEGER,
            ${HistoryColumns.BUDDHAWAJ} BOOLEAN,
            ${HistoryColumns.CONTENT} TEXT
            );
            """.trimIndent()
        )
    }

    @JvmStatic
    fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if ((oldVersion == 1 || oldVersion == 2) && (newVersion == 2 || newVersion == 3)) {
            try {
                db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN ${HistoryColumns.BUDDHAWAJ} BOOLEAN;")
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
        }
    }
}
