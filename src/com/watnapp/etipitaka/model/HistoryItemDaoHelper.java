package com.watnapp.etipitaka.model;

import android.content.Context;
import com.google.inject.Inject;
import roboguice.inject.ContextSingleton;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 4/6/2013
 * Time: 19:51
 */

@ContextSingleton
public class HistoryItemDaoHelper extends DaoHelper {
  private Dao<HistoryItem> mDao;

  @Inject
  public HistoryItemDaoHelper(Context context) {
    super(context);
    mDao = new Dao<HistoryItem>(HistoryItem.class, context, DatabaseProvider.HISTORY_ITEM_CONTENT_URI);
  }

  @Override
  protected Dao getDao() {
    return mDao;
  }

  public List<HistoryItem> getByHistoryId(int historyId) {
    return mDao.get(HistoryItemTable.HistoryItemColumns.HISTORY_ID + " = ?", new String[] { historyId+""});
  }

  public void insertOrUpdate(int historyId, int volume, int page, HistoryItem.Status status) {
    HistoryItem item = get(historyId, volume, page);
    if (item != null) {
      if (!(status == HistoryItem.Status.SKIMMED && item.getStatus() == HistoryItem.Status.READ)
          && item.getStatus() != status) {
        item.setStatus(status);
        mDao.update(item);
      }
    } else {
      item = new HistoryItem();
      item.setStatus(status);
      item.setHistoryId(historyId);
      item.setVolume(volume);
      item.setPage(page);
      mDao.insert(item);
    }
  }

  public HistoryItem get(int historyId, int volume, int page) {
    List<HistoryItem> result = mDao.get(HistoryItemTable.HistoryItemColumns.HISTORY_ID + " = ? AND "
        + HistoryItemTable.HistoryItemColumns.VOLUME + " = ? AND "
        + HistoryItemTable.HistoryItemColumns.PAGE + " = ?",
        new String[]{historyId + "", volume + "", page + ""});
    return result != null && result.size() > 0 ? result.get(0) : null;
  }
}
