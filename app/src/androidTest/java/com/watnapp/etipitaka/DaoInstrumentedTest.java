package com.watnapp.etipitaka;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.plus.model.Favorite;
import com.watnapp.etipitaka.plus.model.FavoriteDaoHelper;
import com.watnapp.etipitaka.plus.model.FavoriteTable;
import com.watnapp.etipitaka.plus.model.HistoryItem;
import com.watnapp.etipitaka.plus.model.HistoryItemDaoHelper;
import com.watnapp.etipitaka.plus.model.HistoryItemTable;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class DaoInstrumentedTest {

  @Test
  public void favoriteDao_insertsReadsAndDeletesTypedRows() {
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    FavoriteDaoHelper helper = new FavoriteDaoHelper(context);
    String note = "dao-test-" + System.currentTimeMillis();

    helper.delete(FavoriteTable.FavoriteColumns.NOTE + " = ?", new String[]{note});
    try {
      Favorite favorite = new Favorite();
      favorite.setLanguage(BookDatabaseHelper.Language.THAI);
      favorite.setVolume(1);
      favorite.setPage(2);
      favorite.setItem(3);
      favorite.setNote(note);
      favorite.setScore(4);

      int id = helper.insert(favorite);
      Favorite saved = helper.getById(id);

      assertNotNull(saved);
      assertEquals(note, saved.getNote());
      assertEquals(BookDatabaseHelper.Language.THAI, saved.getLanguage());
      assertEquals(1, saved.getVolume());
      assertEquals(2, saved.getPage());
      assertEquals(3, saved.getItem());
      assertEquals(4, saved.getScore());
    } finally {
      helper.delete(FavoriteTable.FavoriteColumns.NOTE + " = ?", new String[]{note});
    }
  }

  @Test
  public void historyItemDao_insertOrUpdatePreservesReadStatus() {
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    HistoryItemDaoHelper helper = new HistoryItemDaoHelper(context);
    int historyId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

    helper.delete(HistoryItemTable.HistoryItemColumns.HISTORY_ID + " = ?",
        new String[]{String.valueOf(historyId)});
    try {
      helper.insertOrUpdate(historyId, 1, 2, HistoryItem.Status.READ);
      helper.insertOrUpdate(historyId, 1, 2, HistoryItem.Status.SKIMMED);

      HistoryItem item = helper.get(historyId, 1, 2);
      List<HistoryItem> items = helper.getByHistoryId(historyId);

      assertNotNull(item);
      assertEquals(HistoryItem.Status.READ, item.getStatus());
      assertEquals(1, items.size());
    } finally {
      helper.delete(HistoryItemTable.HistoryItemColumns.HISTORY_ID + " = ?",
          new String[]{String.valueOf(historyId)});
    }
  }
}
