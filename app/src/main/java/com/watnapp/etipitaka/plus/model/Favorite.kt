package com.watnapp.etipitaka.plus.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper

class Favorite : ModelBase() {
    private var id = 0
    private var note: String? = null
    private var language = 0
    private var volume = 0
    private var page = 0
    private var item = 0
    private var score = 0

    override fun getId(): Int = id

    override fun setId(id: Int) {
        this.id = id
    }

    fun getNote(): String? = note

    fun setNote(note: String?) {
        this.note = note
    }

    fun getLanguage(): BookDatabaseHelper.Language = BookDatabaseHelper.Language.values()[language]

    fun setLanguage(language: BookDatabaseHelper.Language) {
        this.language = language.ordinal
    }

    fun getVolume(): Int = volume

    fun setVolume(volume: Int) {
        this.volume = volume
    }

    fun getPage(): Int = page

    fun setPage(page: Int) {
        this.page = page
    }

    fun getItem(): Int = item

    fun setItem(item: Int) {
        this.item = item
    }

    fun getScore(): Int = score

    fun setScore(score: Int) {
        this.score = score
    }

    override fun fromCursor(cursor: Cursor, context: Context) {
        id = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID))
        note = cursor.getString(cursor.getColumnIndexOrThrow(FavoriteTable.FavoriteColumns.NOTE))
        language = cursor.getInt(cursor.getColumnIndexOrThrow(FavoriteTable.FavoriteColumns.LANGUAGE))
        volume = cursor.getInt(cursor.getColumnIndexOrThrow(FavoriteTable.FavoriteColumns.VOLUME))
        page = cursor.getInt(cursor.getColumnIndexOrThrow(FavoriteTable.FavoriteColumns.PAGE))
        item = cursor.getInt(cursor.getColumnIndexOrThrow(FavoriteTable.FavoriteColumns.ITEM))
        score = cursor.getInt(cursor.getColumnIndexOrThrow(FavoriteTable.FavoriteColumns.SCORE))
    }

    override fun toContentValues(): ContentValues =
        ContentValues().apply {
            put(FavoriteTable.FavoriteColumns.NOTE, note)
            put(FavoriteTable.FavoriteColumns.LANGUAGE, language)
            put(FavoriteTable.FavoriteColumns.VOLUME, volume)
            put(FavoriteTable.FavoriteColumns.PAGE, page)
            put(FavoriteTable.FavoriteColumns.ITEM, item)
            put(FavoriteTable.FavoriteColumns.SCORE, score)
        }

    companion object {
        @JvmStatic
        fun newInstance(cursor: Cursor, context: Context): Favorite =
            Favorite().apply { fromCursor(cursor, context) }
    }
}
