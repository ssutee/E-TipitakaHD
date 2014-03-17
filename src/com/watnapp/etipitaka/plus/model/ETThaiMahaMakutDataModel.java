package com.watnapp.etipitaka.plus.model;

import android.content.Context;
import android.database.Cursor;
import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by sutee on 13/2/14.
 */
public class ETThaiMahaMakutDataModel extends ETThaiMahaDataModel {

  protected static final String TAG = "ETThaiMahaMakutDataModel";

  public ETThaiMahaMakutDataModel(Context context) {
    super(context);
  }

  @Override
  protected String getDatabasePath() {
    return Constants.MM_DATABASE_PATH;
  }

  @Override
  public BookDatabaseHelper.Language getLanguage() {
    return BookDatabaseHelper.Language.THAIMM;
  }

  @Override
  public void search(String keywords, BookDatabaseHelper.OnSearchListener listener) {
    search(keywords, listener,
        new Integer[] {
             1, 2, 3, 4, 5, 6, 7, 8, 9,10,
            11,12,13,14,15,16,17,18,19,20,
            21,22,23,24,25,26,27,28,29,30,
            31,32,33,34,35,36,37,38,39,40,
            41,42,43,44,45,46,47,48,49,50,
            51,52,53,54,55,56,57,58,59,60,
            61,62,63,64,65,66,67,68,69,70,
            71,72,73,74,75,76,77,78,79,80,
            81,82,83,84,85,86,87,88,89,90,
            91
        }
    );
  }

  @Override
  public int getSectionBoundary(int index) {
    if (index == 0) {
      return 9;
    }
    if (index == 1) {
      return 74;
    }
    return 91;
  }

  @Override
  public int convertVolume(int volume, int section, int item) {
    return BookDatabaseHelper.getThaiMMVolumeMap(mContext).get(String.format("%d-%d-%d", volume, section, item));
  }

  @Override
  public int getComparingVolume(int volume, int page) {
    openDatabase();
    Cursor cursor = db.query(getLanguage().getStringCode(), new String[] {"volume_orig"}, "volume=? AND page=?",
        new String[] { String.format("%02d", volume), String.format("%04d", page)}, null, null, null);
    int comparingVolume = volume;
    cursor.moveToFirst();
    if (!cursor.isAfterLast()) {
      comparingVolume = Integer.parseInt(cursor.getString(0).split("\\s+")[0]);
    }
    cursor.close();
    return comparingVolume;
  }

  @Override
  protected Map<String, Map<String, Map<String, ArrayList<Integer>>>> getBookItems() {
    return BookDatabaseHelper.getThaiMMBookItems(mContext);
  }

  @Override
  public void getComparingItemsAtPage(int volume, int page, BookDatabaseHelper.OnGetItemsListener listener) {
    getComparingItemsAtPage(volume, page, listener);
  }
}
