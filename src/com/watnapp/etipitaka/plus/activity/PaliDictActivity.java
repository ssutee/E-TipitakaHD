package com.watnapp.etipitaka.plus.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockListActivity;
import com.google.inject.Inject;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.adapter.PaliDictAdapter;
import com.watnapp.etipitaka.plus.helper.PaliDictDatabaseHelper;
import com.watnapp.etipitaka.plus.widget.ClearableAutoCompleteTextView;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 15/7/2013
 * Time: 14:05
 */

@ContentView(R.layout.activity_palidict)
public class PaliDictActivity extends RoboSherlockFragmentActivity {

  private static final String TAG = "PaliDictActivity";
  @Inject
  private PaliDictDatabaseHelper mDatabaseHelper;

  @Inject
  private PaliDictAdapter mAdapter;

  @InjectView(android.R.id.list)
  private ListView mListView;

  @InjectView(R.id.edt_input)
  private ClearableAutoCompleteTextView mInputEditText;

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
    mListView.setAdapter(mAdapter);
    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);
        String content = mDatabaseHelper.getContentById(cursor.getInt(cursor.getColumnIndex("_id")));
        String headword = cursor.getString(cursor.getColumnIndex("headword"));
        WebView webView = (WebView) getLayoutInflater().inflate(R.layout.dialog_dict_message, null);
        String fontFamily = getString(Build.VERSION.SDK_INT >= 15 ? R.string.font_family_new : R.string.font_family_old);
        webView.loadDataWithBaseURL("http://etipitaka.com",
            getString(R.string.html_dict_template, headword, content.trim(), 28, fontFamily),
            "text/html", "UTF-8", null);
        AlertDialog dialog = new AlertDialog.Builder(PaliDictActivity.this)
            .setView(webView).create();
        dialog.show();
      }
    });
  }

  private void search(final String headword) {
    mAdapter.swapCursor(null);
    new Thread(new Runnable() {
      @Override
      public void run() {
        String selection = headword != null ? "headword LIKE ?" : null;
        String[] selectionArgs = headword != null ? new String[] { headword + "%" } : null;
        final Cursor cursor = mDatabaseHelper.queryHeadWords(selection, selectionArgs);
        cursor.moveToFirst();
        mListView.post(new Runnable() {
          @Override
          public void run() {
            mAdapter.swapCursor(cursor);
          }
        });
      }
    }).start();
  }
}