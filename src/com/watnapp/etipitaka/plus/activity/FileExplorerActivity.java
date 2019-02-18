package com.watnapp.etipitaka.plus.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockListActivity;
import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.adapter.FileExplorerAdapter;
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

  private static final int REQUEST_CODE_ACCESS_EXTERNAL_STORAGE_PERMISSION = 1;

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
    mEditTextPath.setText(mCurrentFolder.getAbsolutePath());
    Arrays.sort(mFiles, mFileComparator);
    mAdapter.notifyDataSetChanged();
  }

  private void browseDir() {
    mCurrentFolder = Environment.getExternalStorageDirectory();
    mFiles = mCurrentFolder.listFiles(mJSFileFilter);
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

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mTxtTitle.setText(mTitle);

    int writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    int readExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

    if(writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED ||
        readExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this,
          new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
              Manifest.permission.READ_EXTERNAL_STORAGE},
          REQUEST_CODE_ACCESS_EXTERNAL_STORAGE_PERMISSION);
    } else {
      this.browseDir();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == REQUEST_CODE_ACCESS_EXTERNAL_STORAGE_PERMISSION) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        this.browseDir();
      } else {
        Toast.makeText(this,
            "The app was not allowed to write to your storage. " +
                "Hence, it cannot function properly. Please consider granting it this permission",
            Toast.LENGTH_LONG).show();
      }
    }
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