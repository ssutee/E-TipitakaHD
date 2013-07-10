package com.watnapp.etipitaka.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockListActivity;
import com.watnapp.etipitaka.Constants;
import com.watnapp.etipitaka.R;
import com.watnapp.etipitaka.adapter.FileExplorerAdapter;
import roboguice.inject.ContentView;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 10/7/2013
 * Time: 14:49
 */

@ContentView(R.layout.activity_file_explorer)
public class FileExplorerActivity extends RoboSherlockListActivity {

  @InjectExtra(Constants.TITLE_KEY)
  private String mTitle;

  @InjectExtra(Constants.SELECT_MODE_KEY)
  private int mSelectMode;

  @InjectView(R.id.txt_title)
  private TextView mTxtTitle;

  @InjectView(R.id.edt_path)
  private EditText mEditTextPath;

  private Comparator<File> mFileComparator = new Comparator<File>() {
    @Override
    public int compare(File lhs, File rhs) {
      if (lhs.isDirectory() && rhs.isFile()) {
        return -1;
      } else if (lhs.isFile() && rhs.isDirectory()) {
        return 1;
      }
      return lhs.getName().compareTo(rhs.getName());
    }
  };

  private FileFilter mJSFileFilter = new FileFilter() {
    @Override
    public boolean accept(File path) {
      return !path.getName().startsWith(".")
          && (path.isDirectory() || path.getName()
          .toLowerCase(Locale.US).endsWith(".js"));
    }
  };

  private File mCurrentFolder = Environment.getExternalStorageDirectory();
  private File[] mFiles = mCurrentFolder.listFiles(mJSFileFilter);

  private FileExplorerAdapter mAdapter;
  private int mLevel = 0;

  @Override
  public void onBackPressed() {
    if (mLevel > 0) {
      gotoParentFolder();
    } else {
      super.onBackPressed();
    }
  }

  private void gotoParentFolder() {
    mLevel -= 1;
    mCurrentFolder = mCurrentFolder.getParentFile();
    mFiles = mCurrentFolder.listFiles(mJSFileFilter);
    mEditTextPath.setText(mCurrentFolder.getAbsolutePath());
    Arrays.sort(mFiles, mFileComparator);
    mAdapter.notifyDataSetChanged();
  }

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mTxtTitle.setText(mTitle);
    Arrays.sort(mFiles, mFileComparator);
    mEditTextPath.setText(mCurrentFolder.getAbsolutePath());
    mAdapter = new FileExplorerAdapter(this) {
      @Override
      public int getCount() {
        return mFiles.length;
      }

      @Override
      public Object getItem(int position) {
        return mFiles[position];
      }
    };
    setListAdapter(mAdapter);
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    File file = mFiles[position];
    if (file.isDirectory() && file.canRead()) {
      mLevel += 1;
      mCurrentFolder = file;
      mFiles = mCurrentFolder.listFiles(mJSFileFilter);
      mEditTextPath.setText(mCurrentFolder.getAbsolutePath());
      Arrays.sort(mFiles, mFileComparator);
      mAdapter.notifyDataSetChanged();
    } else if (mSelectMode == Constants.SELECT_MODE_FILE && file.isFile() && file.canRead()) {
      mEditTextPath.setText(file.getAbsolutePath());
      action(null);
    }
  }

  public void action(View view) {
    Intent data = new Intent();
    data.putExtra(Constants.PATH_KEY, mEditTextPath.getText().toString());
    setResult(RESULT_OK, data);
    finish();
  }
}