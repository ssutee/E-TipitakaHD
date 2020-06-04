package com.watnapp.etipitaka.plus.model;

import android.content.Context;
import android.database.Cursor;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sutee on 29/10/18.
 */
public class ETThaiVinayaDataModel extends ETHandbookDataModel {

  private Context context;

  public ETThaiVinayaDataModel(Context context) {
    super(context);
    this.context = context;
  }

  @Override
  protected String getDatabasePath() {
    return Utils.getDatabasePath(mContext, BookDatabaseHelper.Language.THAIVN);
  }

  @Override
  public BookDatabaseHelper.Language getLanguage() {
    return BookDatabaseHelper.Language.THAIVN;
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
  public String getShortTitle() {
    return mContext.getString(R.string.thaivn_short_name);
  }

  @Override
  public void getItemsAtPage(final int volume, final int page, final BookDatabaseHelper.OnGetItemsListener listener) {
    if (volume > 9) {
      listener.onGetItemsFinish(null, null);
      return;
    }
    openDatabase();
    new Thread(new Runnable() {
      @Override
      public void run() {
        Cursor cursor = db.query("main", null, "volume=? AND page=?",
            new String[]{String.valueOf(volume), String.valueOf(page)}, null, null, null);
        if (cursor.getCount() == 0) {
          listener.onGetItemsFinish(null, null);
        } else {
          cursor.moveToFirst();
          String content = cursor.getString(cursor.getColumnIndex("content"));

          Pattern pattern = Pattern.compile("\\[([๐-๙]+)\\]");
          Matcher matcher = pattern.matcher(content);
          ArrayList<Integer> items = new ArrayList<Integer>();
          ArrayList<Integer> sections = new ArrayList<Integer>();
          while (matcher.find()) {
            String item = Utils.convertToArabicNumber(ETThaiVinayaDataModel.this.context, matcher.group(1));
            items.add(Integer.parseInt(item));
            sections.add(1);
          }
          if (items.size() > 0) {
            listener.onGetItemsFinish(items.toArray(new Integer[items.size()]),
                sections.toArray(new Integer[sections.size()]));
          } else {
            listener.onGetItemsFinish(null, null);
          }
        }
      }
    }).start();
  }

  @Override
  public void getComparingItemsAtPage(int volume, int page, BookDatabaseHelper.OnGetItemsListener listener) {
    getItemsAtPage(volume, page, listener);
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
  public boolean hasHtmlContent() {
    return true;
  }

  @Override
  public Integer[] getPagesByItem(int volume, int item) {
    return new Integer[0];
  }

  @Override
  public int getComparingVolume(int volume, int page) {
    return (volume == 1 ? 9 : volume-1);
  }

  @Override
  public void search(String keywords, BookDatabaseHelper.OnSearchListener listener) {
    search(keywords, listener, new Integer[] {
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
    });
  }

  @Override
  public int getSectionBoundary(int index) {
    return 11;
  }

  @Override
  public int getTotalVolumes() {
    return 11;
  }
}
