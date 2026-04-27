package com.watnapp.etipitaka.plus.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns

class HistoryItem : ModelBase() {
    private var id = 0
    private var historyId = 0
    private var volume = 0
    private var page = 0
    private var status = 0

    override fun getId(): Int = id

    override fun setId(id: Int) {
        this.id = id
    }

    fun getHistoryId(): Int = historyId

    fun setHistoryId(historyId: Int) {
        this.historyId = historyId
    }

    fun getVolume(): Int = volume

    fun setVolume(volume: Int) {
        this.volume = volume
    }

    fun getPage(): Int = page

    fun setPage(page: Int) {
        this.page = page
    }

    fun getStatus(): Status = Status.values()[status]

    fun setStatus(status: Status) {
        this.status = status.ordinal
    }

    override fun fromCursor(cursor: Cursor, context: Context) {
        id = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID))
        historyId = cursor.getInt(cursor.getColumnIndexOrThrow(HistoryItemTable.HistoryItemColumns.HISTORY_ID))
        volume = cursor.getInt(cursor.getColumnIndexOrThrow(HistoryItemTable.HistoryItemColumns.VOLUME))
        page = cursor.getInt(cursor.getColumnIndexOrThrow(HistoryItemTable.HistoryItemColumns.PAGE))
        status = cursor.getInt(cursor.getColumnIndexOrThrow(HistoryItemTable.HistoryItemColumns.STATUS))
    }

    override fun toContentValues(): ContentValues =
        ContentValues().apply {
            put(HistoryItemTable.HistoryItemColumns.HISTORY_ID, historyId)
            put(HistoryItemTable.HistoryItemColumns.VOLUME, volume)
            put(HistoryItemTable.HistoryItemColumns.PAGE, page)
            put(HistoryItemTable.HistoryItemColumns.STATUS, status)
        }

    enum class Status(val code: Int) {
        NONE(0),
        READ(1),
        SKIMMED(2)
    }

    companion object {
        @JvmStatic
        fun newInstance(cursor: Cursor, context: Context): HistoryItem =
            HistoryItem().apply { fromCursor(cursor, context) }
    }
}
