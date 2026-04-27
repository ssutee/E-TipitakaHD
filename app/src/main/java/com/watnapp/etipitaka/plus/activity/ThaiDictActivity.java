package com.watnapp.etipitaka.plus.activity;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.adapter.DictAdapter;
import com.watnapp.etipitaka.plus.adapter.ThaiDictAdapter;
import com.watnapp.etipitaka.plus.helper.DictDatabaseHelper;
import com.watnapp.etipitaka.plus.helper.ThaiDictDatabaseHelper;
import static org.koin.java.KoinJavaComponent.*;

/**
 * Created by sutee on 20/3/58.
 */

public class ThaiDictActivity extends DictActivity {
  protected static final String TAG = "ThaiDictActivity";

  private ThaiDictDatabaseHelper mDatabaseHelper = get(ThaiDictDatabaseHelper.class);
  private ThaiDictAdapter mAdapter = get(ThaiDictAdapter.class);

  @Override
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