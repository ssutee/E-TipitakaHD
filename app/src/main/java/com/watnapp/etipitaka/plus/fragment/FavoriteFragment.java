package com.watnapp.etipitaka.plus.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
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
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.activity.MainActivity;
import com.watnapp.etipitaka.plus.model.DatabaseProvider;
import com.watnapp.etipitaka.plus.model.Favorite;
import com.watnapp.etipitaka.plus.model.FavoriteDaoHelper;
import com.watnapp.etipitaka.plus.model.FavoriteTable;
import com.watnapp.etipitaka.plus.vm.SharedViewModel;

import java.util.ArrayList;
import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 9/7/2013
 * Time: 16:48
 */
public class FavoriteFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor>, TextEntryDialogFragment.TextEntryDialogButtonClickListener {

  private static final String TAG = "FavoriteFragment";

  private FavoriteDaoHelper mDaoHelper;
  private ETipitakaApplication application;
  private Favorite selectedFavorite;
  private SharedViewModel viewModel;
  private ComposeView composeView;
  private List<Favorite> favorites = new ArrayList<>();

  @Override
  public void onAttach(@NonNull Context context) {
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
    mDaoHelper = new FavoriteDaoHelper(getContext());

    LoaderManager.getInstance(FavoriteFragment.this)
            .initLoader(Constants.FAVORITE_LOADER, null, this);
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
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    viewModel.getSelected().observe(getViewLifecycleOwner(), language ->  {
      composeView.post(() -> LoaderManager.getInstance(FavoriteFragment.this)
          .restartLoader(Constants.FAVORITE_LOADER, null, FavoriteFragment.this));
    });
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    composeView = new ComposeView(requireContext());
    renderFavoriteScreen();
    return composeView;
  }

  private void mark(Favorite favorite) {
    int score = favorite.getScore();
    if (score == 0) {
      favorite.setScore(1);
    } else {
      favorite.setScore(0);
    }
    mDaoHelper.update(favorite);
  }

  private void order(int type) {
    SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt(Constants.FAV_ORDERING_KEY, type);
    editor.apply();
    LoaderManager.getInstance(FavoriteFragment.this)
            .restartLoader(Constants.FAVORITE_LOADER, null, this);
  }

  private void order() {
    new AlertDialog.Builder(getActivity())
        .setTitle(R.string.select_ordering)
        .setSingleChoiceItems(R.array.ordering_types,
            getActivity().getPreferences(Context.MODE_PRIVATE).getInt(Constants.FAV_ORDERING_KEY, 0),
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
    editor.putInt(Constants.FAV_SORTING_KEY, type);
    editor.apply();
    order();
  }

  private void sort() {
    new AlertDialog.Builder(getActivity())
        .setTitle(R.string.select_sorting_type)
        .setSingleChoiceItems(R.array.favorite_sorting_types,
            getActivity().getPreferences(Context.MODE_PRIVATE).getInt(Constants.FAV_SORTING_KEY, 0),
            new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            sort(which);
            dialog.dismiss();
          }
        }).create().show();
  }

  private void delete(final Favorite favorite) {
    new AlertDialog.Builder(getActivity())
        .setTitle(R.string.confirm_delete_favorite)
        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            mDaoHelper.delete(favorite);
          }
        })
        .setNegativeButton(R.string.cancel, null)
        .create().show();
  }

  private void editNote(Favorite favorite) {
    String message = Utils.getSubtitle(getActivity(),favorite.getLanguage(),
        favorite.getVolume(), favorite.getPage(),
        favorite.getItem() != 0 ? Utils.convertToThaiNumber(getActivity(), favorite.getItem()) : "");
    TextEntryDialogFragment.newInstance(0, message, Constants.EDIT_NOTE_ID, 5,
        TextEntryDialogFragment.InputMode.TEXT, favorite.getNote())
        .show(getChildFragmentManager(), "open_note_dialog");
  }

  private void openNote(Favorite favorite) {
    MainActivity activity = (MainActivity) getActivity();
    Log.d(TAG, favorite.getVolume() + ":" + favorite.getPage());
    activity.openBook(favorite.getLanguage(), favorite.getVolume(), favorite.getPage(), "", false, favorite.getItem());
  }

  @NonNull
  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
    int sortingType = getActivity().getPreferences(Context.MODE_PRIVATE).getInt(Constants.FAV_SORTING_KEY, 0);
    String orderBy = BaseColumns._ID;
    if (sortingType == 0) {
      orderBy = FavoriteTable.FavoriteColumns.VOLUME;
    } else if (sortingType == 1) {
      orderBy = FavoriteTable.FavoriteColumns.NOTE;
    } else if (sortingType == 2) {
      orderBy = BaseColumns._ID;
    } else if (sortingType == 3) {
      orderBy = FavoriteTable.FavoriteColumns.SCORE;
    }

    int orderingType = getActivity().getPreferences(Context.MODE_PRIVATE).getInt(Constants.FAV_ORDERING_KEY, 0);
    if (orderingType == 1) {
      orderBy += " DESC";
    }

    return new CursorLoader(getActivity(), DatabaseProvider.FAVORITE_CONTENT_URI, null,
        FavoriteTable.FavoriteColumns.LANGUAGE + " = ?",
        new String[] { application.getLanguage().getCode()+"" }, orderBy);
  }

  @Override
  public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
    favorites = createFavorites(data);
    renderFavoriteScreen();
  }

  @Override
  public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    favorites = new ArrayList<>();
    renderFavoriteScreen();
  }

  @Override
  public void onTextEntryDialogPositiveButtonClick(String text, int id) {
    if (id == Constants.EDIT_NOTE_ID) {
      selectedFavorite.setNote(text);
      mDaoHelper.update(selectedFavorite);
      LoaderManager.getInstance(FavoriteFragment.this)
              .restartLoader(Constants.FAVORITE_LOADER, null, this);
    }
  }

  @Override
  public void onTextEntryDialogNegativeButtonClick() {
  }

  private List<Favorite> createFavorites(Cursor cursor) {
    List<Favorite> result = new ArrayList<>();
    if (cursor == null || !cursor.moveToFirst()) {
      return result;
    }
    do {
      result.add(Favorite.newInstance(cursor, requireContext()));
    } while (cursor.moveToNext());
    return result;
  }

  private void renderFavoriteScreen() {
    if (composeView == null) {
      return;
    }
    FavoriteHistoryScreenBridge.renderFavorites(
        composeView,
        favorites,
        this::openNote,
        this::editNote,
        this::delete,
        this::mark,
        this::sort);
  }
}
