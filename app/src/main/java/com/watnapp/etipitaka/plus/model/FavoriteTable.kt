package com.watnapp.etipitaka.plus.model

import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns

object FavoriteTable {
    const val TABLE_NAME = "favorite_table"

    object FavoriteColumns : BaseColumns {
        const val NOTE = "note_column"
        const val LANGUAGE = "language_column"
        const val VOLUME = "volume_column"
        const val PAGE = "page_column"
        const val ITEM = "item_column"
        const val SCORE = "score_column"
    }

    @JvmStatic
    fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_NAME (
            ${BaseColumns._ID} INTEGER PRIMARY KEY,
            ${FavoriteColumns.NOTE} TEXT,
            ${FavoriteColumns.LANGUAGE} INTEGER,
            ${FavoriteColumns.VOLUME} INTEGER,
            ${FavoriteColumns.PAGE} INTEGER,
            ${FavoriteColumns.SCORE} INTEGER,
            ${FavoriteColumns.ITEM} INTEGER
            );
            """.trimIndent()
        )
    }

    @JvmStatic
    fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }
}
