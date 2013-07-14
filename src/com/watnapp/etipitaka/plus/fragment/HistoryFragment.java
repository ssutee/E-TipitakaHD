package com.watnapp.etipitaka.plus.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockListFragment;
import com.google.inject.Inject;
import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.E_TipitakaApplication;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.adapter.HistoryAdapter;
import com.watnapp.etipitaka.plus.model.DatabaseProvider;
import com.watnapp.etipitaka.plus.model.History;
import com.watnapp.etipitaka.plus.model.HistoryDaoHelper;
import com.watnapp.etipitaka.plus.model.HistoryTable;

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

  @Inject
  private HistoryDaoHelper mDaoHelper;

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
    registerForContextMenu(getListView());
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    if (v.getId() == android.R.id.list) {
      menu.add(Menu.NONE, Constants.MENU_ITEM_DELETE, Menu.NONE, R.string.delete);
    }
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    Cursor cursor = mAdapter.getCursor();
    History history = History.newInstance(cursor, getActivity());
    switch (item.getItemId()) {
      case Constants.MENU_ITEM_DELETE:
        delete(history);
        return true;
    }
    return false;
  }

  private void delete(final History history) {
    new AlertDialog.Builder(getActivity())
        .setTitle(R.string.confirm_delete_history)
        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            mDaoHelper.delete(history);
          }
        })
        .setNegativeButton(R.string.cancel, null)
        .create().show();
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
