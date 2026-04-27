package com.watnapp.etipitaka.plus.model

import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns

object HistoryItemTable {
    const val TABLE_NAME = "history_item_table"

    object HistoryItemColumns : BaseColumns {
        const val HISTORY_ID = "history_id_column"
        const val VOLUME = "volume_column"
        const val PAGE = "page_column"
        const val STATUS = "status_column"
    }

    @JvmStatic
    fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_NAME (
            ${BaseColumns._ID} INTEGER PRIMARY KEY,
            ${HistoryItemColumns.HISTORY_ID} INTEGER,
            ${HistoryItemColumns.VOLUME} INTEGER,
            ${HistoryItemColumns.PAGE} INTEGER,
            ${HistoryItemColumns.STATUS} INTEGER
            );
            """.trimIndent()
        )
    }

    @JvmStatic
    fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }
}
