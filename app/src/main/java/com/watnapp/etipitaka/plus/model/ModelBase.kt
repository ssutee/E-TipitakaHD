package com.watnapp.etipitaka.plus.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor

abstract class ModelBase {
    abstract fun fromCursor(cursor: Cursor, context: Context)
    abstract fun getId(): Int
    abstract fun setId(id: Int)
    abstract fun toContentValues(): ContentValues
}
