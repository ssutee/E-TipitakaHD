package com.watnapp.etipitaka.plus.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import android.util.SparseBooleanArray
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper

class History : ModelBase() {
    private var id = 0
    private var keywords: String? = null
    private var language = 0
    private var section1 = false
    private var section2 = false
    private var section3 = false
    private var result1 = 0
    private var result2 = 0
    private var result3 = 0
    private var content: String? = null
    private var score = 0
    private var buddhawaj = false

    override fun getId(): Int = id

    override fun setId(id: Int) {
        this.id = id
    }

    fun getKeywords(): String? = keywords

    fun setKeywords(keywords: String?) {
        this.keywords = keywords
    }

    fun getLanguage(): BookDatabaseHelper.Language = BookDatabaseHelper.Language.values()[language]

    fun setLanguage(language: BookDatabaseHelper.Language) {
        this.language = language.ordinal
    }

    fun isSection1(): Boolean = section1

    fun setSection1(section1: Boolean) {
        this.section1 = section1
    }

    fun isSection2(): Boolean = section2

    fun setSection2(section2: Boolean) {
        this.section2 = section2
    }

    fun isSection3(): Boolean = section3

    fun setSection3(section3: Boolean) {
        this.section3 = section3
    }

    fun setSections(sections: SparseBooleanArray?) {
        setSection1(sections?.get(0, false) ?: false)
        setSection2(sections?.get(1, false) ?: false)
        setSection3(sections?.get(2, false) ?: false)
    }

    fun setResults(results: IntArray) {
        setResult1(results.getOrElse(0) { 0 })
        setResult2(results.getOrElse(1) { 0 })
        setResult3(results.getOrElse(2) { 0 })
    }

    fun getResult1(): Int = result1

    fun setResult1(result1: Int) {
        this.result1 = result1
    }

    fun getResult2(): Int = result2

    fun setResult2(result2: Int) {
        this.result2 = result2
    }

    fun getResult3(): Int = result3

    fun setResult3(result3: Int) {
        this.result3 = result3
    }

    fun getContent(): String? = content

    fun setContent(content: String?) {
        this.content = content
    }

    fun getScore(): Int = score

    fun setScore(score: Int) {
        this.score = score
    }

    fun isBuddhawaj(): Boolean = buddhawaj

    fun setBuddhawaj(buddhawaj: Boolean) {
        this.buddhawaj = buddhawaj
    }

    override fun fromCursor(cursor: Cursor, context: Context) {
        id = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID))
        keywords = cursor.getString(cursor.getColumnIndexOrThrow(HistoryTable.HistoryColumns.KEYWORDS))
        language = cursor.getInt(cursor.getColumnIndexOrThrow(HistoryTable.HistoryColumns.LANGUAGE))
        section1 = cursor.getInt(cursor.getColumnIndexOrThrow(HistoryTable.HistoryColumns.SECTION1)) == 1
        section2 = cursor.getInt(cursor.getColumnIndexOrThrow(HistoryTable.HistoryColumns.SECTION2)) == 1
        section3 = cursor.getInt(cursor.getColumnIndexOrThrow(HistoryTable.HistoryColumns.SECTION3)) == 1
        result1 = cursor.getInt(cursor.getColumnIndexOrThrow(HistoryTable.HistoryColumns.RESULT1))
        result2 = cursor.getInt(cursor.getColumnIndexOrThrow(HistoryTable.HistoryColumns.RESULT2))
        result3 = cursor.getInt(cursor.getColumnIndexOrThrow(HistoryTable.HistoryColumns.RESULT3))
        score = cursor.getInt(cursor.getColumnIndexOrThrow(HistoryTable.HistoryColumns.SCORE))
        content = cursor.getString(cursor.getColumnIndexOrThrow(HistoryTable.HistoryColumns.CONTENT))
        buddhawaj = cursor.getInt(cursor.getColumnIndexOrThrow(HistoryTable.HistoryColumns.BUDDHAWAJ)) == 1
    }

    override fun toContentValues(): ContentValues =
        ContentValues().apply {
            put(HistoryTable.HistoryColumns.KEYWORDS, keywords)
            put(HistoryTable.HistoryColumns.LANGUAGE, language)
            put(HistoryTable.HistoryColumns.SECTION1, section1)
            put(HistoryTable.HistoryColumns.SECTION2, section2)
            put(HistoryTable.HistoryColumns.SECTION3, section3)
            put(HistoryTable.HistoryColumns.RESULT1, result1)
            put(HistoryTable.HistoryColumns.RESULT2, result2)
            put(HistoryTable.HistoryColumns.RESULT3, result3)
            put(HistoryTable.HistoryColumns.SCORE, score)
            put(HistoryTable.HistoryColumns.CONTENT, content)
            put(HistoryTable.HistoryColumns.BUDDHAWAJ, buddhawaj)
        }

    companion object {
        @JvmStatic
        fun newInstance(cursor: Cursor, context: Context): History =
            History().apply { fromCursor(cursor, context) }
    }
}
