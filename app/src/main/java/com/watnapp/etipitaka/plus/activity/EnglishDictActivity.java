package com.watnapp.etipitaka.plus.activity;

import android.graphics.Typeface;
import android.os.Bundle;

import com.watnapp.etipitaka.plus.adapter.DictAdapter;
import com.watnapp.etipitaka.plus.adapter.EnglishDictAdapter;
import com.watnapp.etipitaka.plus.helper.DictDatabaseHelper;
import com.watnapp.etipitaka.plus.helper.EnglishDictDatabaseHelper;
import static org.koin.java.KoinJavaComponent.*;

/**
 * Created by sutee on 20/3/58.
 */

public class EnglishDictActivity extends DictActivity {
  protected static final String TAG = "EnglishDictActivity";

  private EnglishDictAdapter mAdapter = get(EnglishDictAdapter.class);
  private EnglishDictDatabaseHelper mDatabaseHelper = get(EnglishDictDatabaseHelper.class);

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

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

  @Override
  public Typeface getTypeface() {
    return null;
  }

}