package com.watnapp.etipitaka.plus.model;

import android.content.Context;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sutee on 19/2/14.
 */
public class ETThaiMahaChulaDataModel extends ETBasicDataModel {

  protected static final String TAG = "ETThaiMahaChulaDataModel";

  public ETThaiMahaChulaDataModel(Context context) {
    super(context);
  }

  @Override
  protected String getDatabasePath() {
    return Utils.getDatabasePath(BookDatabaseHelper.Language.THAIMC);
  }

  @Override
  public BookDatabaseHelper.Language getLanguage() {
    return BookDatabaseHelper.Language.THAIMC;
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
  public boolean hasFooter() {
    return true;
  }

  @Override
  public String getFooterColumn() {
    return "footer";
  }

  @Override
  protected Map<String, Map<String, Map<String, ArrayList<Integer>>>> getBookItems() {
    return BookDatabaseHelper.getThaiMCBookItems(mContext);
  }

  @Override
  public Integer[] getPagesByItem(int volume, int item, boolean needConvertToSiamrat) {
    if (needConvertToSiamrat) {
      ArrayList<Integer> pages = new ArrayList<Integer>();
      int section = 1;
      while (true) {
        Object page = BookDatabaseHelper.getThaiMCConvertItemMap(mContext).get(String.format("v%d-%d-i%d", volume, section, item));
        if (page != null) {
          pages.add(Math.round(Float.parseFloat(page.toString())));
          section += 1;
          continue;
        }
        break;
      }
      return pages.toArray(new Integer[pages.size()]);
    }
    return super.getPagesByItem(volume, item, needConvertToSiamrat);
  }

  @Override
  public void getComparingItemsAtPage(int volume, int page, BookDatabaseHelper.OnGetItemsListener listener) {
    List<Double> pair = (List<Double>) BookDatabaseHelper.getThaiMCConvertItemMap(mContext).get(String.format("v%d-p%d", volume, page));
    listener.onGetItemsFinish(new Integer[] { Math.round(Float.parseFloat(pair.get(0).toString())) },
        new Integer[] { Math.round(Float.parseFloat(pair.get(1).toString()))});
  }

  @Override
  public int getPageByItem(int volume, int item, int section, boolean needConvertToSiamrat) {
    if (!needConvertToSiamrat) {
      return super.getPageByItem(volume, item, section, needConvertToSiamrat);
    }

    Object page = BookDatabaseHelper.getThaiMCConvertItemMap(mContext).get(String.format("v%d-%d-i%d", volume, section, item));
    if (page != null) {
      return Math.round(Float.parseFloat(page.toString()));
    }
    return 0;
  }

  @Override
  public int getTotalVolumes() {
    return 45;
  }


  @Override
  public String getShortTitle() {
    return mContext.getString(R.string.thaimc_short_name);
  }

}
