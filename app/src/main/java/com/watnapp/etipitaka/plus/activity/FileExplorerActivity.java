package com.watnapp.etipitaka.plus.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.R;
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

public class FileExplorerActivity extends ComponentActivity {

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
  private String mSelectedPath = "";
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
    File parentFolder = mCurrentFolder.getParentFile();
    if (parentFolder == null) {
      return;
    }
    mLevel -= 1;
    updateFolder(parentFolder);
  }

  private void browseDir() {
    mCurrentFolder = getExternalFilesDir(null);
    if (mCurrentFolder == null) {
      Toast.makeText(this, R.string.file_not_found, Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    updateFolder(mCurrentFolder);
  }

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Dart.bind(this);
    browseDir();
  }

  private void onFileClicked(int position) {
    File file = mFiles[position];
    if (file.isDirectory() && file.canRead()) {
      mLevel += 1;
      updateFolder(file);
    } else if (navigationModel.mSelectMode == Constants.SELECT_MODE_FILE && file.isFile() && file.canRead()) {
      mSelectedPath = file.getAbsolutePath();
      finishWithSelectedPath();
    }
  }

  private void updateFolder(File folder) {
    mCurrentFolder = folder;
    mFiles = mCurrentFolder.listFiles(mJSFileFilter);
    if (mFiles == null) {
      mFiles = new File[0];
    } else {
      Arrays.sort(mFiles, mFileComparator);
    }
    mSelectedPath = mCurrentFolder.getAbsolutePath();
    render();
  }

  private void render() {
    FileExplorerScreenBridge.render(
        this,
        navigationModel.mTitle != null ? navigationModel.mTitle : "",
        mSelectedPath,
        mFiles,
        path -> mSelectedPath = path,
        this::finishWithSelectedPath,
        this::onFileClicked);
  }

  private void finishWithSelectedPath() {
    Intent data = new Intent();
    data.putExtra(Constants.PATH_KEY, mSelectedPath);
    setResult(RESULT_OK, data);
    finish();
  }
}
