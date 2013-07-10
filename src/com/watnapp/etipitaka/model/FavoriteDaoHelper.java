package com.watnapp.etipitaka.model;

import android.content.Context;
import com.google.inject.Inject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import roboguice.inject.ContextSingleton;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 9/7/2013
 * Time: 15:23
 */

@ContextSingleton
public class FavoriteDaoHelper extends DaoHelper {
  private Dao<Favorite> mDao;

  @Inject
  public FavoriteDaoHelper(Context context) {
    super(context);
    mDao = new Dao<Favorite>(Favorite.class, context, DatabaseProvider.FAVORITE_CONTENT_URI);
  }

  @Override
  protected Dao getDao() {
    return mDao;
  }

  public JSONArray dumpJSONArray() {
    JSONArray jsonArray = new JSONArray();
    for (Favorite favorite : mDao.get(null, null)) {
      JSONObject jsonObject = new JSONObject();
      try {
        jsonObject.put(FavoriteTable.FavoriteColumns.NOTE, favorite.getNote());
        jsonObject.put(FavoriteTable.FavoriteColumns.LANGUAGE, favorite.getLanguage().ordinal());
        jsonObject.put(FavoriteTable.FavoriteColumns.VOLUME, favorite.getVolume());
        jsonObject.put(FavoriteTable.FavoriteColumns.PAGE, favorite.getPage());
        jsonObject.put(FavoriteTable.FavoriteColumns.ITEM, favorite.getItem());
        jsonArray.put(jsonObject);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    return jsonArray;
  }

}
