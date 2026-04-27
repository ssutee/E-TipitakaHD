package com.watnapp.etipitaka.plus.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.compose.ui.platform.ComposeView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.ETipitakaApplication;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.plus.model.*;
import com.watnapp.etipitaka.plus.vm.SharedViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 12/6/2013
 * Time: 19:52
 */

public class HistoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

  public static final String TAG = "HistoryFragment";

  private ETipitakaApplication application;
  private HistoryDaoHelper mDaoHelper;
  private SharedViewModel viewModel;
  private ComposeView composeView;
  private List<History> histories = new ArrayList<>();

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
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    composeView = null;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    composeView = new ComposeView(requireContext());
    renderHistoryScreen();
    return composeView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    viewModel.getSelected().observe(getViewLifecycleOwner(), language -> {
      composeView.post(() -> LoaderManager.getInstance(HistoryFragment.this)
          .restartLoader(Constants.HISTORY_LOADER, null, HistoryFragment.this));
    });
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
    histories = createHistories(data);
    renderHistoryScreen();
  }

  @Override
  public void onLoaderReset(Loader<Cursor> cursorLoader) {
    histories = new ArrayList<>();
    renderHistoryScreen();
  }

  private void openHistory(History history) {
    MenuFragment parentFragment = (MenuFragment) getParentFragment();
    try {
      OnHistorySelectedListener listener = parentFragment;
      parentFragment.setCurrentTab(1);
      listener.onHistorySelected(history);
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

  private List<History> createHistories(Cursor cursor) {
    List<History> result = new ArrayList<>();
    if (cursor == null || !cursor.moveToFirst()) {
      return result;
    }
    do {
      result.add(History.newInstance(cursor, requireContext()));
    } while (cursor.moveToNext());
    return result;
  }

  private void renderHistoryScreen() {
    if (composeView == null) {
      return;
    }
    FavoriteHistoryScreenBridge.renderHistory(
        composeView,
        histories,
        application.getLanguage(),
        this::openHistory,
        this::delete,
        this::mark,
        this::sort);
  }
}
