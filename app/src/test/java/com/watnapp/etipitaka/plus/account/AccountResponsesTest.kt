package com.watnapp.etipitaka.plus.account

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AccountResponsesTest {

    @Test
    fun parseLoginResponse_returnsToken_onSuccessBody() {
        val body = JSONObject().put("key", "abc123").toString()
        assertEquals("abc123", parseLoginResponse(body))
    }

    @Test
    fun parseLoginResponse_returnsNull_whenNoKey() {
        val body = JSONObject().put("non_field_errors", "Login failed").toString()
        assertNull(parseLoginResponse(body))
    }

    @Test
    fun parseLoginResponse_returnsNull_onGarbage() {
        assertNull(parseLoginResponse("not json"))
    }

    @Test
    fun parseUploadResponse_success() {
        val body = JSONObject().put("success", true).put("pk", 7).toString()
        assertEquals(UploadOutcome.SUCCESS, parseUploadResponse(body))
    }

    @Test
    fun parseUploadResponse_fileExists() {
        val body = JSONObject().put("file_exists", true).toString()
        assertEquals(UploadOutcome.FILE_EXISTS, parseUploadResponse(body))
    }

    @Test
    fun parseUploadResponse_failed() {
        val body = JSONObject().put("success", false).toString()
        assertEquals(UploadOutcome.FAILED, parseUploadResponse(body))
    }

    @Test
    fun parseUploadResponse_garbage_isFailed() {
        assertEquals(UploadOutcome.FAILED, parseUploadResponse("not json"))
    }
}
