package com.watnapp.etipitaka.plus.activity;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.adapter.DictAdapter;
import com.watnapp.etipitaka.plus.adapter.PaliDictAdapter;
import com.watnapp.etipitaka.plus.helper.DictDatabaseHelper;
import com.watnapp.etipitaka.plus.helper.PaliDictDatabaseHelper;
import static org.koin.java.KoinJavaComponent.*;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 15/7/2013
 * Time: 14:05
 */

public class PaliDictActivity extends DictActivity {
  protected static final String TAG = "PaliDictActivity";

  private PaliDictDatabaseHelper mDatabaseHelper = get(PaliDictDatabaseHelper.class);
  private PaliDictAdapter mAdapter = get(PaliDictAdapter.class);

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