package com.watnapp.etipitaka.plus.activity;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ListView;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.adapter.DictAdapter;
import com.watnapp.etipitaka.plus.adapter.PaliDictAdapter;
import com.watnapp.etipitaka.plus.helper.DictDatabaseHelper;
import com.watnapp.etipitaka.plus.helper.PaliDictDatabaseHelper;
import com.watnapp.etipitaka.plus.widget.ClearableAutoCompleteTextView;
import roboguice.inject.InjectView;

/**
 * Created by sutee on 20/3/58.
 */
abstract public class DictActivity extends RoboSherlockFragmentActivity {

  protected static final String TAG = "DictActivity";

  @InjectView(android.R.id.list)
  ListView mListView;
  @InjectView(R.id.edt_input)
  ClearableAutoCompleteTextView mInputEditText;

  public abstract DictDatabaseHelper getDictDatabaseHelper();
  public abstract DictAdapter getDictAdapter();
  public abstract String getFontFamily();
  public abstract String getFontFaces();
  public abstract int getFontSize();

  @Override
  protected void onDestroy() {
    getDictDatabaseHelper().closeDatabase();
    super.onDestroy();
  }

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mInputEditText.setClearDrawable(R.drawable.ic_clear_holo_light);
    mInputEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        Log.d(TAG, String.valueOf(s.toString().trim().length()));
        search(s.toString().trim().length() == 0 ? null : s.toString());
      }
    });
    search(null);
    mListView.setAdapter(getDictAdapter());
    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = getDictAdapter().getCursor();
        cursor.moveToPosition(position);
        String content = getDictDatabaseHelper().getContentById(cursor.getInt(cursor.getColumnIndex("_id")));
        String headword = cursor.getString(cursor.getColumnIndex(getDictAdapter().getHeadWordColumn()));
        WebView webView = (WebView) getLayoutInflater().inflate(R.layout.dialog_dict_message, null);
        webView.loadDataWithBaseURL("http://etipitaka.com",
            getString(R.string.html_dict_template, headword, content.trim(),
                getFontSize(), getFontFaces(), getFontFamily()),
            "text/html", "UTF-8", null);
        AlertDialog dialog = new AlertDialog.Builder(DictActivity.this)
            .setView(webView).create();
        dialog.show();
      }
    });
  }

  private void search(final String headword) {
    getDictAdapter().swapCursor(null);
    new Thread(new Runnable() {
      @Override
      public void run() {
        String selection = headword != null ? getDictAdapter().getHeadWordColumn()+" LIKE ?" : null;
        String[] selectionArgs = headword != null ? new String[] { headword + "%" } : null;
        final Cursor cursor = getDictDatabaseHelper().queryHeadWords(selection, selectionArgs);
        cursor.moveToFirst();
        mListView.post(new Runnable() {
          @Override
          public void run() {
            getDictAdapter().swapCursor(cursor);
          }
        });
      }
    }).start();
  }
}
