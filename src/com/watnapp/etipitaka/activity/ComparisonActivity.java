package com.watnapp.etipitaka.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.watnapp.etipitaka.Constants;
import com.watnapp.etipitaka.R;
import roboguice.inject.ContentView;
import roboguice.inject.InjectExtra;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 24/6/2013
 * Time: 22:20
 */

@ContentView(R.layout.activity_comparison)
public class ComparisonActivity extends RoboSherlockFragmentActivity {

  protected static final String TAG = "ComparisonActivity";

  @InjectExtra(Constants.LANGUAGE_KEY)
  private int mLanguage;

  @InjectExtra(Constants.VOLUME_KEY)
  private int mVolume;

  @InjectExtra(Constants.ITEM_KEY)
  private int mItem;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, mLanguage + " : " + mVolume + " : " + mItem);
  }
}