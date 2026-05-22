package com.watnapp.etipitaka.plus.account

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserDataListParserTest {

    private fun wrap(innerJsonArray: String): String =
        JSONObject().put("items", innerJsonArray).toString()

    @Test
    fun parsesSingleRow_withNestedFields() {
        val inner = """
            [{"model":"user_data.userdata","pk":7,
              "fields":{"user":2,"platform":"android",
                        "file":"alice/android/edata-2026-05-20.js",
                        "deleted":false,
                        "created_at":"2026-05-20T08:30:00Z"}}]
        """.trimIndent()

        val backups = parseUserDataList(wrap(inner))

        assertEquals(1, backups.size)
        val b = backups[0]
        assertEquals(7, b.pk)
        assertEquals("edata-2026-05-20.js", b.filename)
        assertEquals("android", b.platform)
        assertEquals("2026-05-20T08:30:00Z", b.createdAt)
    }

    @Test
    fun parsesMultipleRows_acrossPlatforms() {
        val inner = """
            [{"pk":1,"fields":{"platform":"ios","file":"u/ios/a.json","created_at":"x"}},
             {"pk":2,"fields":{"platform":"android","file":"u/android/b.js","created_at":"y"}}]
        """.trimIndent()

        val backups = parseUserDataList(wrap(inner))

        assertEquals(2, backups.size)
        assertEquals("ios", backups[0].platform)
        assertEquals("android", backups[1].platform)
        assertEquals("b.js", backups[1].filename)
    }

    @Test
    fun emptyList_returnsEmpty() {
        assertTrue(parseUserDataList(wrap("[]")).isEmpty())
    }

    @Test
    fun malformedBody_returnsEmpty() {
        assertTrue(parseUserDataList("not json").isEmpty())
    }
}
