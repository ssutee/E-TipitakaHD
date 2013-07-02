package com.watnapp.etipitaka.fragment;

import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockListFragment;
import com.google.inject.Inject;
import com.watnapp.etipitaka.Constants;
import com.watnapp.etipitaka.E_TipitakaApplication;
import com.watnapp.etipitaka.R;
import com.watnapp.etipitaka.adapter.HistoryAdapter;
import com.watnapp.etipitaka.model.DatabaseProvider;
import com.watnapp.etipitaka.model.History;
import com.watnapp.etipitaka.model.HistoryTable;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 12/6/2013
 * Time: 19:52
 */

public class HistoryFragment extends RoboSherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

  public static final String TAG = "HistoryFragment";

  private E_TipitakaApplication application;

  @Inject
  private HistoryAdapter mAdapter;

  private Handler mHandler = new Handler();

  private ContentObserver mContentObserver = new ContentObserver(mHandler) {

    @Override
    public void onChange(boolean selfChange) {
      getListView().post(new Runnable() {
        @Override
        public void run() {
          Log.d(TAG, "restart");
          getLoaderManager().restartLoader(Constants.HISTORY_LOADER, null, HistoryFragment.this);
        }
      });
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    application = (E_TipitakaApplication) getActivity().getApplication();
    getLoaderManager().initLoader(Constants.HISTORY_LOADER, null, this);
    getActivity().getContentResolver()
        .registerContentObserver(Constants.LANGUAGE_CHANGE_URI, false, mContentObserver);
  }

  @Override
  public void onDestroy() {
    getActivity().getContentResolver().unregisterContentObserver(mContentObserver);
    super.onDestroy();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_history, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    setListAdapter(mAdapter);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
    Log.d(TAG, "language = " + application.getLanguage());
    return new CursorLoader(getActivity(), DatabaseProvider.HISTORY_CONTENT_URI, null,
        HistoryTable.HistoryColumns.LANGUAGE + " = ?",
        new String[] { application.getLanguage().getCode()+"" },
        HistoryTable.HistoryColumns.KEYWORDS);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
    mAdapter.swapCursor(cursor);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> cursorLoader) {
    mAdapter.swapCursor(null);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    MenuFragment parentFragment = (MenuFragment) getParentFragment();
    try {
      OnHistorySelectedListener listener = (OnHistorySelectedListener)parentFragment;
      parentFragment.setCurrentTab(1);
      Cursor cursor = mAdapter.getCursor();
      cursor.moveToPosition(position);
      listener.onHistorySelected(History.newInstance(cursor, getActivity()));
    } catch (ClassCastException e) {
    }
  }

  public interface OnHistorySelectedListener {
    public void onHistorySelected(History history);
  }
}
