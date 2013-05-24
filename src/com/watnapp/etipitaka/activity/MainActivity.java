package com.watnapp.etipitaka.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.meetup.adapter.CursorPagerAdapter;
import com.watnapp.etipitaka.fragment.MenuFragment;
import com.watnapp.etipitaka.fragment.PageFragment;
import com.watnapp.etipitaka.helper.DatabaseHelper;
import com.watnapp.etipitaka.R;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 22/5/2013
 * Time: 8:03
 */

@ContentView(R.layout.activity_main)
public class MainActivity extends RoboSherlockFragmentActivity implements DatabaseHelper.OnSearchListener {

  protected static final String TAG = "MainActivity";

  @InjectView(R.id.viewpager)
  private ViewPager mViewPager;

  @Inject
  private DatabaseHelper mDatabaseHelper;

  private CursorPagerAdapter<PageFragment> mPagerAdapter;
  private SlidingMenu mSlidingMenu;
  private MenuFragment mMenuFragment;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    mSlidingMenu = new SlidingMenu(this);
    mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
    mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
    mSlidingMenu.setShadowDrawable(R.drawable.shadow);
    mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
    mSlidingMenu.setFadeDegree(0.35f);
    mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
    mSlidingMenu.setMenu(R.layout.menu_frame);
    mMenuFragment = new MenuFragment();

    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.menu_frame, mMenuFragment)
        .commit();

    mDatabaseHelper.openDatabase();

    mPagerAdapter = new CursorPagerAdapter<PageFragment>(getSupportFragmentManager(),
        PageFragment.class, null) {
      @Override
      public Bundle buildArguments(Cursor cursor) {
        Bundle args = new Bundle();
        args.putString("content", cursor.getString(cursor.getColumnIndex("content")));
        args.putInt("number", cursor.getInt(cursor.getColumnIndex("number")));
        return args;
      }
    };
    mViewPager.setAdapter(mPagerAdapter);
    Cursor cursor = mDatabaseHelper.read(DatabaseHelper.Language.THAI, 1);
    cursor.moveToFirst();
    Log.d(TAG, cursor.getCount() + "");
    Log.d(TAG, cursor.getString(cursor.getColumnIndex("content")));
    mPagerAdapter.swapCursor(cursor);
//    mDatabaseHelper.search(DatabaseHelper.Language.THAI, "ดูกร อานนท์", this, new int[] { 1, 12, 24 });
  }

  @Override
  protected void onDestroy() {
    mDatabaseHelper.closeDatabase();
    super.onDestroy();
  }

  @Override
  public void onSearchProgress(String keywords, int volume, float progress, Cursor cursor) {
    Log.d(TAG, String.valueOf(progress) + ":" + cursor.getCount());
  }

  @Override
  public void onSearchFinish(String keywords, Cursor cursor) {
    Log.d(TAG, String.valueOf(cursor.getCount()));
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        mMenuFragment.setCurrentTab(1);
        mSlidingMenu.showMenu();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed() {
    if (mSlidingMenu.isMenuShowing()) {
      mSlidingMenu.showContent();
    } else {
      super.onBackPressed();
    }
  }

}