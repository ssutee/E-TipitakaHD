package com.watnapp.etipitaka.plus.activity;

import android.app.AlertDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.adapter.DictAdapter;
import com.watnapp.etipitaka.plus.databinding.ActivityDictBinding;
import com.watnapp.etipitaka.plus.helper.DictDatabaseHelper;

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
  private ActivityDictBinding binding;

  @Override
  protected void onDestroy() {
    getDictDatabaseHelper().closeDatabase();
    super.onDestroy();
  }

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityDictBinding.inflate(getLayoutInflater());
    View view = binding.getRoot();
    setContentView(view);
//    binding.edtInput.setClearDrawable(R.drawable.ic_clear_holo_light);
//    binding.edtInput.addTextChangedListener(new TextWatcher() {
//      @Override
//      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//      }
//
//      @Override
//      public void onTextChanged(CharSequence s, int start, int before, int count) {
//      }
//
//      @Override
//      public void afterTextChanged(Editable s) {
//        Log.d(TAG, String.valueOf(s.toString().trim().length()));
//        search(s.toString().trim().isEmpty() ? null : s.toString());
//      }
//    });
    search(null);
    binding.list.setAdapter(getDictAdapter());
    binding.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = getDictAdapter().getCursor();
        cursor.moveToPosition(position);
        String content = getDictDatabaseHelper().getContentById(cursor.getInt(cursor.getColumnIndex("_id")));
        String headword = cursor.getString(cursor.getColumnIndex(getDictAdapter().getHeadWordColumn()));

        Typeface font = getTypeface();

        TextView title = new TextView(DictActivity.this);
        if (font != null) {
          title.setTypeface(font);
        }
        title.setTextSize(getFontSize());
        title.setTextColor(Color.BLUE);
        title.setText("  " + headword);
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
    });
  }

  private void search(final String headword) {
    getDictAdapter().swapCursor(null);
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
        cursor.moveToFirst();
        binding.list.post(new Runnable() {
          @Override
          public void run() {
            getDictAdapter().swapCursor(cursor);
          }
        });
      }
    }).start();
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
