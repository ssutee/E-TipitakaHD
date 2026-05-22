package com.watnapp.etipitaka.plus.account

import com.watnapp.etipitaka.plus.model.FavoriteDaoHelper
import com.watnapp.etipitaka.plus.model.FavoriteTable
import com.watnapp.etipitaka.plus.model.HistoryDaoHelper
import com.watnapp.etipitaka.plus.model.HistoryTable
import org.json.JSONException
import org.json.JSONObject

/** Builds the user-data export JSON: `{"favorite_table":[...],"history_table":[...]}`. */
class UserDataExporter(
    private val favoriteDaoHelper: FavoriteDaoHelper,
    private val historyDaoHelper: HistoryDaoHelper,
) {
    @Throws(JSONException::class)
    fun buildExportJson(): String {
        val json = JSONObject()
        json.put(FavoriteTable.TABLE_NAME, favoriteDaoHelper.dumpJSONArray())
        json.put(HistoryTable.TABLE_NAME, historyDaoHelper.dumpJSONArray())
        return json.toString()
    }
}

/**
 * Restores an Android export JSON into the local database. `restoreJSONArray`
 * already merges by natural key (inserts only rows not already present), so
 * re-importing the same backup is idempotent.
 */
class UserDataImporter(
    private val favoriteDaoHelper: FavoriteDaoHelper,
    private val historyDaoHelper: HistoryDaoHelper,
) {
    @Throws(JSONException::class)
    fun importAndroidJson(json: String) {
        val obj = JSONObject(json)
        favoriteDaoHelper.restoreJSONArray(obj.getJSONArray(FavoriteTable.TABLE_NAME))
        historyDaoHelper.restoreJSONArray(obj.getJSONArray(HistoryTable.TABLE_NAME))
    }
}
