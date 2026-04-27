package com.watnapp.etipitaka.plus.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.adapter.FileExplorerAdapter;
import com.watnapp.etipitaka.plus.databinding.ActivityFileExplorerBinding;
import com.watnapp.etipitaka.plus.model.FileExplorerActivityNavigationModel;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import dart.Dart;
import dart.DartModel;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 10/7/2013
 * Time: 14:49
 */

//@ContentView(R.layout.activity_file_explorer)
public class FileExplorerActivity extends ListActivity {

  private ActivityFileExplorerBinding binding;

  @DartModel
  FileExplorerActivityNavigationModel navigationModel;

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
          .toLowerCase(Locale.US).endsWith(".js") || path.getName()
          .toLowerCase(Locale.US).endsWith(".json.etz"));
    }
  };

  private File mCurrentFolder;
  private File[] mFiles;

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
    binding.edtPath.setText(mCurrentFolder.getAbsolutePath());
    Arrays.sort(mFiles, mFileComparator);
    mAdapter.notifyDataSetChanged();
  }

  private void browseDir() {
    mCurrentFolder = getExternalFilesDir(null);
    if (mCurrentFolder == null) {
      Toast.makeText(this, R.string.file_not_found, Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    mFiles = mCurrentFolder.listFiles(mJSFileFilter);
    if (mFiles != null) {
      Arrays.sort(mFiles, mFileComparator);
    }
    binding.edtPath.setText(mCurrentFolder.getAbsolutePath());
    mAdapter = new FileExplorerAdapter(this) {
      @Override
      public int getCount() {
        return mFiles != null ? mFiles.length : 0;
      }

      @Override
      public Object getItem(int position) {
        return mFiles[position];
      }
    };
    setListAdapter(mAdapter);
  }

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Dart.bind(this);
    binding = ActivityFileExplorerBinding.inflate(getLayoutInflater());
    View view = binding.getRoot();
    setContentView(view);
    binding.txtTitle.setText(navigationModel.mTitle);
    browseDir();
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    File file = mFiles[position];
    if (file.isDirectory() && file.canRead()) {
      mLevel += 1;
      mCurrentFolder = file;
      mFiles = mCurrentFolder.listFiles(mJSFileFilter);
      binding.edtPath.setText(mCurrentFolder.getAbsolutePath());
      Arrays.sort(mFiles, mFileComparator);
      mAdapter.notifyDataSetChanged();
    } else if (navigationModel.mSelectMode == Constants.SELECT_MODE_FILE && file.isFile() && file.canRead()) {
      binding.edtPath.setText(file.getAbsolutePath());
      action(null);
    }
  }

  public void action(View view) {
    Intent data = new Intent();
    data.putExtra(Constants.PATH_KEY, binding.edtPath.getText().toString());
    setResult(RESULT_OK, data);
    finish();
  }
}
