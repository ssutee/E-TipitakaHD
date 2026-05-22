# Account Data Sync Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users log into the E-Tipitaka backend, then upload/download their user-data export file (favorites + history) to it from a new in-app Account screen.

**Architecture:** A new `account` package holds an OkHttp-based API client (`AccountApi`), token storage (`SessionManager`), a `ViewModel`, and a Compose UI hosted in `AccountActivity`. The export-JSON build and the Android-JSON import are extracted from `MainActivity` into reusable `UserDataExporter` / `UserDataImporter` units (Koin singles) so both the existing SAF export/import and the new sync flow share them. A new overflow menu item opens `AccountActivity`.

**Tech Stack:** Kotlin, Jetpack Compose + Material3, Koin DI, Kotlin Coroutines, OkHttp 4.12.0. minSdk 21 / targetSdk 36. Backend: Django REST Framework at `https://data.etipitaka.com`.

**Spec:** `docs/superpowers/specs/2026-05-22-account-data-sync-design.md`

**Note on testing:** The spec mentioned unit-testing `SessionManager`. `SessionManager` only wraps `SharedPreferences`, which needs Robolectric (not a current dependency) to test on the JVM. To avoid adding Robolectric, unit tests in this plan cover the pure response-parsing functions only (the real logic). `SessionManager` is verified by manual QA in Task 15.

---

### Task 1: Add OkHttp dependency

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle`

- [ ] **Step 1: Add version + library entries to the version catalog**

In `gradle/libs.versions.toml`, add to the `[versions]` block (after line `koin = "3.3.3"`):

```toml
okhttp = "4.12.0"
org-json = "20231013"
```

In the same file, add to the `[libraries]` block (alphabetically, after the `koin-core` line):

```toml
okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }
org-json = { module = "org.json:json", version.ref = "org-json" }
```

- [ ] **Step 2: Reference the dependencies in the app module**

In `app/build.gradle`, inside the `dependencies { }` block, add the OkHttp implementation line after `implementation libs.kotlinx.coroutines.core`:

```gradle
    implementation libs.okhttp
```

And add the test dependency after `testImplementation libs.junit`:

```gradle
    testImplementation libs.org.json
```

(`org.json:json` supplies a real `JSONObject`/`JSONArray` for JVM unit tests; the `android.jar` stub throws "not mocked".)

- [ ] **Step 3: Verify the build resolves the new dependency**

Run: `./gradlew :app:compileDebugKotlin --rerun-tasks`
Expected: `BUILD SUCCESSFUL`. No "Could not find com.squareup.okhttp3" error.

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle
git commit -m "build: add OkHttp and test-scope org.json dependencies"
```

---

### Task 2: Add server + menu constants

**Files:**
- Modify: `app/src/main/java/com/watnapp/etipitaka/plus/Constants.java`

- [ ] **Step 1: Add the server URL and menu-item constants**

In `Constants.java`, add the menu-item constant immediately after `MENU_ITEM_VERSION = 1023;`:

```java
  public static final int MENU_ITEM_ACCOUNT             = 1024;
```

Add the server URL constants near the existing host constants (`S3_HOST`, `THAI_HOST`, ...):

```java
  public static final String DATA_SERVER_URL = "https://data.etipitaka.com";
  public static final String SIGNUP_URL = DATA_SERVER_URL + "/signup/";
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :app:compileDebugJavaWithJavac`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/watnapp/etipitaka/plus/Constants.java
git commit -m "feat: add data-server URL and account menu constants"
```

---

### Task 3: Account result and model types

**Files:**
- Create: `app/src/main/java/com/watnapp/etipitaka/plus/account/AccountTypes.kt`

- [ ] **Step 1: Create the sealed result type, the upload outcome, and the backup model**

Create `app/src/main/java/com/watnapp/etipitaka/plus/account/AccountTypes.kt`:

```kotlin
package com.watnapp.etipitaka.plus.account

/** Result of one backend call. */
sealed class ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>()

    /** HTTP 401 — token rejected; caller should force-logout. */
    object AuthError : ApiResult<Nothing>()

    /** Could not reach the server (IO failure). */
    object NetworkError : ApiResult<Nothing>()

    /** Reached the server but it returned an error. */
    data class ServerError(val message: String) : ApiResult<Nothing>()
}

/** Domain outcome of a POST /upload/ call (HTTP itself is 200 in all three cases). */
enum class UploadOutcome {
    SUCCESS,
    /** A non-deleted backup with the same name already exists on the server. */
    FILE_EXISTS,
    FAILED,
}

/** One backup file listed by GET /user_data_list/. */
data class ServerBackup(
    val pk: Int,
    val filename: String,
    val platform: String,
    val createdAt: String,
)
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/watnapp/etipitaka/plus/account/AccountTypes.kt
git commit -m "feat: add account result and model types"
```

---

### Task 4: Login and upload response parsers (TDD)

**Files:**
- Create: `app/src/main/java/com/watnapp/etipitaka/plus/account/AccountResponses.kt`
- Test: `app/src/test/java/com/watnapp/etipitaka/plus/account/AccountResponsesTest.kt`

- [ ] **Step 1: Write the failing tests**

Create `app/src/test/java/com/watnapp/etipitaka/plus/account/AccountResponsesTest.kt`:

```kotlin
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
```

- [ ] **Step 2: Run the tests to verify they fail**

Run: `./gradlew :app:testDebugUnitTest --tests "com.watnapp.etipitaka.plus.account.AccountResponsesTest"`
Expected: FAIL — `parseLoginResponse` / `parseUploadResponse` unresolved.

- [ ] **Step 3: Write the parser implementations**

Create `app/src/main/java/com/watnapp/etipitaka/plus/account/AccountResponses.kt`:

```kotlin
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
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `./gradlew :app:testDebugUnitTest --tests "com.watnapp.etipitaka.plus.account.AccountResponsesTest"`
Expected: PASS — 7 tests.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/watnapp/etipitaka/plus/account/AccountResponses.kt app/src/test/java/com/watnapp/etipitaka/plus/account/AccountResponsesTest.kt
git commit -m "feat: add login and upload response parsers"
```

---

### Task 5: user_data_list parser tests (TDD)

`parseUserDataList` was implemented in Task 4. This task adds its dedicated tests for the tricky double-encoded `items` string.

**Files:**
- Test: `app/src/test/java/com/watnapp/etipitaka/plus/account/UserDataListParserTest.kt`

- [ ] **Step 1: Write the failing tests**

Create `app/src/test/java/com/watnapp/etipitaka/plus/account/UserDataListParserTest.kt`:

```kotlin
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
```

- [ ] **Step 2: Run the tests**

Run: `./gradlew :app:testDebugUnitTest --tests "com.watnapp.etipitaka.plus.account.UserDataListParserTest"`
Expected: PASS — 4 tests (the implementation already exists from Task 4). If any fail, fix `parseUserDataList` in `AccountResponses.kt`.

- [ ] **Step 3: Commit**

```bash
git add app/src/test/java/com/watnapp/etipitaka/plus/account/UserDataListParserTest.kt
git commit -m "test: cover user_data_list double-encoded parsing"
```

---

### Task 6: SessionManager

**Files:**
- Create: `app/src/main/java/com/watnapp/etipitaka/plus/account/SessionManager.kt`

- [ ] **Step 1: Create SessionManager**

Create `app/src/main/java/com/watnapp/etipitaka/plus/account/SessionManager.kt`:

```kotlin
package com.watnapp.etipitaka.plus.account

import android.content.Context

/** Persists the logged-in user's username + auth token in SharedPreferences. */
class SessionManager(context: Context) {

    private val prefs =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val token: String?
        get() = prefs.getString(KEY_TOKEN, null)

    val username: String?
        get() = prefs.getString(KEY_USERNAME, null)

    val isLoggedIn: Boolean
        get() = token != null && username != null

    fun save(username: String, token: String) {
        prefs.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun clear() {
        prefs.edit()
            .remove(KEY_USERNAME)
            .remove(KEY_TOKEN)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "account_preferences"
        private const val KEY_TOKEN = "token"
        private const val KEY_USERNAME = "username"
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/watnapp/etipitaka/plus/account/SessionManager.kt
git commit -m "feat: add SessionManager for account token storage"
```

---

### Task 7: AccountApi OkHttp client

**Files:**
- Create: `app/src/main/java/com/watnapp/etipitaka/plus/account/AccountApi.kt`

- [ ] **Step 1: Create AccountApi**

Create `app/src/main/java/com/watnapp/etipitaka/plus/account/AccountApi.kt`:

```kotlin
package com.watnapp.etipitaka.plus.account

import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

/**
 * Talks to the E-Tipitaka backend. All methods are blocking and MUST be called
 * off the main thread (e.g. from Dispatchers.IO).
 */
class AccountApi(private val baseUrl: String) {

    private val client = OkHttpClient()

    /** POST /rest-auth/login/ — returns the auth token on success. */
    fun login(username: String, password: String): ApiResult<String> = try {
        val form = FormBody.Builder()
            .add("username", username)
            .add("password", password)
            .build()
        val request = Request.Builder()
            .url("$baseUrl/rest-auth/login/")
            .post(form)
            .build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            val token = if (response.isSuccessful) parseLoginResponse(body) else null
            if (token != null) ApiResult.Success(token)
            else ApiResult.ServerError("login failed")
        }
    } catch (e: IOException) {
        ApiResult.NetworkError
    }

    /** POST /rest-auth/logout/ — invalidates the token server-side. */
    fun logout(token: String): ApiResult<Unit> = try {
        val request = authedBuilder(token, "/rest-auth/logout/")
            .post(FormBody.Builder().build())
            .build()
        client.newCall(request).execute().use { response ->
            mapStatus(response) { Unit }
        }
    } catch (e: IOException) {
        ApiResult.NetworkError
    }

    /**
     * POST /upload/ — multipart upload of the export JSON.
     * [filename] MUST end in `.js` so the backend tags the platform `android`.
     */
    fun uploadBackup(
        token: String,
        filename: String,
        json: String,
    ): ApiResult<UploadOutcome> = try {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("title", filename)
            .addFormDataPart(
                "file",
                filename,
                json.toRequestBody("application/json".toMediaType()),
            )
            .build()
        val request = authedBuilder(token, "/upload/").post(body).build()
        client.newCall(request).execute().use { response ->
            mapStatus(response) { parseUploadResponse(it) }
        }
    } catch (e: IOException) {
        ApiResult.NetworkError
    }

    /** GET /user_data_list/ — the caller's own non-deleted backups. */
    fun listBackups(token: String): ApiResult<List<ServerBackup>> = try {
        val request = authedBuilder(token, "/user_data_list/").get().build()
        client.newCall(request).execute().use { response ->
            mapStatus(response) { parseUserDataList(it) }
        }
    } catch (e: IOException) {
        ApiResult.NetworkError
    }

    /** GET /user_data/{pk}/ — returns the backup file content as text. */
    fun downloadBackup(token: String, pk: Int): ApiResult<String> = try {
        val request = authedBuilder(token, "/user_data/$pk/").get().build()
        client.newCall(request).execute().use { response ->
            mapStatus(response) { it }
        }
    } catch (e: IOException) {
        ApiResult.NetworkError
    }

    /** DELETE /user_data/{pk}/ — soft-deletes the backup server-side. */
    fun deleteBackup(token: String, pk: Int): ApiResult<Unit> = try {
        val request = authedBuilder(token, "/user_data/$pk/").delete().build()
        client.newCall(request).execute().use { response ->
            mapStatus(response) { Unit }
        }
    } catch (e: IOException) {
        ApiResult.NetworkError
    }

    private fun authedBuilder(token: String, path: String): Request.Builder =
        Request.Builder()
            .url("$baseUrl$path")
            .header("Authorization", "Token $token")

    private fun <T> mapStatus(
        response: Response,
        onSuccess: (String) -> T,
    ): ApiResult<T> {
        val body = response.body?.string().orEmpty()
        return when {
            response.code == 401 -> ApiResult.AuthError
            response.isSuccessful -> ApiResult.Success(onSuccess(body))
            else -> ApiResult.ServerError("HTTP ${response.code}")
        }
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/watnapp/etipitaka/plus/account/AccountApi.kt
git commit -m "feat: add AccountApi OkHttp client"
```

---

### Task 8: UserDataExporter / UserDataImporter + Koin registration

**Files:**
- Create: `app/src/main/java/com/watnapp/etipitaka/plus/account/UserData.kt`
- Modify: `app/src/main/java/com/watnapp/etipitaka/plus/AppModule.kt`

- [ ] **Step 1: Create the exporter and importer**

Create `app/src/main/java/com/watnapp/etipitaka/plus/account/UserData.kt`:

```kotlin
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
```

- [ ] **Step 2: Register both as Koin singles**

In `app/src/main/java/com/watnapp/etipitaka/plus/AppModule.kt`, add these imports with the other imports:

```kotlin
import com.watnapp.etipitaka.plus.account.UserDataExporter
import com.watnapp.etipitaka.plus.account.UserDataImporter
```

Inside the `module { }` block, add after the `single { HistoryDaoHelper(androidContext()) }` line:

```kotlin
    single { UserDataExporter(get(), get()) }
    single { UserDataImporter(get(), get()) }
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/watnapp/etipitaka/plus/account/UserData.kt app/src/main/java/com/watnapp/etipitaka/plus/AppModule.kt
git commit -m "feat: extract reusable UserDataExporter and UserDataImporter"
```

---

### Task 9: Route MainActivity export/import through the new helpers

**Files:**
- Modify: `app/src/main/java/com/watnapp/etipitaka/plus/activity/MainActivity.java`

- [ ] **Step 1: Add the import for the new helper classes**

In `MainActivity.java`, with the other imports, add:

```java
import com.watnapp.etipitaka.plus.account.UserDataExporter;
import com.watnapp.etipitaka.plus.account.UserDataImporter;
```

- [ ] **Step 2: Replace the inline export-JSON build**

In `exportData(final Uri uri)` (around line 1079-1082), replace these three lines:

```java
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(FavoriteTable.TABLE_NAME, mFavoriteDaoHelper.dumpJSONArray());
                jsonObject.put(HistoryTable.TABLE_NAME, mHistoryDaoHelper.dumpJSONArray());
```

with:

```java
                String exportJson = get(UserDataExporter.class).buildExportJson();
```

Then, further down in the same method, change the write line:

```java
                    bw.write(jsonObject.toString());
```

to:

```java
                    bw.write(exportJson);
```

(The surrounding `try { ... } catch (JSONException | IOException e)` stays — `buildExportJson()` declares `throws JSONException`.)

- [ ] **Step 3: Replace the importAndroidData body**

Replace the entire `importAndroidData(String path)` method (lines 848-872) with:

```java
  private void importAndroidData(String path) {
    try {
      get(UserDataImporter.class).importAndroidJson(Utils.readTextFile(path));
      mHandler.post(new Runnable() {
        @Override
        public void run() {
          Toast.makeText(MainActivity.this, R.string.import_complete, Toast.LENGTH_SHORT).show();
        }
      });
    } catch (IOException | JSONException e) {
      e.printStackTrace();
      mHandler.post(new Runnable() {
        @Override
        public void run() {
          Toast.makeText(MainActivity.this, R.string.file_not_found, Toast.LENGTH_SHORT).show();
        }
      });
    }
  }
```

- [ ] **Step 4: Verify it compiles**

Run: `./gradlew :app:compileDebugJavaWithJavac`
Expected: `BUILD SUCCESSFUL`. If `JSONObject`/`FavoriteTable`/`HistoryTable` become unused imports, that is only a warning — leave them; they may still be used elsewhere in the file.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/watnapp/etipitaka/plus/activity/MainActivity.java
git commit -m "refactor: route SAF export/import through shared user-data helpers"
```

---

### Task 10: Account UI strings

**Files:**
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Add the account strings**

In `app/src/main/res/values/strings.xml`, add before the closing `</resources>` tag:

```xml
    <string name="account_menu">บัญชีผู้ใช้</string>
    <string name="account_title">บัญชีผู้ใช้</string>
    <string name="account_username">ชื่อผู้ใช้</string>
    <string name="account_password">รหัสผ่าน</string>
    <string name="account_login">เข้าสู่ระบบ</string>
    <string name="account_logout">ออกจากระบบ</string>
    <string name="account_create">สร้างบัญชีใหม่</string>
    <string name="account_upload">อัปโหลดข้อมูลปัจจุบัน</string>
    <string name="account_download">ดาวน์โหลด</string>
    <string name="account_delete">ลบ</string>
    <string name="account_refresh">รีเฟรช</string>
    <string name="account_logged_in_as">เข้าสู่ระบบในชื่อ %1$s</string>
    <string name="account_no_backups">ยังไม่มีข้อมูลสำรองบนเซิร์ฟเวอร์</string>
    <string name="account_login_failed">เข้าสู่ระบบไม่สำเร็จ</string>
    <string name="account_network_error">เชื่อมต่อเซิร์ฟเวอร์ไม่สำเร็จ</string>
    <string name="account_session_expired">เซสชันหมดอายุ กรุณาเข้าสู่ระบบใหม่</string>
    <string name="account_upload_success">อัปโหลดข้อมูลสำเร็จ</string>
    <string name="account_upload_failed">อัปโหลดข้อมูลไม่สำเร็จ</string>
    <string name="account_file_exists">มีข้อมูลสำรองของวันนี้อยู่แล้ว ลบรายการเดิมก่อนหากต้องการอัปโหลดใหม่</string>
    <string name="account_download_success">นำข้อมูลเข้าสำเร็จ</string>
    <string name="account_download_failed">ดาวน์โหลดข้อมูลไม่สำเร็จ</string>
    <string name="account_delete_failed">ลบข้อมูลไม่สำเร็จ</string>
    <string name="account_platform_ios">iOS</string>
    <string name="account_platform_android">Android</string>
    <string name="account_platform_pc">PC</string>
```

- [ ] **Step 2: Verify resources compile**

Run: `./gradlew :app:processDebugResources`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/values/strings.xml
git commit -m "feat: add account UI strings"
```

---

### Task 11: AccountViewModel + Koin registration

**Files:**
- Create: `app/src/main/java/com/watnapp/etipitaka/plus/account/AccountViewModel.kt`
- Modify: `app/src/main/java/com/watnapp/etipitaka/plus/AppModule.kt`

- [ ] **Step 1: Create the UI state and ViewModel**

Create `app/src/main/java/com/watnapp/etipitaka/plus/account/AccountViewModel.kt`:

```kotlin
package com.watnapp.etipitaka.plus.account

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watnapp.etipitaka.plus.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** UI state for the Account screen. */
sealed interface AccountUiState {
    data class LoggedOut(
        val loggingIn: Boolean = false,
        val errorRes: Int? = null,
    ) : AccountUiState

    data class LoggedIn(
        val username: String,
        val backups: List<ServerBackup> = emptyList(),
        val busy: Boolean = false,
        val messageRes: Int? = null,
    ) : AccountUiState
}

class AccountViewModel(
    private val api: AccountApi,
    private val session: SessionManager,
    private val exporter: UserDataExporter,
    private val importer: UserDataImporter,
) : ViewModel() {

    var uiState by mutableStateOf<AccountUiState>(initialState())
        private set

    private fun initialState(): AccountUiState {
        val username = session.username
        return if (session.isLoggedIn && username != null) {
            AccountUiState.LoggedIn(username)
        } else {
            AccountUiState.LoggedOut()
        }
    }

    init {
        if (session.isLoggedIn) refreshBackups()
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) return
        uiState = AccountUiState.LoggedOut(loggingIn = true)
        viewModelScope.launch {
            when (val result = withContext(Dispatchers.IO) { api.login(username, password) }) {
                is ApiResult.Success -> {
                    session.save(username, result.value)
                    uiState = AccountUiState.LoggedIn(username)
                    refreshBackups()
                }
                ApiResult.NetworkError ->
                    uiState = AccountUiState.LoggedOut(errorRes = R.string.account_network_error)
                else ->
                    uiState = AccountUiState.LoggedOut(errorRes = R.string.account_login_failed)
            }
        }
    }

    fun logout() {
        val token = session.token ?: return forceLoggedOut()
        viewModelScope.launch {
            withContext(Dispatchers.IO) { api.logout(token) }
            forceLoggedOut()
        }
    }

    fun refreshBackups() {
        val token = session.token ?: return forceLoggedOut()
        val current = uiState as? AccountUiState.LoggedIn ?: return
        uiState = current.copy(busy = true, messageRes = null)
        viewModelScope.launch {
            when (val result = withContext(Dispatchers.IO) { api.listBackups(token) }) {
                is ApiResult.Success ->
                    uiState = current.copy(backups = result.value, busy = false)
                ApiResult.AuthError -> forceLoggedOut(R.string.account_session_expired)
                else -> uiState = current.copy(busy = false, messageRes = R.string.account_network_error)
            }
        }
    }

    fun upload() {
        val token = session.token ?: return forceLoggedOut()
        val current = uiState as? AccountUiState.LoggedIn ?: return
        uiState = current.copy(busy = true, messageRes = null)
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                val json = exporter.buildExportJson()
                api.uploadBackup(token, uploadFilename(), json)
            }
            when (result) {
                is ApiResult.Success -> when (result.value) {
                    UploadOutcome.SUCCESS -> {
                        setMessage(R.string.account_upload_success)
                        refreshBackups()
                    }
                    UploadOutcome.FILE_EXISTS -> setMessage(R.string.account_file_exists)
                    UploadOutcome.FAILED -> setMessage(R.string.account_upload_failed)
                }
                ApiResult.AuthError -> forceLoggedOut(R.string.account_session_expired)
                else -> setMessage(R.string.account_upload_failed)
            }
        }
    }

    fun download(backup: ServerBackup) {
        val token = session.token ?: return forceLoggedOut()
        val current = uiState as? AccountUiState.LoggedIn ?: return
        uiState = current.copy(busy = true, messageRes = null)
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                when (val dl = api.downloadBackup(token, backup.pk)) {
                    is ApiResult.Success -> {
                        runCatching { importer.importAndroidJson(dl.value) }
                            .fold({ ApiResult.Success(Unit) },
                                  { ApiResult.ServerError("import failed") })
                    }
                    ApiResult.AuthError -> ApiResult.AuthError
                    ApiResult.NetworkError -> ApiResult.NetworkError
                    is ApiResult.ServerError -> dl
                }
            }
            when (result) {
                is ApiResult.Success -> setMessage(R.string.account_download_success)
                ApiResult.AuthError -> forceLoggedOut(R.string.account_session_expired)
                else -> setMessage(R.string.account_download_failed)
            }
        }
    }

    fun delete(backup: ServerBackup) {
        val token = session.token ?: return forceLoggedOut()
        val current = uiState as? AccountUiState.LoggedIn ?: return
        uiState = current.copy(busy = true, messageRes = null)
        viewModelScope.launch {
            when (val result = withContext(Dispatchers.IO) { api.deleteBackup(token, backup.pk) }) {
                is ApiResult.Success -> refreshBackups()
                ApiResult.AuthError -> forceLoggedOut(R.string.account_session_expired)
                else -> setMessage(R.string.account_delete_failed)
            }
        }
    }

    /** Clears a transient message after the UI has shown it. */
    fun messageShown() {
        val current = uiState as? AccountUiState.LoggedIn ?: return
        uiState = current.copy(messageRes = null)
    }

    private fun setMessage(res: Int) {
        val current = uiState as? AccountUiState.LoggedIn ?: return
        uiState = current.copy(busy = false, messageRes = res)
    }

    private fun forceLoggedOut(errorRes: Int? = null) {
        session.clear()
        uiState = AccountUiState.LoggedOut(errorRes = errorRes)
    }

    private fun uploadFilename(): String {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return "edata-$date.js"
    }
}
```

- [ ] **Step 2: Register the ViewModel in Koin**

In `AppModule.kt`, add these imports:

```kotlin
import com.watnapp.etipitaka.plus.account.AccountApi
import com.watnapp.etipitaka.plus.account.AccountViewModel
import com.watnapp.etipitaka.plus.account.SessionManager
import com.watnapp.etipitaka.plus.Constants
```

Inside the `module { }` block, add after the `UserDataImporter` single from Task 8:

```kotlin
    single { SessionManager(androidContext()) }
    single { AccountApi(Constants.DATA_SERVER_URL) }
    viewModel { AccountViewModel(get(), get(), get(), get()) }
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/watnapp/etipitaka/plus/account/AccountViewModel.kt app/src/main/java/com/watnapp/etipitaka/plus/AppModule.kt
git commit -m "feat: add AccountViewModel and Koin wiring"
```

---

### Task 12: AccountScreen Compose UI

**Files:**
- Create: `app/src/main/java/com/watnapp/etipitaka/plus/account/AccountScreen.kt`

- [ ] **Step 1: Create the Compose screen**

Create `app/src/main/java/com/watnapp/etipitaka/plus/account/AccountScreen.kt`:

```kotlin
package com.watnapp.etipitaka.plus.account

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.watnapp.etipitaka.plus.Constants
import com.watnapp.etipitaka.plus.R

@Composable
fun AccountScreen(viewModel: AccountViewModel) {
    when (val state = viewModel.uiState) {
        is AccountUiState.LoggedOut -> LoggedOutContent(state, viewModel::login)
        is AccountUiState.LoggedIn -> LoggedInContent(
            state = state,
            onUpload = viewModel::upload,
            onRefresh = viewModel::refreshBackups,
            onLogout = viewModel::logout,
            onDownload = viewModel::download,
            onDelete = viewModel::delete,
            onMessageShown = viewModel::messageShown,
        )
    }
}

@Composable
private fun LoggedOutContent(
    state: AccountUiState.LoggedOut,
    onLogin: (String, String) -> Unit,
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(stringResource(R.string.account_username)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.account_password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )
        if (state.errorRes != null) {
            Text(stringResource(state.errorRes))
        }
        Button(
            onClick = { onLogin(username, password) },
            enabled = !state.loggingIn,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.account_login))
        }
        if (state.loggingIn) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        TextButton(
            onClick = {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(Constants.SIGNUP_URL))
                )
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(stringResource(R.string.account_create))
        }
    }
}

@Composable
private fun LoggedInContent(
    state: AccountUiState.LoggedIn,
    onUpload: () -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onDownload: (ServerBackup) -> Unit,
    onDelete: (ServerBackup) -> Unit,
    onMessageShown: () -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(state.messageRes) {
        val res = state.messageRes
        if (res != null) {
            Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
            onMessageShown()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.account_logged_in_as, state.username))
            TextButton(onClick = onLogout) {
                Text(stringResource(R.string.account_logout))
            }
        }
        Button(
            onClick = onUpload,
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.account_upload))
        }
        OutlinedButton(
            onClick = onRefresh,
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.account_refresh))
        }
        if (state.busy) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        Divider()
        if (state.backups.isEmpty()) {
            Text(stringResource(R.string.account_no_backups))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.backups, key = { it.pk }) { backup ->
                    BackupRow(
                        backup = backup,
                        enabled = !state.busy,
                        onDownload = { onDownload(backup) },
                        onDelete = { onDelete(backup) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BackupRow(
    backup: ServerBackup,
    enabled: Boolean,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(platformLabel(backup.platform) + "  •  " + backup.filename)
        Text(backup.createdAt)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onDownload, enabled = enabled) {
                Text(stringResource(R.string.account_download))
            }
            OutlinedButton(onClick = onDelete, enabled = enabled) {
                Text(stringResource(R.string.account_delete))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Divider()
    }
}

@Composable
private fun platformLabel(platform: String): String = when (platform) {
    "ios" -> stringResource(R.string.account_platform_ios)
    "android" -> stringResource(R.string.account_platform_android)
    "pc" -> stringResource(R.string.account_platform_pc)
    else -> platform
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`. If `Divider` is flagged deprecated, that is a warning only — leave it.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/watnapp/etipitaka/plus/account/AccountScreen.kt
git commit -m "feat: add AccountScreen Compose UI"
```

---

### Task 13: AccountActivity + manifest registration

**Files:**
- Create: `app/src/main/java/com/watnapp/etipitaka/plus/activity/AccountActivity.kt`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Create the activity**

Create `app/src/main/java/com/watnapp/etipitaka/plus/activity/AccountActivity.kt`:

```kotlin
package com.watnapp.etipitaka.plus.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.watnapp.etipitaka.plus.account.AccountScreen
import com.watnapp.etipitaka.plus.account.AccountViewModel
import com.watnapp.etipitaka.plus.ui.compose.ETipitakaTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class AccountActivity : AppCompatActivity() {

    private val viewModel: AccountViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ETipitakaTheme {
                AccountScreen(viewModel)
            }
        }
    }
}
```

- [ ] **Step 2: Register the activity in the manifest**

In `app/src/main/AndroidManifest.xml`, add inside the `<application>` element, next to the other `<activity>` entries:

```xml
        <activity
            android:name=".activity.AccountActivity"
            android:theme="@style/Theme.AppCompat.Light.NoActionBar"
            android:label="@string/account_title"
            android:exported="false" />
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :app:compileDebugKotlin`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/watnapp/etipitaka/plus/activity/AccountActivity.kt app/src/main/AndroidManifest.xml
git commit -m "feat: add AccountActivity hosting the account screen"
```

---

### Task 14: Wire the Account overflow menu item

**Files:**
- Modify: `app/src/main/java/com/watnapp/etipitaka/plus/activity/MainActivity.java`

- [ ] **Step 1: Add the menu item**

In `MainActivity.java` `onCreateOptionsMenu(Menu menu)`, add the Account item immediately after the Version line (`menu.add(Menu.NONE, Constants.MENU_ITEM_VERSION, ...)`):

```java
    menu.add(Menu.NONE, Constants.MENU_ITEM_ACCOUNT, Menu.NONE, R.string.account_menu);
```

- [ ] **Step 2: Handle the menu selection**

In `onOptionsItemSelected(MenuItem item)`, add a case alongside the `MENU_ITEM_VERSION` case:

```java
      case Constants.MENU_ITEM_ACCOUNT:
        showAccount();
        return true;
```

- [ ] **Step 3: Add the launcher method**

Add this method near `showVersionDialog()`:

```java
  private void showAccount() {
    startActivity(new android.content.Intent(this,
        com.watnapp.etipitaka.plus.activity.AccountActivity.class));
  }
```

- [ ] **Step 4: Verify it compiles**

Run: `./gradlew :app:compileDebugJavaWithJavac`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/watnapp/etipitaka/plus/activity/MainActivity.java
git commit -m "feat: add Account overflow menu entry"
```

---

### Task 15: Full build, unit tests, and manual QA

**Files:** none (verification only)

- [ ] **Step 1: Run the full unit-test suite**

Run: `./gradlew :app:testDebugUnitTest`
Expected: `BUILD SUCCESSFUL`; the 11 account-parser tests pass.

- [ ] **Step 2: Build the debug APK**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Install on a device/emulator and manually verify**

Install: `./gradlew :app:installDebug`

Verify each item against a backend reachable at `https://data.etipitaka.com` (or change `Constants.DATA_SERVER_URL` to a dev server). Use an account that already exists on the backend (registration is web-only).

- [ ] Overflow menu shows "บัญชีผู้ใช้"; tapping it opens the Account screen.
- [ ] Logged-out screen: wrong password → "เข้าสู่ระบบไม่สำเร็จ"; airplane mode → "เชื่อมต่อเซิร์ฟเวอร์ไม่สำเร็จ".
- [ ] "สร้างบัญชีใหม่" opens `https://data.etipitaka.com/signup/` in a browser.
- [ ] Correct credentials → logged-in screen showing the username and the backup list.
- [ ] "อัปโหลดข้อมูลปัจจุบัน" → "อัปโหลดข้อมูลสำเร็จ"; the new backup appears, grouped under Android.
- [ ] Uploading again the same day → "มีข้อมูลสำรองของวันนี้อยู่แล้ว …".
- [ ] On the backend admin, the uploaded row has `platform = android` (filename ended `.js`).
- [ ] Download a backup → "นำข้อมูลเข้าสำเร็จ"; favorites/history merged with no duplicates (download twice → still no duplicates).
- [ ] Delete a backup → it disappears from the list.
- [ ] Logout → returns to the logged-out screen; reopening the Account screen stays logged out.
- [ ] Log in again, close and reopen the app, reopen the Account screen → still logged in (token persisted by `SessionManager`).
- [ ] The existing SAF Export and Import (Preferences → Manage data) still work after the Task 9 refactor.

- [ ] **Step 4: Commit any fixes**

If manual QA surfaces bugs, fix them and commit with descriptive messages. If everything passes, there is nothing to commit for this task.

---

## Self-Review Notes

- **Spec coverage:** login/logout (Tasks 7, 11, 12), upload (8, 11, 12), list (4-5, 7, 11, 12), download + merge restore (8, 11, 12), delete (7, 11, 12), `.js` extension enforced in `AccountViewModel.uploadFilename()` (11), web-only registration link (12), manual triggers (12), token storage (6), overflow-menu entry (2, 14), `buildExportJson`/`importAndroidJson` refactor (8, 9), error handling incl. 401/file_exists/network (7, 11). New OkHttp dependency (1).
- **Deviation from spec:** the spec listed a `SessionManager` unit test. `SessionManager` only wraps `SharedPreferences` and would need Robolectric (not a dependency) to unit-test on the JVM. It is instead covered by manual QA (Task 15, Step 3 — token persistence across app restart). Parser logic — the real complexity — is unit-tested (Tasks 4-5).
