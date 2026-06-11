package com.watnapp.etipitaka

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.watnapp.etipitaka.plus.Utils
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper
import com.watnapp.etipitaka.plus.helper.getLocalDatabaseVersion
import com.watnapp.etipitaka.plus.model.ETThaiWatnaDataModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assume.assumeFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.RandomAccessFile

/**
 * Regression tests for two Play Console crash classes around unhealthy
 * database files (the DB lives on external storage, so files can vanish or
 * rot at any time):
 *
 * 1. SQLiteCantOpenDatabaseException in getLocalDatabaseVersion — the caller
 *    checks File.exists() on the main thread but the open happens later on a
 *    GlobalScope coroutine, where any exception kills the process.
 *
 * 2. SQLiteDatabaseCorruptException in ET*DataModel.read — openDatabase only
 *    validates the 100-byte SQLite header, so a file with an intact header
 *    but corrupt btree pages opens fine and then blows up on the first
 *    cursor-window fill (moveToFirst).
 *
 * Uses THAIWN (thaiwn.db) and skips if a real database is already installed
 * on the test device, so it never clobbers user data.
 */
@RunWith(AndroidJUnit4::class)
class DatabaseFailureInstrumentedTest {

  private val context: Context
    get() = InstrumentationRegistry.getInstrumentation().targetContext

  private lateinit var dbFile: File
  private var ownsDbFile = false

  @Before
  fun setUp() {
    dbFile = File(Utils.getDatabasePath(context, BookDatabaseHelper.Language.THAIWN))
    // If a real database is installed, the assume skips the test — and @After
    // still runs after a violated assumption, so only delete what we created.
    assumeFalse("real thaiwn.db installed on device — skipping", dbFile.exists())
    ownsDbFile = true
  }

  @After
  fun tearDown() {
    if (ownsDbFile) {
      dbFile.delete()
      File(dbFile.path + "-journal").delete()
    }
  }

  // ── getLocalDatabaseVersion (DownloadDatabase.kt) ──────────────────────

  @Test
  fun localDatabaseVersion_missingFile_returnsZero() {
    val version = runBlocking {
      getLocalDatabaseVersion(context, BookDatabaseHelper.Language.THAIWN)
    }
    assertEquals(0, version)
  }

  @Test
  fun localDatabaseVersion_garbageFile_returnsZero() {
    dbFile.writeBytes("this is not a sqlite database".toByteArray())
    val version = runBlocking {
      getLocalDatabaseVersion(context, BookDatabaseHelper.Language.THAIWN)
    }
    assertEquals(0, version)
  }

  // ── ET*DataModel.read with unhealthy DB files ──────────────────────────

  @Test
  fun read_garbageFile_returnsEmptyCursor() {
    dbFile.writeBytes("this is not a sqlite database".toByteArray())
    val model = ETThaiWatnaDataModel(context)
    val cursor = model.read(1)
    assertNotNull(cursor)
    assertEquals(0, cursor.count)
    cursor.close()
    model.closeDatabase()
  }

  @Test
  fun read_corruptBody_returnsEmptyCursor() {
    val pageSize = createDatabaseWithIntactHeaderAndRows()
    // Overwrite everything past page 1 (sqlite_master root). The header
    // still validates so openDatabase succeeds, but the main table's btree
    // pages are garbage — exactly the Play Console crash shape.
    RandomAccessFile(dbFile, "rw").use { raf ->
      raf.seek(pageSize)
      raf.write(ByteArray((raf.length() - pageSize).toInt()) { 0x55 })
    }

    val model = ETThaiWatnaDataModel(context)
    val cursor = model.read(1)
    assertNotNull(cursor)
    assertEquals(0, cursor.count)
    cursor.close()
    model.closeDatabase()
  }

  /** Builds a multi-page thaiwn.db with the main table schema; returns the page size. */
  private fun createDatabaseWithIntactHeaderAndRows(): Long {
    val db = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
    val pageSize = db.rawQuery("pragma page_size", null).use { c ->
      c.moveToFirst()
      c.getLong(0)
    }
    db.execSQL(
      "CREATE TABLE main (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "volume TEXT, page TEXT, items TEXT, content TEXT)"
    )
    db.beginTransaction()
    try {
      val filler = "x".repeat(500)
      for (i in 1..200) {
        db.execSQL(
          "INSERT INTO main (volume, page, items, content) VALUES (?, ?, ?, ?)",
          arrayOf("1", "$i", "1", filler)
        )
      }
      db.setTransactionSuccessful()
    } finally {
      db.endTransaction()
    }
    db.close()
    return pageSize
  }
}
