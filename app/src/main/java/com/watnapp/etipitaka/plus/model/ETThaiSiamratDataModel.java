package com.watnapp.etipitaka.plus.model;

import android.content.Context;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by sutee on 4/2/14.
 */

public class ETThaiSiamratDataModel extends ETBasicDataModel {
  public ETThaiSiamratDataModel(Context context) {
    super(context);
  }

  @Override
  protected Map<String, Map<String, Map<String, ArrayList<Integer>>>> getBookItems() {
    return BookDatabaseHelper.getThaiBookItems(mContext);
  }

  @Override
  protected String getDatabasePath() {
    return Utils.getDatabasePath(mContext, BookDatabaseHelper.Language.THAI);
  }

  @Override
  public BookDatabaseHelper.Language getLanguage() {
    return BookDatabaseHelper.Language.THAI;
  }

  @Override
  public String getShortTitle() {
    return mContext.getString(R.string.thai_short_name);
  }

  @Override
  public void getComparingItemsAtPage(int volume, int page, BookDatabaseHelper.OnGetItemsListener listener) {
    getItemsAtPage(volume, page, listener);
  }

  @Override
  public void search(String keywords, BookDatabaseHelper.OnSearchListener listener) {
    search(keywords, listener,
        new Integer[] {
            1,2,3,4,5,6,7,8,9,10,
            11,12,13,14,15,16,17,18,19,20,
            21,22,23,24,25,26,27,28,29,30,
            31,32,33,34,35,36,37,38,39,40,
            41,42,43,44,45
        }
    );
  }

  @Override
  public int getSectionBoundary(int index) {
    if (index == 0) {
      return 8;
    }
    if (index == 1) {
      return 33;
    }
    return 45;
  }

  @Override
  public int getTotalVolumes() {
    return 45;
  }
}
