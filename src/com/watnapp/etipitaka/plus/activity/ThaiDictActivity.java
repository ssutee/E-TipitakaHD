package com.watnapp.etipitaka.plus.activity;

import android.graphics.Typeface;
import android.os.Build;
import com.google.inject.Inject;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.adapter.DictAdapter;
import com.watnapp.etipitaka.plus.adapter.ThaiDictAdapter;
import com.watnapp.etipitaka.plus.helper.DictDatabaseHelper;
import com.watnapp.etipitaka.plus.helper.ThaiDictDatabaseHelper;
import roboguice.inject.ContentView;

/**
 * Created by sutee on 20/3/58.
 */

@ContentView(R.layout.activity_dict)
public class ThaiDictActivity extends DictActivity {
  protected static final String TAG = "ThaiDictActivity";

  @Inject
  private ThaiDictDatabaseHelper mDatabaseHelper;

  @Inject
  private ThaiDictAdapter mAdapter;

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
    return "font-family:'TH SarabunPSK'";
  }

  @Override
  public String getFontFaces() {
    return getString(Build.VERSION.SDK_INT >= 15 ? R.string.font_family_new : R.string.font_family_old);
  }

  @Override
  public int getFontSize() {
    return 28;
  }

  @Override
  public Typeface getTypeface() {
    return Typeface.createFromAsset(getAssets(), "fonts/THSarabun.ttf");
  }
}