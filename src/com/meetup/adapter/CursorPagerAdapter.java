package com.meetup.adapter;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import java.util.HashMap;

public abstract class CursorPagerAdapter<F extends Fragment> extends FragmentStatePagerAdapter {
  private static final String TAG = "CursorPagerAdapter";
  private final Class<F> fragmentClass;
  private final HashMap<Integer, Fragment> mHash = new HashMap<Integer, Fragment>();
  private Cursor cursor;

  abstract public Bundle buildArguments(Cursor cursor);

  public CursorPagerAdapter(FragmentManager fm, Class<F> fragmentClass, Cursor cursor) {
    super(fm);
    this.fragmentClass = fragmentClass;
    this.cursor = cursor;
  }

  @Override
  public F getItem(int position) {
    if (cursor == null) // shouldn't happen
      return null;

    cursor.moveToPosition(position);
    F frag;
    try {
      frag = fragmentClass.newInstance();
      Log.d(TAG, "save fragment at " + position);
      mHash.put(position, frag);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }

    frag.setArguments(buildArguments(cursor));

    return frag;
  }

  @Override
  public int getCount() {
    if (cursor == null)
      return 0;
    else
      return cursor.getCount();
  }

  @Override
  public int getItemPosition(Object object) {
    return POSITION_NONE;
  }

  public void swapCursor(Cursor c) {
    if (cursor == c)
      return;

    if (cursor != null)
      cursor.close();

    cursor = c;
    notifyDataSetChanged();
  }

  public Cursor getCursor() {
    return cursor;
  }

  public Fragment getFragment(int position) {
    Log.d(TAG, "position = " + position);
    return mHash.get(position);
  }
}
