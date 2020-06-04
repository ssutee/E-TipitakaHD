package com.watnapp.etipitaka.plus.model;

import android.content.Context;
import android.util.SparseBooleanArray;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.watnapp.etipitaka.plus.model.HistoryTable.HistoryColumns;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 4/6/2013
 * Time: 12:58
 */

public class HistoryDaoHelper extends DaoHelper {

  private Dao<History> mDao;

  public HistoryDaoHelper(Context context) {
    super(context);
    mDao = new Dao<History>(History.class, context, DatabaseProvider.HISTORY_CONTENT_URI);
  }

  @Override
  protected Dao getDao() {
    return mDao;
  }

  public History get(String keywords, BookDatabaseHelper.Language language,
                     SparseBooleanArray selectedSections, boolean isBuddhawaj) {
    List<ModelBase> result = get(HistoryColumns.KEYWORDS + " LIKE ? AND "
        + HistoryColumns.LANGUAGE + " = ? AND "
        + HistoryColumns.SECTION1 + " = ? AND "
        + HistoryColumns.SECTION2 + " = ? AND "
        + HistoryColumns.SECTION3 + " = ? AND ("
        + HistoryColumns.BUDDHAWAJ + " = ? OR " + HistoryColumns.BUDDHAWAJ + (isBuddhawaj ? " = 1)" : " is null)"),
        new String[]{keywords, language.getCode() + "",
            (selectedSections != null && selectedSections.get(0, false) ? "1" : "0"),
            (selectedSections != null && selectedSections.get(1, false) ? "1" : "0"),
            (selectedSections != null && selectedSections.get(2, false) ? "1" : "0"),
            isBuddhawaj ? "1" : "0"
        });

    return result != null && result.size() > 0 ? (History) result.get(0) : null;
  }

  public boolean contains(String keywords, BookDatabaseHelper.Language language,
                          SparseBooleanArray selectedSections, boolean isBuddhawaj) {
    return get(keywords, language, selectedSections, isBuddhawaj) != null;
  }

  public void restoreJSONArray(JSONArray jsonArray) {
    HistoryItemDaoHelper historyItemDaoHelper = new HistoryItemDaoHelper(mContext);
    for (int i=0; i<jsonArray.length(); ++i) {
      try {
        JSONObject jsonObject = jsonArray.getJSONObject(i);
        SparseBooleanArray selectedSections = new SparseBooleanArray(3);
        selectedSections.put(0, jsonObject.getBoolean(HistoryColumns.SECTION1));
        selectedSections.put(1, jsonObject.getBoolean(HistoryColumns.SECTION2));
        selectedSections.put(2, jsonObject.getBoolean(HistoryColumns.SECTION3));
        String keywords = jsonObject.getString(HistoryColumns.KEYWORDS);
        BookDatabaseHelper.Language language = BookDatabaseHelper
            .Language.values()[jsonObject.getInt(HistoryColumns.LANGUAGE)];
        boolean isBuddhawaj = jsonObject.has(HistoryColumns.BUDDHAWAJ)
            ? jsonObject.getBoolean(HistoryColumns.BUDDHAWAJ) : false;
        if (!contains(keywords, language, selectedSections, isBuddhawaj)) {
          int result1 = jsonObject.getInt(HistoryColumns.RESULT1);
          int result2 = jsonObject.getInt(HistoryColumns.RESULT2);
          int result3 = jsonObject.getInt(HistoryColumns.RESULT3);
          int score = jsonObject.has(HistoryColumns.SCORE) ? jsonObject.getInt(HistoryColumns.SCORE) : 0;
          String content = jsonObject.getString(HistoryColumns.CONTENT);
          History history = new History();
          history.setLanguage(language);
          history.setContent(content);
          history.setKeywords(keywords);
          history.setResult1(result1);
          history.setResult2(result2);
          history.setResult3(result3);
          history.setScore(score);
          history.setSection1(selectedSections.get(0));
          history.setSection2(selectedSections.get(1));
          history.setSection3(selectedSections.get(2));
          history.setBuddhawaj(isBuddhawaj);
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
        jsonObject.put(HistoryColumns.SCORE, history.getScore());
        jsonObject.put(HistoryColumns.BUDDHAWAJ, history.isBuddhawaj());
        jsonObject.put(HistoryItemTable.TABLE_NAME, historyItemDaoHelper.dumpJSONArray(history.getId()));
      } catch (JSONException e) {
        e.printStackTrace();
      }
      jsonArray.put(jsonObject);
    }
    return jsonArray;
  }

  public void delete(History history) {
    HistoryItemDaoHelper historyItemDaoHelper = new HistoryItemDaoHelper(mContext);
    for (HistoryItem item : historyItemDaoHelper.getByHistoryId(history.getId())) {
      historyItemDaoHelper.delete(item);
    }
    super.delete(history);
  }
}
