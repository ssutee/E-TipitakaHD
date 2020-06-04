package com.watnapp.etipitaka.plus.model;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.provider.BaseColumns;

public class DatabaseProvider extends ContentProvider {
    private DatabaseOpenHelper dbHelper;
    private SQLiteDatabase database;
    public static final String AUTHORITY = "com.watnapp.etipitaka.plus.model.contentprovider";
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int HISTORYS = 1001;
    private static final int HISTORY_ID = 1002;
    public static final String HISTORY_PATH = "historys";
    public static final Uri HISTORY_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + HISTORY_PATH);
    public static final String HISTORY_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/historys";
    public static final String HISTORY_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/history";
    static {
        sURIMatcher.addURI(AUTHORITY, HISTORY_PATH, HISTORYS);
        sURIMatcher.addURI(AUTHORITY, HISTORY_PATH + "/#", HISTORY_ID);
    }

    private static final int FAVORITES = 1003;
    private static final int FAVORITE_ID = 1004;
    public static final String FAVORITE_PATH = "favorites";
    public static final Uri FAVORITE_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + FAVORITE_PATH);
    public static final String FAVORITE_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/favorites";
    public static final String FAVORITE_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/favorite";
    static {
        sURIMatcher.addURI(AUTHORITY, FAVORITE_PATH, FAVORITES);
        sURIMatcher.addURI(AUTHORITY, FAVORITE_PATH + "/#", FAVORITE_ID);
    }

    private static final int HISTORY_ITEMS = 1005;
    private static final int HISTORY_ITEM_ID = 1006;
    public static final String HISTORY_ITEM_PATH = "history_items";
    public static final Uri HISTORY_ITEM_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + HISTORY_ITEM_PATH);
    public static final String HISTORY_ITEM_CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/history_items";
    public static final String HISTORY_ITEM_CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/history_item";
    static {
        sURIMatcher.addURI(AUTHORITY, HISTORY_ITEM_PATH, HISTORY_ITEMS);
        sURIMatcher.addURI(AUTHORITY, HISTORY_ITEM_PATH + "/#", HISTORY_ITEM_ID);
    }



    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseOpenHelper(getContext());
        database = dbHelper.getWritableDatabase();
        return true;
    }

    @Override
    public String getType(Uri uri) {
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
        case HISTORYS:
            return HISTORY_CONTENT_TYPE;
        case HISTORY_ID:
            return HISTORY_CONTENT_ITEM_TYPE;
        case FAVORITES:
            return FAVORITE_CONTENT_TYPE;
        case FAVORITE_ID:
            return FAVORITE_CONTENT_ITEM_TYPE;
        case HISTORY_ITEMS:
            return HISTORY_ITEM_CONTENT_TYPE;
        case HISTORY_ITEM_ID:
            return HISTORY_ITEM_CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        long id = 0;
        switch (uriType) {
        case HISTORYS:
            id = database.insert(HistoryTable.TABLE_NAME, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return Uri.parse("content://" + AUTHORITY + "/" + HISTORY_PATH + "/" + id);
        case FAVORITES:
            id = database.insert(FavoriteTable.TABLE_NAME, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return Uri.parse("content://" + AUTHORITY + "/" + FAVORITE_PATH + "/" + id);
        case HISTORY_ITEMS:
            id = database.insert(HistoryItemTable.TABLE_NAME, null, values);
            getContext().getContentResolver().notifyChange(uri, null);
            return Uri.parse("content://" + AUTHORITY + "/" + HISTORY_ITEM_PATH + "/" + id);
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int uriType = sURIMatcher.match(uri);
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        switch (uriType) {
        case HISTORYS:
            queryBuilder.setTables(HistoryTable.TABLE_NAME);
            break;
        case HISTORY_ID:
            queryBuilder.setTables(HistoryTable.TABLE_NAME);
            queryBuilder.appendWhere(BaseColumns._ID + "=" + uri.getLastPathSegment());
            break;
        case FAVORITES:
            queryBuilder.setTables(FavoriteTable.TABLE_NAME);
            break;
        case FAVORITE_ID:
            queryBuilder.setTables(FavoriteTable.TABLE_NAME);
            queryBuilder.appendWhere(BaseColumns._ID + "=" + uri.getLastPathSegment());
            break;
        case HISTORY_ITEMS:
            queryBuilder.setTables(HistoryItemTable.TABLE_NAME);
            break;
        case HISTORY_ITEM_ID:
            queryBuilder.setTables(HistoryItemTable.TABLE_NAME);
            queryBuilder.appendWhere(BaseColumns._ID + "=" + uri.getLastPathSegment());
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);        
        }
        Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        int rowsDeleted = 0;
        switch (uriType) {
        case HISTORYS:
            rowsDeleted = database.delete(HistoryTable.TABLE_NAME, selection, selectionArgs);
            break;
        case HISTORY_ID:
            String historyId = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection)) {
                rowsDeleted = database.delete(HistoryTable.TABLE_NAME, BaseColumns._ID + "=" + historyId, null);
            } else {
                rowsDeleted = database.delete(HistoryTable.TABLE_NAME, BaseColumns._ID + "=" + historyId + " AND " + selection, selectionArgs);
            }
            break;
        case FAVORITES:
            rowsDeleted = database.delete(FavoriteTable.TABLE_NAME, selection, selectionArgs);
            break;
        case FAVORITE_ID:
            String favoriteId = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection)) {
                rowsDeleted = database.delete(FavoriteTable.TABLE_NAME, BaseColumns._ID + "=" + favoriteId, null);
            } else {
                rowsDeleted = database.delete(FavoriteTable.TABLE_NAME, BaseColumns._ID + "=" + favoriteId + " AND " + selection, selectionArgs);
            }
            break;
        case HISTORY_ITEMS:
            rowsDeleted = database.delete(HistoryItemTable.TABLE_NAME, selection, selectionArgs);
            break;
        case HISTORY_ITEM_ID:
            String historyItemId = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection)) {
                rowsDeleted = database.delete(HistoryItemTable.TABLE_NAME, BaseColumns._ID + "=" + historyItemId, null);
            } else {
                rowsDeleted = database.delete(HistoryItemTable.TABLE_NAME, BaseColumns._ID + "=" + historyItemId + " AND " + selection, selectionArgs);
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);      
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        int rowsUpdated = 0;
        switch (uriType) {
        case HISTORYS:
            rowsUpdated = database.update(HistoryTable.TABLE_NAME, values, selection, selectionArgs);
            break;
        case HISTORY_ID:
            String historyId = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection)) {
                rowsUpdated = database.update(HistoryTable.TABLE_NAME, values, BaseColumns._ID + "=" + historyId, null);
            } else {
                rowsUpdated = database.update(HistoryTable.TABLE_NAME, values, BaseColumns._ID + "=" + historyId + " AND " + selection, selectionArgs);
            }
            break;
        case FAVORITES:
            rowsUpdated = database.update(FavoriteTable.TABLE_NAME, values, selection, selectionArgs);
            break;
        case FAVORITE_ID:
            String favoriteId = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection)) {
                rowsUpdated = database.update(FavoriteTable.TABLE_NAME, values, BaseColumns._ID + "=" + favoriteId, null);
            } else {
                rowsUpdated = database.update(FavoriteTable.TABLE_NAME, values, BaseColumns._ID + "=" + favoriteId + " AND " + selection, selectionArgs);
            }
            break;
        case HISTORY_ITEMS:
            rowsUpdated = database.update(HistoryItemTable.TABLE_NAME, values, selection, selectionArgs);
            break;
        case HISTORY_ITEM_ID:
            String historyItemId = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection)) {
                rowsUpdated = database.update(HistoryItemTable.TABLE_NAME, values, BaseColumns._ID + "=" + historyItemId, null);
            } else {
                rowsUpdated = database.update(HistoryItemTable.TABLE_NAME, values, BaseColumns._ID + "=" + historyItemId + " AND " + selection, selectionArgs);
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }


}