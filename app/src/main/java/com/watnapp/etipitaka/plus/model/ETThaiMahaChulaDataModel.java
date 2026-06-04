package com.watnapp.etipitaka.plus.model;

import android.content.Context;
import android.util.Log;

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
    return Utils.getDatabasePath(mContext, BookDatabaseHelper.Language.THAIMC);
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
    if (!needConvertToSiamrat) {
      return super.getPagesByItem(volume, item, false);
    }
    Map<String, Object> map = BookDatabaseHelper.getThaiMCConvertItemMap(mContext);
    if (map == null) {
      return new Integer[0];
    }
    // The MC mapping table has scattered gaps for some items. When the
    // exact item is absent, walk down to the nearest lower item that
    // exists so jump-to-item by Siam indexing lands on the right page
    // vicinity instead of failing silently. Mirrors PC reference
    // Engine._NearestItemPage and iOS ETDifferIndexModel.queryPages.
    for (int target = item; target > 0; --target) {
      ArrayList<Integer> pages = new ArrayList<>();
      int section = 1;
      while (true) {
        Object page = map.get(String.format("v%d-%d-i%d", volume, section, target));
        if (page == null) {
          break;
        }
        try {
          pages.add(Math.round(Float.parseFloat(page.toString())));
        } catch (NumberFormatException ignored) {
          // Skip malformed entry, continue with next section.
        }
        section += 1;
      }
      if (!pages.isEmpty()) {
        return pages.toArray(new Integer[0]);
      }
    }
    return new Integer[0];
  }

  @Override
  public void getComparingItemsAtPage(int volume, int page, BookDatabaseHelper.OnGetItemsListener listener) {
    if (listener == null) return;
    Map<String, Object> map = BookDatabaseHelper.getThaiMCConvertItemMap(mContext);
    Object raw = map == null ? null : map.get(String.format("v%d-p%d", volume, page));
    if (!(raw instanceof List)) {
      listener.onGetItemsFinish(null, null);
      return;
    }
    List<?> pair = (List<?>) raw;
    if (pair.size() < 2 || pair.get(0) == null || pair.get(1) == null) {
      listener.onGetItemsFinish(null, null);
      return;
    }
    try {
      int itemNumber = Math.round(Float.parseFloat(pair.get(0).toString()));
      int section = Math.round(Float.parseFloat(pair.get(1).toString()));
      listener.onGetItemsFinish(new Integer[] { itemNumber }, new Integer[] { section });
    } catch (Exception e) {
      Log.e(TAG, "getComparingItemsAtPage failed for volume=" + volume + ", page=" + page, e);
      listener.onGetItemsFinish(null, null);
    }
  }

  @Override
  public int getPageByItem(int volume, int item, int section, boolean needConvertToSiamrat) {
    if (!needConvertToSiamrat) {
      return super.getPageByItem(volume, item, section, false);
    }

    Map<String, Object> map = BookDatabaseHelper.getThaiMCConvertItemMap(mContext);
    if (map == null) {
      return 0;
    }
    // Mapping table has scattered gaps (e.g. v37-1-i124 absent while
    // v37-1-i123 exists). Walk down to the nearest lower item that exists
    // so compare lands on the right page vicinity instead of page 0.
    // Mirrors PC reference Engine._NearestItemPage and iOS
    // ETDifferIndexModel.convert(fromPivot:).
    for (int i = item; i > 0; --i) {
      Object page = map.get(String.format("v%d-%d-i%d", volume, section, i));
      if (page != null) {
        try {
          return Math.round(Float.parseFloat(page.toString()));
        } catch (NumberFormatException ignored) {
          // Skip malformed entry and keep walking down.
        }
      }
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
