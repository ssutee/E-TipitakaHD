package com.watnapp.etipitaka.plus.activity;

import com.google.inject.Inject;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.adapter.DictAdapter;
import com.watnapp.etipitaka.plus.adapter.EnglishDictAdapter;
import com.watnapp.etipitaka.plus.helper.DictDatabaseHelper;
import com.watnapp.etipitaka.plus.helper.EnglishDictDatabaseHelper;
import roboguice.inject.ContentView;

/**
 * Created by sutee on 20/3/58.
 */


@ContentView(R.layout.activity_dict)
public class EnglishDictActivity extends DictActivity {
  protected static final String TAG = "EnglishDictActivity";

  @Inject
  private EnglishDictAdapter mAdapter;

  @Inject
  private EnglishDictDatabaseHelper mDatabaseHelper;

  @Override
  public DictDatabaseHelper getDictDatabaseHelper() {
    return mDatabaseHelper;
  }

  @Override
  public DictAdapter getDictAdapter() {
    return mAdapter;
  }

  @Override
  public String getFontFamily() {
    return "";
  }

  @Override
  public String getFontFaces() {
    return "";
  }

  @Override
  public int getFontSize() {
    return 18;
  }

}