package com.watnapp.etipitaka.plus.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
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
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.plus.model.*;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 12/6/2013
 * Time: 19:52
 */

public class HistoryFragment extends RoboSherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

  public static final String TAG = "HistoryFragment";
  private static final int FRAGMENT_GROUPID = 2;

  private E_TipitakaApplication application;

  private HistoryAdapter mAdapter;

  @Inject
  private HistoryDaoHelper mDaoHelper;

  private Handler mHandler = new Handler();

  private ContentObserver mContentObserver;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    application = (E_TipitakaApplication) activity.getApplication();
    mContentObserver = new ContentObserver(mHandler) {

      @Override
      public void onChange(boolean selfChange) {
        getListView().post(new Runnable() {
          @Override
          public void run() {
            getLoaderManager().restartLoader(Constants.HISTORY_LOADER, null, HistoryFragment.this);
          }
        });
      }
    };
    activity.getContentResolver()
        .registerContentObserver(Constants.LANGUAGE_CHANGE_URI, false, mContentObserver);
  }

  @Override
  public void onDetach() {
    if (getActivity() != null && mContentObserver != null) {
      getActivity().getContentResolver().unregisterContentObserver(mContentObserver);
    }
    super.onDetach();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getLoaderManager().initLoader(Constants.HISTORY_LOADER, null, this);
    mAdapter = new HistoryAdapter(getActivity()) {
      @Override
      public BookDatabaseHelper.Language getLanguage() {
        return application.getLanguage();
      }
    };
  }

  @Override
  public void onDestroy() {
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
      menu.add(FRAGMENT_GROUPID, Constants.MENU_ITEM_DELETE, Menu.NONE, R.string.delete);
      menu.add(FRAGMENT_GROUPID, Constants.MENU_ITEM_MARK, Menu.NONE, R.string.mark);
      menu.add(FRAGMENT_GROUPID, Constants.MENU_ITEM_SORT, Menu.NONE, R.string.sorting);
    }
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    if (item.getGroupId() == FRAGMENT_GROUPID) {
      AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
      Cursor cursor = mAdapter.getCursor();
      History history = History.newInstance(cursor, getActivity());
      switch (item.getItemId()) {
        case Constants.MENU_ITEM_DELETE:
          delete(history);
          return true;
        case Constants.MENU_ITEM_SORT:
          sort();
          return true;
        case Constants.MENU_ITEM_MARK:
          mark(history);
          return true;
      }
    }
    return super.onContextItemSelected(item);
  }

  private void mark(History history) {
    int score = history.getScore();
    history.setScore(score == 0 ? 1 : 0);
    mDaoHelper.update(history);
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
    int sortingType = getActivity().getPreferences(Context.MODE_PRIVATE).getInt(Constants.HIS_SORTING_KEY, 0);
    String orderBy = BaseColumns._ID;
    if (sortingType == 0) {
      orderBy = HistoryTable.HistoryColumns.KEYWORDS;
    } else if (sortingType == 1) {
      orderBy = BaseColumns._ID;
    } else if (sortingType == 2) {
      orderBy = HistoryTable.HistoryColumns.SCORE;
    }

    int orderingType = getActivity().getPreferences(Context.MODE_PRIVATE).getInt(Constants.HIS_ORDERING_KEY, 0);
    if (orderingType == 1) {
      orderBy += " DESC";
    }

    return new CursorLoader(getActivity(), DatabaseProvider.HISTORY_CONTENT_URI, null,
        HistoryTable.HistoryColumns.LANGUAGE + " = ?",
        new String[] { application.getLanguage().getCode()+"" }, orderBy);
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
      OnHistorySelectedListener listener = parentFragment;
      parentFragment.setCurrentTab(1);
      Cursor cursor = mAdapter.getCursor();
      cursor.moveToPosition(position);
      listener.onHistorySelected(History.newInstance(cursor, getActivity()));
    } catch (ClassCastException e) {
    }
  }

  private void order(int type) {
    SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt(Constants.HIS_ORDERING_KEY, type);
    editor.commit();
    getLoaderManager().restartLoader(Constants.HISTORY_LOADER, null, this);
  }

  private void order() {
    new AlertDialog.Builder(getActivity())
        .setTitle(R.string.select_ordering)
        .setSingleChoiceItems(R.array.ordering_types,
            getActivity().getPreferences(Context.MODE_PRIVATE).getInt(Constants.HIS_ORDERING_KEY, 0),
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                order(which);
                dialog.dismiss();
              }
            })
        .create().show();
  }

  private void sort(int type) {
    SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt(Constants.HIS_SORTING_KEY, type);
    editor.commit();
    order();
  }

  private void sort() {
    new AlertDialog.Builder(getActivity())
        .setTitle(R.string.select_sorting_type)
        .setSingleChoiceItems(R.array.history_sorting_types,
            getActivity().getPreferences(Context.MODE_PRIVATE).getInt(Constants.HIS_SORTING_KEY, 0),
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                sort(which);
                dialog.dismiss();
              }
            }).create().show();
  }

  public interface OnHistorySelectedListener {
    public void onHistorySelected(History history);
  }
}
