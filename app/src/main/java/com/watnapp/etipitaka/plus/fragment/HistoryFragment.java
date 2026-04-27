package com.watnapp.etipitaka.plus.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.ETipitakaApplication;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.adapter.HistoryAdapter;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.plus.model.*;
import com.watnapp.etipitaka.plus.vm.SharedViewModel;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 12/6/2013
 * Time: 19:52
 */

public class HistoryFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

  public static final String TAG = "HistoryFragment";
  private static final int FRAGMENT_GROUPID = 2;

  private ETipitakaApplication application;
  private HistoryAdapter mAdapter;
  private HistoryDaoHelper mDaoHelper;
  private SharedViewModel viewModel;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    Activity activity = (Activity)context;
    application = (ETipitakaApplication) activity.getApplication();
  }

  @Override
  public void onDetach() {
    super.onDetach();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    LoaderManager.getInstance(this)
            .initLoader(Constants.HISTORY_LOADER, null, this);
    mDaoHelper = new HistoryDaoHelper(getContext());
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
    viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    viewModel.getSelected().observe(getViewLifecycleOwner(), language -> {
      getListView().post(() ->
              LoaderManager.getInstance(HistoryFragment.this)
                      .restartLoader(Constants.HISTORY_LOADER, null, HistoryFragment.this));
    });
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
        .setPositiveButton(R.string.delete, (dialog, which) -> mDaoHelper.delete(history))
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
  public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
    mAdapter.swapCursor(data);
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
    } catch (ClassCastException ignored) {
    }
  }

  private void order(int type) {
    SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt(Constants.HIS_ORDERING_KEY, type);
    editor.commit();
    LoaderManager.getInstance(this).restartLoader(Constants.HISTORY_LOADER, null, this);
  }

  private void order() {
    new AlertDialog.Builder(getActivity())
        .setTitle(R.string.select_ordering)
        .setSingleChoiceItems(R.array.ordering_types,
            getActivity().getPreferences(Context.MODE_PRIVATE).getInt(Constants.HIS_ORDERING_KEY, 0),
                (dialog, which) -> {
                  order(which);
                  dialog.dismiss();
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
                (dialog, which) -> {
                  sort(which);
                  dialog.dismiss();
                }).create().show();
  }

  public interface OnHistorySelectedListener {
    public void onHistorySelected(History history);
  }
}
