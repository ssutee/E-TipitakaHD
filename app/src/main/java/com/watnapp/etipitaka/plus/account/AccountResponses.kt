package com.watnapp.etipitaka.plus.account

import org.json.JSONArray
import org.json.JSONObject

/** Parses `{"key":"<token>"}` from POST /rest-auth/login/. Returns null on failure. */
fun parseLoginResponse(body: String): String? = try {
    val obj = JSONObject(body)
    if (obj.has("key")) obj.getString("key") else null
} catch (e: Exception) {
    null
}

/** Parses the JSON body of POST /upload/ into a domain outcome. */
fun parseUploadResponse(body: String): UploadOutcome = try {
    val obj = JSONObject(body)
    when {
        obj.optBoolean("file_exists", false) -> UploadOutcome.FILE_EXISTS
        obj.optBoolean("success", false) -> UploadOutcome.SUCCESS
        else -> UploadOutcome.FAILED
    }
} catch (e: Exception) {
    UploadOutcome.FAILED
}

/**
 * Parses GET /user_data_list/. The body is `{"items":"<json-encoded-string>"}` —
 * `items` is a JSON string that must be parsed a second time into an array of
 * Django-serialized rows: `{"pk":N,"fields":{...}}`.
 */
fun parseUserDataList(body: String): List<ServerBackup> {
    val result = mutableListOf<ServerBackup>()
    try {
        val itemsString = JSONObject(body).getString("items")
        val array = JSONArray(itemsString)
        for (i in 0 until array.length()) {
            val row = array.getJSONObject(i)
            val fields = row.getJSONObject("fields")
            val file = fields.getString("file")
            result.add(
                ServerBackup(
                    pk = row.getInt("pk"),
                    filename = file.substringAfterLast('/'),
                    platform = fields.optString("platform", "unknown"),
                    createdAt = fields.optString("created_at", ""),
                )
            )
        }
    } catch (e: Exception) {
        // Malformed list — treat as empty.
    }
    return result
}
