package com.watnapp.etipitaka.plus.model;
import android.provider.BaseColumns;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class HistoryItem extends ModelBase {
  private Context context;
  private int id;
  private int historyId;
  private int volume;
  private int page;
  private int status;

  public HistoryItem() {
    super();
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public void setId(int id) {
    this.id = id;
  }

  public int getHistoryId() {
    return historyId;
  }

  public void setHistoryId(int historyId) {
    this.historyId = historyId;
  }

  public int getVolume() {
    return volume;
  }

  public void setVolume(int volume) {
    this.volume = volume;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public Status getStatus() {
    return Status.values()[status];
  }

  public void setStatus(Status status) {
    this.status = status.ordinal();
  }

  @Override
  public void fromCursor(Cursor cursor, Context context) {
    this.id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
    this.historyId = cursor.getInt(cursor.getColumnIndex(HistoryItemTable.HistoryItemColumns.HISTORY_ID));
    this.volume = cursor.getInt(cursor.getColumnIndex(HistoryItemTable.HistoryItemColumns.VOLUME));
    this.page = cursor.getInt(cursor.getColumnIndex(HistoryItemTable.HistoryItemColumns.PAGE));
    this.status = cursor.getInt(cursor.getColumnIndex(HistoryItemTable.HistoryItemColumns.STATUS));
    this.context = context;
  }

  @Override
  public ContentValues toContentValues() {
    ContentValues values = new ContentValues();
    values.put(HistoryItemTable.HistoryItemColumns.HISTORY_ID, this.historyId);
    values.put(HistoryItemTable.HistoryItemColumns.VOLUME, this.volume);
    values.put(HistoryItemTable.HistoryItemColumns.PAGE, this.page);
    values.put(HistoryItemTable.HistoryItemColumns.STATUS, this.status);
    return values;
  }

  public static HistoryItem newInstance(Cursor cursor, Context context) {
    HistoryItem historyItem = new HistoryItem();
    historyItem.fromCursor(cursor, context);
    return historyItem;
  }

  public enum Status {
    NONE(0), READ(1), SKIMMED(2);

    private int code;

    private Status(int code) {
      this.code = code;
    }

    public int getCode() {
      return code;
    }
  }
}