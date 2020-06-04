package com.watnapp.etipitaka.plus.model;

import android.content.Context;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;

/**
 * Created by sutee on 12/12/14.
 */
public class ETThaiPocketBookDataModel extends ETHandbookDataModel {

  public ETThaiPocketBookDataModel(Context context) {
    super(context);
  }

  @Override
  protected String getDatabasePath() {
    return Utils.getDatabasePath(mContext, BookDatabaseHelper.Language.THAIPB);
  }

  @Override
  public BookDatabaseHelper.Language getLanguage() {
    return BookDatabaseHelper.Language.THAIPB;
  }

  @Override
  public String getContentColumn() {
    return "content";
  }

  @Override
  public String getPageNumberColumn() {
    return "page";
  }

  @Override
  public void getItemsAtPage(final int volume, final int page, final BookDatabaseHelper.OnGetItemsListener listener) {
    listener.onGetItemsFinish(null, null);
  }

  @Override
  public void getComparingItemsAtPage(int volume, int page, BookDatabaseHelper.OnGetItemsListener listener) {
    listener.onGetItemsFinish(null, null);
  }

  @Override
  public int getMinimumItemNumber(int volume) {
    return 0;
  }

  @Override
  public int getMaximumItemNumber(int volume) {
    return 0;
  }

  @Override
  public int getPageIdByItem(int volume, int item, int section) {
    return 0;
  }

  @Override
  public Integer[] getPagesByItem(int volume, int item) {
    return null;
  }

  @Override
  public boolean hasHtmlContent() {
    return true;
  }

  @Override
  public void search(String keywords, BookDatabaseHelper.OnSearchListener listener) {
    search(keywords, listener, new Integer[] {
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19
    });
  }

  @Override
  public int getSectionBoundary(int index) {
    return 19;
  }

  @Override
  public int getTotalVolumes() {
    return 19;
  }


  @Override
  public void convertToPivot(int volume, int page, int item, BookDatabaseHelper.OnConvertToPivotListener listener) {
    listener.onConvertToPivotFinish(1, item, 1);
  }

  @Override
  public void convertFromPivot(int volume, int item, int section, BookDatabaseHelper.OnConvertFromPivotListener listener) {
    listener.onConvertFromPivotFinish(1,1);
  }

  @Override
  public String getShortTitle() {
    return mContext.getString(R.string.thaipb_short_name);
  }

}
