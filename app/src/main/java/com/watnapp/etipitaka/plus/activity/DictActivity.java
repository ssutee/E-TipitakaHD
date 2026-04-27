package com.watnapp.etipitaka.plus.activity;

import android.app.AlertDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.compose.ui.platform.ComposeView;

import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.adapter.DictAdapter;
import com.watnapp.etipitaka.plus.helper.DictDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sutee on 20/3/58.
 */
abstract public class DictActivity extends AppCompatActivity {

  protected static final String TAG = "DictActivity";

  public abstract DictDatabaseHelper getDictDatabaseHelper();
  public abstract DictAdapter getDictAdapter();
  public abstract String getFontFamily();
  public abstract String getFontFaces();
  public abstract int getFontSize();
  public abstract Typeface getTypeface();
  private ComposeView composeView;
  private List<DictHeadword> entries = new ArrayList<>();

  @Override
  protected void onDestroy() {
    getDictDatabaseHelper().closeDatabase();
    super.onDestroy();
  }

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    composeView = new ComposeView(this);
    setContentView(composeView);
    renderEntries();
    search(null);
  }

  private void search(final String headword) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        String query = headword != null ? getDictDatabaseHelper().prepareQueryString(headword) : null;
        String headwordColumn = getDictAdapter().getHeadWordColumn();
        String selection = query != null ? headwordColumn+" LIKE ? OR " + headwordColumn+" LIKE ? OR "
            + headwordColumn+" LIKE ?" : null;
        String[] selectionArgs = query != null ?
            new String[] { query + "%", "%," + query + "%", "%, " + query + "%" } : null;
        final Cursor cursor = getDictDatabaseHelper().queryHeadWords(selection, selectionArgs);
        final List<DictHeadword> results = new ArrayList<>();
        try {
          int idColumn = cursor.getColumnIndexOrThrow("_id");
          int headwordIndex = cursor.getColumnIndexOrThrow(headwordColumn);
          while (cursor.moveToNext()) {
            results.add(new DictHeadword(cursor.getInt(idColumn), cursor.getString(headwordIndex)));
          }
        } finally {
          cursor.close();
        }
        composeView.post(new Runnable() {
          @Override
          public void run() {
            entries = results;
            renderEntries();
          }
        });
      }
    }).start();
  }

  private void renderEntries() {
    DictScreenBridge.render(composeView, entries, getFontSize(), getTypeface(), this::showEntry);
  }

  private void showEntry(int position) {
    DictHeadword entry = entries.get(position);
    String content = getDictDatabaseHelper().getContentById(entry.getId());

    Typeface font = getTypeface();

    TextView title = new TextView(DictActivity.this);
    if (font != null) {
      title.setTypeface(font);
    }
    title.setTextSize(getFontSize());
    title.setTextColor(Color.BLUE);
    title.setText("  " + entry.getHeadword());
    AlertDialog dialog = new AlertDialog.Builder(DictActivity.this)
        .setCustomTitle(title).setMessage(content.trim()).create();
    dialog.show();

    TextView message = (TextView) dialog.findViewById(android.R.id.message);
    if (message != null) {
      message.setTextSize(getFontSize());
      if (font != null) {
        message.setTypeface(font);
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.dict_menu, menu);

    MenuItem searchItem = menu.findItem(R.id.action_search);
    SearchView searchView = (SearchView) searchItem.getActionView();
      assert searchView != null;
      searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String s) {
        return false;
      }

      @Override
      public boolean onQueryTextChange(String s) {
        search(s.trim().isEmpty() ? null : s);
        return true;
      }
    });
    return true;
  }
}
