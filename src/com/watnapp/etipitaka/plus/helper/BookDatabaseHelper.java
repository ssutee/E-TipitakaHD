package com.watnapp.etipitaka.plus.helper;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import com.actionbarsherlock.internal.nineoldandroids.animation.ObjectAnimator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 22/5/2013
 * Time: 0:31
 */

@Singleton
public class BookDatabaseHelper {

  protected static final String TAG = "DatabaseHelper";
  private SQLiteDatabase db;

  @Inject
  private Context mContext;

  public void openDatabase() {
    if ((db == null || !db.isOpen()) && (new File(Utils.getDatabasePath(Language.THAI))).exists()) {
      db = SQLiteDatabase.openDatabase(Utils.getDatabasePath(Language.THAI), null, 0);
    }
  }

  public void closeDatabase() {
    if (db != null && db.isOpen()) {
      db.close();
    }
  }

  private int getPageId(Language language, int volume, int page) {
    openDatabase();
    int pageId = -1;
    Cursor cursor = db.query("page", new String[] {"_id"}, "language = ? AND volume = ? AND number = ?",
        new String[] {String.valueOf(language.getCode()), String.valueOf(volume), String.valueOf(page)},
        null, null, null);
    if (cursor.getCount() > 0) {
      cursor.moveToFirst();
      pageId = cursor.getInt(0);
    }
    cursor.close();
    return pageId;
  }

  public void getItemsAtPage(final Language language, final int volume, final int page,
                             final OnGetItemsListener listener) {
    openDatabase();
    new Thread(new Runnable() {
      @Override
      public void run() {
        ArrayList<Integer> items = new ArrayList<Integer>();
        ArrayList<Integer> sections = new ArrayList<Integer>();
        int pageId = getPageId(language, volume, page);
        Cursor cursor = db.query("item", new String[] {"number", "section"},
            "page_id = ?", new String[] {String.valueOf(pageId)}, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
          int number = cursor.getInt(0);
          int section = cursor.getInt(1);
          if (!items.contains(number)) {
            items.add(number);
            sections.add(section);
          }
          cursor.moveToNext();
        }
        cursor.close();
        listener.onGetItemsFinish(items.toArray(new Integer[items.size()]),
            sections.toArray(new Integer[sections.size()]));
      }
    }).start();
  }

  public Cursor read(Language language, int volume, int page) {
    openDatabase();
    Cursor cursor = db.query("page", null, "language = ? AND volume = ?",
        new String[] {String.valueOf(language.getCode()), String.valueOf(volume)}, null, null, null);
    cursor.moveToFirst();
    if (page >= 0 && page <= cursor.getCount()) {
      cursor.moveToPosition(page-1);
    }
    return cursor;
  }

  public Cursor read(Language language, int volume) {
    return read(language, volume, 0);
  }

  public int getMinimumPageNumber(Language language, int volume) {
    return 1;
  }

  public int getMaximumPageNumber(Language language, int volume) {
    openDatabase();
    Cursor cursor = db.query("page", new String[] {"number"}, "language = ? AND volume = ?",
        new String[] { String.valueOf(language.getCode()), String.valueOf(volume)},
        null, null, "number");
    cursor.moveToLast();
    int page = cursor.getInt(0);
    cursor.close();
    return page;
  }

  private int getFirstPageId(Language language, int volume) {
    openDatabase();
    Cursor cursor = db.query("page", new String[] {"_id"}, "language = ? AND volume = ?",
        new String[] { String.valueOf(language.getCode()), String.valueOf(volume)},
        null, null, null);
    cursor.moveToFirst();
    int id = cursor.getInt(0);
    cursor.close();
    return id;
  }

  private int getLastPageId(Language language, int volume) {
    openDatabase();
    Cursor cursor = db.query("page", new String[] {"_id"}, "language = ? AND volume = ?",
        new String[] { String.valueOf(language.getCode()), String.valueOf(volume)},
        null, null, null);
    cursor.moveToLast();
    int id = cursor.getInt(0);
    cursor.close();
    return id;
  }

  public int getMinimumItemNumber(Language language, int volume) {
    openDatabase();
    int pageId = getFirstPageId(language, volume);
    Cursor cursor = db.query("item", new String[] {"number"}, "page_id = ?",
        new String[] {String.valueOf(pageId)},
        null, null, null);
    cursor.moveToFirst();
    int item = cursor.getInt(0);
    cursor.close();
    return item;
  }

  public int getMaximumItemNumber(Language language, int volume) {
    openDatabase();
    int pageId = getLastPageId(language, volume);
    Cursor cursor = db.query("item", new String[] {"number"}, "page_id = ?",
        new String[] {String.valueOf(pageId)},
        null, null, null);
    cursor.moveToFirst();
    int item = cursor.getInt(0);
    cursor.close();
    return item;
  }

  private String getPagesTuple(Language language, int volume) {
    openDatabase();
    Cursor cursor = db.query("page", new String[] {"_id"}, "language = ? AND volume = ?",
        new String[] { String.valueOf(language.getCode()), String.valueOf(volume)},
        null, null, null);
    StringBuilder sb = new StringBuilder();
    cursor.moveToFirst();
    sb.append('(');
    while(!cursor.isAfterLast()) {
      sb.append(cursor.getInt(0));
      if (!cursor.isLast()) {
        sb.append(',');
      }
      cursor.moveToNext();
    }
    sb.append(')');
    cursor.close();
    return sb.toString();
  }

  private Integer[] getPageIdsByItem(Language language, int volume, int item) {
    openDatabase();
    Cursor cursor = db.query(true, "item", new String[] {"page_id"},
        "page_id IN " + getPagesTuple(language, volume) + " AND start = 1 AND number = ?",
        new String[] { String.valueOf(item) }, null, null, "page_id", null);
    ArrayList<Integer> pageIds = new ArrayList<Integer>();
    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      pageIds.add(cursor.getInt(0));
      cursor.moveToNext();
    }
    cursor.close();
    return pageIds.toArray(new Integer[pageIds.size()]);
  }

  public int getPageIdByItem(Language language, int volume, int item, int section) {
    openDatabase();
    Cursor cursor = db.query(true, "item", new String[] {"page_id"},
        "page_id IN " + getPagesTuple(language, volume) + " AND start = 1 AND number = ? AND section = ?",
        new String[] { String.valueOf(item), String.valueOf(section) }, null, null, "page_id", null);
    int pageId = -1;
    if (cursor.getCount() > 0) {
      cursor.moveToFirst();
      pageId = cursor.getInt(0);
    }
    cursor.close();
    return pageId;
  }

  public int getPageById(int pageId) {
    openDatabase();
    Cursor cursor = db.query("page", new String[] {"number"}, "_id = ?",
        new String[] { String.valueOf(pageId) }, null, null, null);
    int page = -1;
    if (cursor.getCount() > 0) {
      cursor.moveToFirst();
      page = cursor.getInt(0);
    }
    cursor.close();
    return page;
  }

  public Integer[] getPagesByItem(Language language, int volume, int item) {
    openDatabase();
    Integer[] pageIds = getPageIdsByItem(language, volume, item);
    ArrayList<Integer> pages = new ArrayList<Integer>();
    for (int pageId : pageIds) {
      Cursor cursor = db.query("page", new String[] {"number"}, "_id = ?",
          new String[] {String.valueOf(pageId)}, null, null, "number");
      cursor.moveToFirst();
      pages.add(cursor.getInt(0));
      cursor.close();
    }
    return pages.toArray(new Integer[pages.size()]);
  }

  public void search(final Language language, final String keywords, final OnSearchListener listener,
                     final Integer[] volumes) {
    openDatabase();
    new Thread(new Runnable() {
      @Override
      public void run() {
        Cursor[] cursors = new Cursor[volumes.length+1];
        int totalPages[] = new int[3];
        for (int i=0; i < volumes.length; ++i) {
          int volume = volumes[i];
          String selection = "language = ? AND volume = ?";
          ArrayList<String> selectionArgs = new ArrayList<String>();
          selectionArgs.add(String.valueOf(language.getCode()));
          selectionArgs.add(String.valueOf(volume));

          for (String keyword : keywords.split("\\s+")) {
            selection += " AND content LIKE ?";
            selectionArgs.add("%" + keyword.replace('+', ' ') + "%");
          }

          Cursor cursor = db.query("page", null, selection, selectionArgs.toArray(new String[selectionArgs.size()]),
              null, null, null);

          if (listener != null) {
            listener.onSearchProgress(keywords, volume, i+1, cursor);
          }

          if (volume >= 1 && volume <= 8) {
            totalPages[0] += cursor.getCount();
          } else if (volume >=9 && volume <= 33) {
            totalPages[1] += cursor.getCount();
          } else {
            totalPages[2] += cursor.getCount();
          }

          cursors[i+1] = cursor;

        }

        MatrixCursor headerCursor = new MatrixCursor(new String[] { "_id", "total" });
        headerCursor.addRow(new Object[] {10001, totalPages[0]});
        headerCursor.addRow(new Object[] {10002, totalPages[1]});
        headerCursor.addRow(new Object[] {10003, totalPages[2]});
        cursors[0] = headerCursor;
        if (listener != null) {
          listener.onSearchFinish(keywords, new MergeCursor(cursors), totalPages);
        }
      }
    }).start();
  }

  public void search(Language language, String keywords, OnSearchListener listener) {
    search(language, keywords, listener,
        new Integer[] {
            1,2,3,4,5,6,7,8,9,10,
            11,12,13,14,15,16,17,18,19,20,
            21,22,23,24,25,26,27,28,29,30,
            31,32,33,34,35,36,37,38,39,40,
            41,42,43,44,45
        }
    );
  }

  public interface OnGetItemsListener {
    public void onGetItemsFinish(Integer[] items, Integer[] sections);
  }

  public interface OnSearchListener {

    public void onSearchProgress(String keywords, int volume, int progress, Cursor cursor);
    public void onSearchFinish(String keywords, Cursor cursor, int[] totalPages);

  }

  public interface OnConvertToPivotListener {
    public void onConvertToPivotFinish(int volume, int item, int section);
  }

  public interface OnConvertFromPivotListener {
    public void onConvertFromPivotFinish(int volume, int page);
  }

  public enum SearchType {
    ALL(1), BUDDHAWAJ(2);

    private int code;

    private SearchType(int code) {
      this.code = code;
    }

    public int getCode() {
      return this.code;
    }
  }

  public enum Language {
    THAI(0), PALI(1), THAIMM(2), THAIMC(3), THAIBT(4), THAIWN(5), THAIPB(6), ROMANCT(7);

    private int code;

    private Language(int code) {
      this.code = code;
    }

    public int getCode() {
      return code;
    }

    public String getFullName(Context context) {
      switch (code) {
        case 0:
          return context.getString(R.string.thai_full_name);
        case 1:
          return context.getString(R.string.pali_full_name);
        case 2:
          return context.getString(R.string.thaimm_full_name);
        case 3:
          return context.getString(R.string.thaimc_full_name);
        case 4:
          return context.getString(R.string.thaibt_full_name);
        case 5:
          return context.getString(R.string.thaiwn_full_name);
        case 6:
          return context.getString(R.string.thaipb_full_name);
        case 7:
          return context.getString(R.string.romanct_full_name);
      }
      return null;
    }

    public String getStringCode() {
      switch (code) {
        case 0:
          return "thai";
        case 1:
          return "pali";
        case 2:
          return "thaimm";
        case 3:
          return "thaimc";
        case 4:
          return "thaibt";
        case 5:
          return "thaiwn";
        case 6:
          return "thaipb";
        case 7:
          return "romanct";
      }
      return null;
    }
  }

  static Map<String, Integer> thaiMMVolumeMap = null;
  public static Map<String, Integer> getThaiMMVolumeMap(Context context) {
    if (thaiMMVolumeMap != null) {
      return thaiMMVolumeMap;
    }
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("volume_mapping_thaimm.json"), "UTF-8"));
      Map<String, Integer> result = new Gson().fromJson(reader, new TypeToken<Map<String, Integer>>() {}.getType());
      reader.close();
      return result;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  static Map<String,Map<String,Map<String,ArrayList<Integer>>>> thaiBookItems = null;
  static Map<String,Map<String,Map<String,ArrayList<Integer>>>> paliBookItems = null;
  static Map<String,Map<String,Map<String,ArrayList<Integer>>>> thaiMMBookItems = null;
  static Map<String,Map<String,Map<String,ArrayList<Integer>>>> thaiMMOriginBookItems = null;
  static Map<String,Map<String,Map<String,ArrayList<Integer>>>> thaiMCBookItems = null;
  static Map<String,Map<String,Map<String,ArrayList<Integer>>>> thaiWNBookItems = null;
  static Map<String,Map<String,Map<String,Integer>>> romanPageIndex = null;
  static Map<String,Map<String,ArrayList<ArrayList<Integer>>>> romanItems = null;
  static Map<String, Map<String, ArrayList<ArrayList<Integer>>>> romanMappingTable = null;
  static Map<String, Map<String, Map<String, ArrayList<Integer>>>> romanReverseMappingTable = null;

  private static Map<String,Map<String,Map<String,ArrayList<Integer>>>> getBookItems(Context context, String filename) {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename), "UTF-8"));
      Map<String,Map<String,Map<String,ArrayList<Integer>>>> result = new Gson().fromJson(reader,
          new TypeToken<Map<String,Map<String,Map<String,ArrayList<Integer>>>>>(){}.getType());
      reader.close();
      return result;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static Map<String, Map<String, Map<String, ArrayList<Integer>>>> getRomanReverseMappingTable(Context context) {
    if (romanReverseMappingTable != null) {
      return romanReverseMappingTable;
    }
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("map_cst_r.json"), "UTF-8"));
      romanReverseMappingTable = new Gson().fromJson(reader,
          new TypeToken<Map<String, Map<String, Map<String, ArrayList<Integer>>>>>(){}.getType());
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return romanReverseMappingTable;
  }

  public static Map<String, Map<String, ArrayList<ArrayList<Integer>>>> getRomanMappingTable(Context context) {
    if (romanMappingTable != null) {
      return romanMappingTable;
    }
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("map_cst.json"), "UTF-8"));
      romanMappingTable = new Gson().fromJson(reader,
          new TypeToken<Map<String, Map<String, ArrayList<ArrayList<Integer>>>>>() {
          }.getType());
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return romanMappingTable;
  }

  public static Map<String,Map<String,Map<String,Integer>>> getRomanPageIndex(Context context) {
    if (romanPageIndex != null) {
      return romanPageIndex;
    }
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("roman_page_index.json"), "UTF-8"));
      romanPageIndex = new Gson().fromJson(reader, new TypeToken<Map<String,Map<String,Map<String,Integer>>>>(){}.getType());
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return romanPageIndex;
  }

  public static Map<String,Map<String,ArrayList<ArrayList<Integer>>>> getRomanItems(Context context) {
    if (romanItems != null) {
      return romanItems;
    }
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("roman_items.json"), "UTF-8"));
      romanItems = new Gson().fromJson(reader, new TypeToken<Map<String,Map<String,ArrayList<ArrayList<Integer>>>>>(){}.getType());
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return romanItems;

  }

  public static Map<String,Map<String,Map<String,ArrayList<Integer>>>> getThaiBookItems(Context context) {
    if (thaiBookItems != null) {
      return thaiBookItems;
    }
    thaiBookItems = getBookItems(context, "book_item_thai.json");
    return thaiBookItems;
  }

  public static Map<String,Map<String,Map<String,ArrayList<Integer>>>> getPaliBookItems(Context context) {
    if (paliBookItems != null) {
      return paliBookItems;
    }
    paliBookItems = getBookItems(context, "book_item_pali.json");
    return paliBookItems;
  }

  public static Map<String,Map<String,Map<String,ArrayList<Integer>>>> getThaiMMBookItems(Context context) {
    if (thaiMMBookItems != null) {
      return thaiMMBookItems;
    }
    thaiMMBookItems = getBookItems(context, "book_item_thaimm.json");
    return thaiMMBookItems;
  }

  public static Map<String,Map<String,Map<String,ArrayList<Integer>>>> getThaiMMOriginBookItems(Context context) {
    if (thaiMMOriginBookItems != null) {
      return thaiMMOriginBookItems;
    }
    thaiMMOriginBookItems = getBookItems(context, "book_item_thaimm_orig.json");
    return thaiMMOriginBookItems;
  }

  public static Map<String,Map<String,Map<String,ArrayList<Integer>>>> getThaiMCBookItems(Context context) {
    if (thaiMCBookItems != null) {
      return thaiMCBookItems;
    }
    thaiMCBookItems = getBookItems(context, "book_item_thaimc.json");
    return thaiMCBookItems;
  }

  public static Map<String,Map<String,Map<String,ArrayList<Integer>>>> getThaiWNBookItems(Context context) {
    if (thaiWNBookItems != null) {
      return thaiWNBookItems;
    }
    thaiWNBookItems = getBookItems(context, "book_item_thaiwn.json");
    return thaiWNBookItems;
  }

  public static int getSubItem(Context context, Language language, int volume, int page, int item) {
    Map<String,Map<String,Map<String,ArrayList<Integer>>>> bookItems = null;
    switch (language) {
      case THAI:
        bookItems = getThaiBookItems(context);
        break;
      case PALI:
        bookItems = getPaliBookItems(context);
        break;
      case THAIMM:
        bookItems = getThaiMMBookItems(context);
        break;
      case THAIMC:
        bookItems = getThaiMCBookItems(context);
        break;
      case THAIWN:
        bookItems = getThaiWNBookItems(context);
        break;
    }

    if (bookItems == null) {
      return 1;
    }

    for (String section : bookItems.get(volume+"").keySet()) {
      if (bookItems.get(volume+"").get(section).containsKey(item+"") &&
          bookItems.get(volume+"").get(section).get(item+"").contains(page)) {
        return Integer.parseInt(section);
      }
    }

    return 1;
  }

  static Map<String, Object> thaiMCConvertItemMap;

  public static Map<String, Object> getThaiMCConvertItemMap(Context context) {
    if (thaiMCConvertItemMap != null) {
      return thaiMCConvertItemMap;
    }

    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("mc_map.json"), "UTF-8"));
      thaiMCConvertItemMap = new Gson().fromJson(reader, new TypeToken<Map<String, Object>>(){}.getType());
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return thaiMCConvertItemMap;
  }
}
