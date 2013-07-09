package com.watnapp.etipitaka.model;

import android.content.Context;
import com.google.inject.Inject;
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
}
