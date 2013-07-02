package com.watnapp.etipitaka.model;
import android.provider.BaseColumns;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class Favorite extends ModelBase {
    private Context context;
    private int id;
    private String note;
    private int language;
    private int volume;
    private int page;


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

  @Override
    public void fromCursor(Cursor cursor, Context context) {
        this.id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
        this.note = cursor.getString(cursor.getColumnIndex(FavoriteTable.FavoriteColumns.NOTE));
        this.language = cursor.getInt(cursor.getColumnIndex(FavoriteTable.FavoriteColumns.LANGUAGE));
        this.volume = cursor.getInt(cursor.getColumnIndex(FavoriteTable.FavoriteColumns.VOLUME));
        this.page = cursor.getInt(cursor.getColumnIndex(FavoriteTable.FavoriteColumns.PAGE));
        this.context = context;
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(FavoriteTable.FavoriteColumns.NOTE, this.note);
        values.put(FavoriteTable.FavoriteColumns.LANGUAGE, this.language);
        values.put(FavoriteTable.FavoriteColumns.VOLUME, this.volume);
        values.put(FavoriteTable.FavoriteColumns.PAGE, this.page);
        return values;
    }

    public static Favorite newInstance(Cursor cursor, Context context) {
        Favorite favorite = new Favorite();
        favorite.fromCursor(cursor, context);
        return favorite;
    }


}