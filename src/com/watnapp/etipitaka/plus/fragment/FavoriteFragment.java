package com.watnapp.etipitaka.plus.fragment;

import android.app.Activity;
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
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.activity.MainActivity;
import com.watnapp.etipitaka.plus.adapter.FavoriteAdapter;
import com.watnapp.etipitaka.plus.model.DatabaseProvider;
import com.watnapp.etipitaka.plus.model.Favorite;
import com.watnapp.etipitaka.plus.model.FavoriteDaoHelper;
import com.watnapp.etipitaka.plus.model.FavoriteTable;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 9/7/2013
 * Time: 16:48
 */
public class FavoriteFragment extends RoboSherlockListFragment
    implements LoaderManager.LoaderCallbacks<Cursor>, TextEntryDialogFragment.TextEntryDialogButtonClickListener {

  private static final String TAG = "FavoriteFragment";

  @Inject
  private FavoriteAdapter mAdapter;

  @Inject
  private FavoriteDaoHelper mDaoHelper;

  private E_TipitakaApplication application;

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
            getLoaderManager().restartLoader(Constants.HISTORY_LOADER, null, FavoriteFragment.this);
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
    getLoaderManager().initLoader(Constants.FAVORITE_LOADER, null, this);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    setListAdapter(mAdapter);
    registerForContextMenu(getListView());
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_favorite, container, false);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    if (v.getId() == android.R.id.list) {
      menu.add(Menu.NONE, Constants.MENU_ITEM_OPEN, Menu.NONE, R.string.open_note);
      menu.add(Menu.NONE, Constants.MENU_ITEM_DELETE, Menu.NONE, R.string.delete);
    }
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    Cursor cursor = mAdapter.getCursor();
    cursor.moveToPosition(info.position);
    Favorite favorite = Favorite.newInstance(cursor, getActivity());
    switch (item.getItemId()) {
      case Constants.MENU_ITEM_OPEN:
        openNote(favorite);
        return true;
      case Constants.MENU_ITEM_DELETE:
        delete(favorite);
        return true;
    }
    return false;
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

  private void openNote(Favorite favorite) {
    String message = Utils.getSubtitle(getActivity(),favorite.getLanguage(),
        favorite.getVolume(), favorite.getPage(),
        Utils.convertToThaiNumber(getActivity(), favorite.getItem()));
    TextEntryDialogFragment.newInstance(0, message, Constants.OPEN_NOTE_ID, 5,
        TextEntryDialogFragment.InputMode.TEXT, favorite.getNote())
        .show(getChildFragmentManager(), "open_note_dialog");
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
    return new CursorLoader(getActivity(), DatabaseProvider.FAVORITE_CONTENT_URI, null,
        FavoriteTable.FavoriteColumns.LANGUAGE + " = ?",
        new String[] { application.getLanguage().getCode()+"" }, null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
    mAdapter.swapCursor(c);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    mAdapter.swapCursor(null);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    Cursor cursor = mAdapter.getCursor();
    cursor.moveToPosition(position);
    Favorite favorite = Favorite.newInstance(cursor, getActivity());
    MainActivity activity = (MainActivity) getActivity();
    activity.openBook(favorite.getLanguage(), favorite.getVolume(), favorite.getPage(), "", favorite.getItem());
  }

  @Override
  public void onTextEntryDialogPositiveButtonClick(String text, int id) {
    if (id == Constants.OPEN_NOTE_ID) {
      Log.d(TAG, text);
    }
  }

  @Override
  public void onTextEntryDialogNegativeButtonClick() {
  }
}
