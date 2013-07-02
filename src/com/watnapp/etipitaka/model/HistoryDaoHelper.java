package com.watnapp.etipitaka.model;

import android.content.Context;
import android.util.SparseBooleanArray;
import com.google.inject.Inject;
import com.watnapp.etipitaka.helper.BookDatabaseHelper;
import roboguice.inject.ContextSingleton;

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
}
