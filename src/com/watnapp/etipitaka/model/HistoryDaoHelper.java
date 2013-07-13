package com.watnapp.etipitaka.model;

import android.content.Context;
import android.util.SparseBooleanArray;
import com.google.inject.Inject;
import com.watnapp.etipitaka.helper.BookDatabaseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import roboguice.inject.ContextSingleton;
import com.watnapp.etipitaka.model.HistoryTable.HistoryColumns;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 4/6/2013
 * Time: 12:58
 */

@ContextSingleton
public class HistoryDaoHelper extends DaoHelper {

  private Dao<History> mDao;

  @Inject
  public HistoryDaoHelper(Context context) {
    super(context);
    mDao = new Dao<History>(History.class, context, DatabaseProvider.HISTORY_CONTENT_URI);
  }

  @Override
  protected Dao getDao() {
    return mDao;
  }

  public History get(String keywords, BookDatabaseHelper.Language language,
                     SparseBooleanArray selectedSections) {
    List<ModelBase> result = get(HistoryTable.HistoryColumns.KEYWORDS + " LIKE ? AND "
        + HistoryTable.HistoryColumns.LANGUAGE + " = ? AND "
        + HistoryTable.HistoryColumns.SECTION1 + " = ? AND "
        + HistoryTable.HistoryColumns.SECTION2 + " = ? AND "
        + HistoryTable.HistoryColumns.SECTION3 + " = ?",
        new String[]{keywords, language.getCode() + "",
            (selectedSections.get(0, false) ? "1" : "0"),
            (selectedSections.get(1, false) ? "1" : "0"),
            (selectedSections.get(2, false) ? "1" : "0")});

    return result != null && result.size() > 0 ? (History) result.get(0) : null;
  }

  public boolean contains(String keywords, BookDatabaseHelper.Language language,
                          SparseBooleanArray selectedSections) {
    return get(keywords, language, selectedSections) != null;
  }

  public void restoreJSONArray(JSONArray jsonArray) {
    HistoryItemDaoHelper historyItemDaoHelper = new HistoryItemDaoHelper(mContext);
    for (int i=0; i<jsonArray.length(); ++i) {
      try {
        JSONObject jsonObject = jsonArray.getJSONObject(i);
        SparseBooleanArray selectedSections = new SparseBooleanArray(3);
        selectedSections.put(0, jsonObject.getBoolean(HistoryColumns.SECTION1));
        selectedSections.put(1, jsonObject.getBoolean(HistoryColumns.SECTION2));
        selectedSections.put(1, jsonObject.getBoolean(HistoryColumns.SECTION3));
        String keywords = jsonObject.getString(HistoryColumns.KEYWORDS);
        BookDatabaseHelper.Language language = BookDatabaseHelper
            .Language.values()[jsonObject.getInt(HistoryColumns.LANGUAGE)];
        if (!contains(keywords, language, selectedSections)) {
          int result1 = jsonObject.getInt(HistoryColumns.RESULT1);
          int result2 = jsonObject.getInt(HistoryColumns.RESULT2);
          int result3 = jsonObject.getInt(HistoryColumns.RESULT3);
          String content = jsonObject.getString(HistoryColumns.CONTENT);
          History history = new History();
          history.setLanguage(language);
          history.setContent(content);
          history.setKeywords(keywords);
          history.setResult1(result1);
          history.setResult2(result2);
          history.setResult3(result3);
          history.setSection1(selectedSections.get(0));
          history.setSection2(selectedSections.get(1));
          history.setSection3(selectedSections.get(2));
          int historyId = insert(history);
          historyItemDaoHelper.restoreJSONArray(historyId,
              jsonObject.getJSONArray(HistoryItemTable.TABLE_NAME));
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }
  public JSONArray dumpJSONArray() {
    HistoryItemDaoHelper historyItemDaoHelper = new HistoryItemDaoHelper(mContext);
    JSONArray jsonArray = new JSONArray();
    for (History history : mDao.get(null, null)) {
      JSONObject jsonObject = new JSONObject();
      try {
        jsonObject.put(HistoryColumns.CONTENT, history.getContent());
        jsonObject.put(HistoryColumns.KEYWORDS, history.getKeywords());
        jsonObject.put(HistoryColumns.LANGUAGE, history.getLanguage().ordinal());
        jsonObject.put(HistoryColumns.RESULT1, history.getResult1());
        jsonObject.put(HistoryColumns.RESULT2, history.getResult2());
        jsonObject.put(HistoryColumns.RESULT3, history.getResult3());
        jsonObject.put(HistoryColumns.SECTION1, history.isSection1());
        jsonObject.put(HistoryColumns.SECTION2, history.isSection2());
        jsonObject.put(HistoryColumns.SECTION3, history.isSection3());
        jsonObject.put(HistoryItemTable.TABLE_NAME, historyItemDaoHelper.dumpJSONArray(history.getId()));
      } catch (JSONException e) {
        e.printStackTrace();
      }
      jsonArray.put(jsonObject);
    }
    return jsonArray;
  }
}
