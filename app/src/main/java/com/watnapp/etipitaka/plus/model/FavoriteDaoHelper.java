package com.watnapp.etipitaka.plus.model;

import android.content.Context;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.watnapp.etipitaka.plus.model.FavoriteTable.FavoriteColumns;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 9/7/2013
 * Time: 15:23
 */

public class FavoriteDaoHelper extends DaoHelper {
  private Dao<Favorite> mDao;

  public FavoriteDaoHelper(Context context) {
    super(context);
    mDao = new Dao<Favorite>(Favorite.class, context, DatabaseProvider.FAVORITE_CONTENT_URI);
  }

  @Override
  protected Dao getDao() {
    return mDao;
  }

  public boolean contains(int languageCode, int volume, int page, int item, String note) {
    List<Favorite> result = mDao.get(FavoriteColumns.LANGUAGE + " = ? AND " + FavoriteColumns.VOLUME + " = ? AND "
        + FavoriteColumns.PAGE + " = ? AND " + FavoriteColumns.ITEM + " = ? AND "
        + FavoriteColumns.NOTE + " LIKE ?", new String[]{languageCode + "", volume + "", page + "", item + "", note});
    return result != null && result.size() > 0;
  }

  public void restoreJSONArray(JSONArray jsonArray) {
    for (int i=0; i<jsonArray.length(); ++i) {
      try {
        JSONObject jsonObject = jsonArray.getJSONObject(i);
        int languageCode = jsonObject.getInt(FavoriteColumns.LANGUAGE);
        int volume = jsonObject.getInt(FavoriteColumns.VOLUME);
        int page = jsonObject.getInt(FavoriteColumns.PAGE);
        int item = jsonObject.getInt(FavoriteColumns.ITEM);
        int score = jsonObject.has(FavoriteColumns.SCORE) ? jsonObject.getInt(FavoriteColumns.SCORE) : 0;
        String note = jsonObject.getString(FavoriteColumns.NOTE);
        if (!contains(languageCode, volume, page, item, note)) {
          Favorite favorite = new Favorite();
          favorite.setLanguage(BookDatabaseHelper.Language.values()[languageCode]);
          favorite.setVolume(volume);
          favorite.setPage(page);
          favorite.setItem(item);
          favorite.setNote(note);
          favorite.setScore(score);
          mDao.insert(favorite);
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }

  public JSONArray dumpJSONArray() {
    JSONArray jsonArray = new JSONArray();
    for (Favorite favorite : mDao.get(null, null)) {
      JSONObject jsonObject = new JSONObject();
      try {
        jsonObject.put(FavoriteColumns.NOTE, favorite.getNote());
        jsonObject.put(FavoriteColumns.LANGUAGE, favorite.getLanguage().ordinal());
        jsonObject.put(FavoriteColumns.VOLUME, favorite.getVolume());
        jsonObject.put(FavoriteColumns.PAGE, favorite.getPage());
        jsonObject.put(FavoriteColumns.ITEM, favorite.getItem());
        jsonObject.put(FavoriteColumns.SCORE, favorite.getScore());
        jsonArray.put(jsonObject);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    return jsonArray;
  }

}
