package com.meetup.adapter;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.HashMap;

public abstract class CursorPagerAdapter<F extends Fragment> extends FragmentStateAdapter {
  private static final String TAG = "CursorPagerAdapter";
  private final Class<F> fragmentClass;
  private final HashMap<Integer, Fragment> mHash = new HashMap<Integer, Fragment>();
  private Cursor cursor;

  abstract public Bundle buildArguments(Cursor cursor);

  public CursorPagerAdapter(FragmentManager fm, Lifecycle lifecycle, Class<F> fragmentClass, Cursor cursor) {
    super(fm, lifecycle);
    this.fragmentClass = fragmentClass;
    this.cursor = cursor;
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
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
  public long getItemId(int position) {
    return super.getItemId(position);
  }

  @Override
  public int getItemCount() {
    if (cursor == null)
      return 0;
    else
      return cursor.getCount();
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
