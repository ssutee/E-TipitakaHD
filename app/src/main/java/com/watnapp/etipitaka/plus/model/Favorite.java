package com.watnapp.etipitaka.plus.model;
import android.provider.BaseColumns;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;

public class Favorite extends ModelBase {
  private int id;
  private String note;
  private int language;
  private int volume;
  private int page;
  private int item;
  private int score;


  public Favorite() {
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

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public BookDatabaseHelper.Language getLanguage() {
    return BookDatabaseHelper.Language.values()[language];
  }

  public void setLanguage(BookDatabaseHelper.Language language) {
    this.language = language.ordinal();
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

  public int getItem() {
    return item;
  }

  public void setItem(int item) {
    this.item = item;
  }

  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

  @Override
  public void fromCursor(Cursor cursor, Context context) {
    this.id = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID));
    this.note = cursor.getString(cursor.getColumnIndexOrThrow(FavoriteTable.FavoriteColumns.NOTE));
    this.language = cursor.getInt(cursor.getColumnIndexOrThrow(FavoriteTable.FavoriteColumns.LANGUAGE));
    this.volume = cursor.getInt(cursor.getColumnIndexOrThrow(FavoriteTable.FavoriteColumns.VOLUME));
    this.page = cursor.getInt(cursor.getColumnIndexOrThrow(FavoriteTable.FavoriteColumns.PAGE));
    this.item = cursor.getInt(cursor.getColumnIndexOrThrow(FavoriteTable.FavoriteColumns.ITEM));
    this.score = cursor.getInt(cursor.getColumnIndexOrThrow(FavoriteTable.FavoriteColumns.SCORE));
  }

  @Override
  public ContentValues toContentValues() {
    ContentValues values = new ContentValues();
    values.put(FavoriteTable.FavoriteColumns.NOTE, this.note);
    values.put(FavoriteTable.FavoriteColumns.LANGUAGE, this.language);
    values.put(FavoriteTable.FavoriteColumns.VOLUME, this.volume);
    values.put(FavoriteTable.FavoriteColumns.PAGE, this.page);
    values.put(FavoriteTable.FavoriteColumns.ITEM, this.item);
    values.put(FavoriteTable.FavoriteColumns.SCORE, this.score);
    return values;
  }

  public static Favorite newInstance(Cursor cursor, Context context) {
    Favorite favorite = new Favorite();
    favorite.fromCursor(cursor, context);
    return favorite;
  }

}
