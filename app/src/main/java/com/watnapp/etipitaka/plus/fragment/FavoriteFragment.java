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
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.activity.MainActivity;
import com.watnapp.etipitaka.plus.adapter.FavoriteAdapter;
import com.watnapp.etipitaka.plus.model.DatabaseProvider;
import com.watnapp.etipitaka.plus.model.Favorite;
import com.watnapp.etipitaka.plus.model.FavoriteDaoHelper;
import com.watnapp.etipitaka.plus.model.FavoriteTable;
import com.watnapp.etipitaka.plus.vm.SharedViewModel;


/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 9/7/2013
 * Time: 16:48
 */
public class FavoriteFragment extends ListFragment
    implements LoaderManager.LoaderCallbacks<Cursor>, TextEntryDialogFragment.TextEntryDialogButtonClickListener {

  private static final String TAG = "FavoriteFragment";
  private static final int FRAGMENT_GROUPID = 1;

  private FavoriteAdapter mAdapter;
  private FavoriteDaoHelper mDaoHelper;
  private ETipitakaApplication application;
  private Favorite selectedFavorite;
  private SharedViewModel viewModel;

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
    mAdapter = new FavoriteAdapter(getContext());
    mDaoHelper = new FavoriteDaoHelper(getContext());

    LoaderManager.getInstance(FavoriteFragment.this)
            .initLoader(Constants.FAVORITE_LOADER, null, this);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    setListAdapter(mAdapter);
    registerForContextMenu(getListView());
    viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    viewModel.getSelected().observe(getViewLifecycleOwner(), language ->  {
      getListView().post(() -> LoaderManager.getInstance(FavoriteFragment.this)
              .restartLoader(Constants.FAVORITE_LOADER, null, FavoriteFragment.this));
    });
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_favorite, container, false);
  }

  @Override
  public void onCreateContextMenu(@NonNull ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    if (v.getId() == android.R.id.list) {
      menu.add(FRAGMENT_GROUPID, Constants.MENU_ITEM_OPEN, Menu.NONE, R.string.open_note);
      menu.add(FRAGMENT_GROUPID, Constants.MENU_ITEM_EDIT, Menu.NONE, R.string.edit_note);
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
      cursor.moveToPosition(info.position);
      selectedFavorite = Favorite.newInstance(cursor, getActivity());
      switch (item.getItemId()) {
        case Constants.MENU_ITEM_OPEN:
          openNote(selectedFavorite);
          return true;
        case Constants.MENU_ITEM_EDIT:
          editNote(selectedFavorite);
          return true;
        case Constants.MENU_ITEM_DELETE:
          delete(selectedFavorite);
          return true;
        case Constants.MENU_ITEM_MARK:
          mark(selectedFavorite);
          return true;
        case Constants.MENU_ITEM_SORT:
          sort();
          return true;
      }
    }
    return super.onContextItemSelected(item);
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
    mAdapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    mAdapter.swapCursor(null);
  }

  @Override
  public void onListItemClick(ListView l, @NonNull View v, int position, long id) {
    Cursor cursor = mAdapter.getCursor();
    cursor.moveToPosition(position);
    Favorite favorite = Favorite.newInstance(cursor, getActivity());
    openNote(favorite);
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
}
