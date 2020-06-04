package com.watnapp.etipitaka.plus.model;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;


public abstract class DaoHelper {

  protected Context mContext;

  @SuppressWarnings("rawtypes")
  protected abstract Dao getDao();

  public DaoHelper(Context context) {
    mContext = context;
  }

  public int getCount() {
    return getDao().size();
  }

  public ModelBase get(int position) {
    return getDao().get(position);
  }

  @SuppressWarnings("unchecked")
  public int insert(ModelBase object) {
    Uri uri = getDao().insert(object);
    try {
      if (uri.getLastPathSegment() != null) {
        object.setId(Integer.valueOf(uri.getLastPathSegment()));
        return Integer.valueOf(uri.getLastPathSegment());
      }
    } catch (NumberFormatException e) {
    }
    return -1;
  }

  public void delete(String selection, String[] selectionArgs) {
    getDao().delete(selection, selectionArgs);
  }

  @SuppressWarnings("unchecked")
  public void delete(ModelBase object) {
    getDao().delete(object);
  }

  public void update(ContentValues values, String selection, String[] selectionArgs) {
    getDao().update(values, selection, selectionArgs);
  }

  @SuppressWarnings("unchecked")
  public void update(ModelBase object) {
    getDao().update(object);
  }

  public ModelBase getById(int id) {
    return getDao().getById(id);
  }

  @SuppressWarnings("unchecked")
  public List<ModelBase> get(String selection, String[] selectionArgs) {
    return getDao().get(selection, selectionArgs);
  }
}
