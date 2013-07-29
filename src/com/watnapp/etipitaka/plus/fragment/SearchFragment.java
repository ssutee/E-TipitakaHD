package com.watnapp.etipitaka.plus.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;
import com.touchsi.widget.ClearableAutoCompleteTextView;
import com.watnapp.etipitaka.plus.E_TipitakaApplication;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.activity.MainActivity;
import com.watnapp.etipitaka.plus.adapter.SearchResultAdapter;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.plus.model.*;
import roboguice.inject.InjectView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 30/5/2013
 * Time: 10:38
 */

public class SearchFragment extends RoboSherlockFragment implements BookDatabaseHelper.OnSearchListener,
    AdapterView.OnItemClickListener {

  private static final String TAG = "SearchFragment";

  @InjectView(R.id.search_input)
  private ClearableAutoCompleteTextView mSearchInput;

  @InjectView(R.id.btn_search)
  private ImageView mButtonSearch;

  @InjectView(android.R.id.list)
  private StickyListHeadersListView mListView;

  @Inject
  private BookDatabaseHelper mDatabaseHelper;

  @Inject
  private InputMethodManager mInputMethodManager;

  @Inject
  private HistoryDaoHelper mHistoryDaoHelper;

  @Inject
  private HistoryItemDaoHelper mHistoryItemDaoHelper;

  @Inject
  private Handler mHandler;

  private E_TipitakaApplication application;
  private ProgressDialog mProgressDialog;
  private Integer[] mSelectedVolumes;
  private SearchResultAdapter mAdapter;
  private int[] mResultsCount;
  private String mKeywords;
  private SparseBooleanArray mCheckedCategories;
  private History mCurrentHistory;
  private List<HistoryItem> mCurrentHistoryItems;

  private ContentObserver mContentObserver;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    application = (E_TipitakaApplication) activity.getApplication();
    mContentObserver = new ContentObserver(mHandler) {
      @Override
      public void onChange(boolean selfChange) {
        mListView.post(new Runnable() {
          @Override
          public void run() {
            mAdapter.notifyDataSetChanged();
          }
        });
      }
    };
    activity.getContentResolver()
        .registerContentObserver(DatabaseProvider.HISTORY_ITEM_CONTENT_URI, false, mContentObserver);

    mProgressDialog = new ProgressDialog(activity);
    mProgressDialog.setCancelable(false);
    mProgressDialog.setTitle(R.string.searching);
    mProgressDialog.setMessage(getString(R.string.please_wait));
    mProgressDialog.setIndeterminate(false);
    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

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
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_search, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mSearchInput.setClearDrawable(R.drawable.ic_clear_holo_light);
    mSearchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        showBookCategorySelectionDialog();
        return true;
      }
    });

    mButtonSearch.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mSearchInput.getText().length() > 0) {
          showBookCategorySelectionDialog();
        } else {
          mInputMethodManager.showSoftInput(mSearchInput, InputMethodManager.SHOW_FORCED);
          Toast.makeText(getActivity(), R.string.please_enter_keywords, Toast.LENGTH_SHORT).show();
        }
      }
    });
    mAdapter = new SearchResultAdapter(getActivity(), null) {
      @Override
      public String getKeywords() {
        return mKeywords;
      }

      @Override
      public int[] getResultsCount() {
        return mResultsCount;
      }

      @Override
      public BookDatabaseHelper.Language getLanguage() {
        return application.getLanguage();
      }

      @Override
      public HistoryItem.Status getStatus(int volume, int page) {
        HistoryItem item = mHistoryItemDaoHelper.get(mCurrentHistory.getId(), volume, page);
        return item != null ? item.getStatus() : HistoryItem.Status.NONE;
      }

    };
    mListView.setAdapter(mAdapter);
    mListView.setOnItemClickListener(this);
  }

  private void showBookCategorySelectionDialog() {
    AlertDialog dialog = new AlertDialog.Builder(getActivity())
        .setMultiChoiceItems(R.array.sections,
            new boolean[]{true, true, true},
            new DialogInterface.OnMultiChoiceClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which, boolean isChecked) {

              }
            })
        .setTitle(R.string.please_select_category)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            mCheckedCategories = ((AlertDialog) dialog).getListView().getCheckedItemPositions();
            ArrayList<Integer> volumes = new ArrayList<Integer>();
            if (mCheckedCategories.get(0, false)) {
              for (int volume = 1; volume < 9; volume++) {
                volumes.add(volume);
              }
            }
            if (mCheckedCategories.get(1, false)) {
              for (int volume = 9; volume < 34; volume++) {
                volumes.add(volume);
              }
            }
            if (mCheckedCategories.get(2, false)) {
              for (int volume = 34; volume < 46; volume++) {
                volumes.add(volume);
              }
            }
            if (volumes.size() > 0) {
              mSelectedVolumes = volumes.toArray(new Integer[volumes.size()]);
            }
          }
        })
        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            mSelectedVolumes = null;
          }
        })
        .create();
    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
      @Override
      public void onDismiss(DialogInterface dialog) {
        if (mSelectedVolumes != null && mSelectedVolumes.length > 0) {
          search(mSelectedVolumes);
        }
      }
    });
    dialog.show();
  }

  private boolean search(Integer[] volumes) {
    if (mSearchInput.getText().length() > 0) {
      mResultsCount = new int[] {0, 0, 0};
      mDatabaseHelper.search(application.getLanguage(), mSearchInput.getText().toString(), this, volumes);
      mProgressDialog.setMax(volumes.length);
      mProgressDialog.setProgress(0);
      mProgressDialog.show();
      return true;
    }
    return false;
  }

  @Override
  public void onSearchProgress(String keywords, int volume, int progress, Cursor cursor) {
    mProgressDialog.setProgress(progress);
    if (volume <= 8) {
      mResultsCount[0] += cursor.getCount();
    } else if (volume >= 9 && volume <= 33) {
      mResultsCount[1] += cursor.getCount();
    } else {
      mResultsCount[2] += cursor.getCount();
    }
  }

  @Override
  public void onSearchFinish(String keywords, final Cursor cursor, int[] totalPages) {
    mKeywords = mSearchInput.getText().toString();

    if (!mHistoryDaoHelper.contains(keywords, application.getLanguage(), mCheckedCategories)) {
      History history = new History();
      history.setKeywords(keywords);
      history.setLanguage(application.getLanguage());
      history.setSections(mCheckedCategories);
      history.setResults(totalPages);
      StringBuilder sb = new StringBuilder();
      if (cursor.getCount() > 3) {
        cursor.moveToPosition(3);
        while (!cursor.isAfterLast()) {
          sb.append(cursor.getInt(cursor.getColumnIndex("volume")));
          sb.append(':');
          sb.append(cursor.getInt(cursor.getColumnIndex("number")));
          if (!cursor.isLast()) {
            sb.append(',');
          }
          cursor.moveToNext();
        }
      }
      Log.d(TAG, sb.toString());
      history.setContent(sb.toString());
      mHistoryDaoHelper.insert(history);
    }

    mCurrentHistory = mHistoryDaoHelper.get(keywords, application.getLanguage(), mCheckedCategories);
    mCurrentHistoryItems = mHistoryItemDaoHelper.getByHistoryId(mCurrentHistory.getId());

    mProgressDialog.dismiss();
    mInputMethodManager.hideSoftInputFromWindow(mSearchInput.getWindowToken(), 0);
    mListView.post(new Runnable() {
      @Override
      public void run() {
        cursor.moveToFirst();
        mAdapter.swapCursor(cursor);
      }
    });

  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    if (position <= 2) {
      scrollToSection(position+1);
    } else {
      application.setHistory(mCurrentHistory);
      Cursor cursor = mAdapter.getCursor();
      cursor.moveToPosition(position);
      int volume = cursor.getInt(cursor.getColumnIndex("volume"));
      int page = cursor.getInt(cursor.getColumnIndex("number"));
      MainActivity activity = (MainActivity) getActivity();
      activity.openBook(application.getLanguage(), volume, page, mKeywords);
      mHistoryItemDaoHelper.insertOrUpdate(mCurrentHistory.getId(), volume, page, HistoryItem.Status.READ);
    }
  }

  private void scrollToSection(int section) {
    if (section == 1 && mResultsCount[0] > 0) {
      mListView.setSelectionFromTop(3, 0);
    } else if (section == 2 && mResultsCount[1] > 0) {
      mListView.setSelectionFromTop(mResultsCount[0]+3, 0);
    } else if (section == 3 && mResultsCount[2] > 0) {
      mListView.setSelectionFromTop(mResultsCount[0]+mResultsCount[1]+3, 0);
    }
  }

  private Cursor createCursorFromHistory(History history) {
    MatrixCursor itemCursor = new MatrixCursor(new String[] { "_id", "volume", "number" });
    int id = 4;
    mResultsCount = new int[] { 0,0,0 };
    if (history.getContent().length() > 0) {
      for (String item : history.getContent().split(",")) {
        String[] tokens = item.split(":");
        int volume = Integer.parseInt(tokens[0]);
        int page = Integer.parseInt(tokens[1]);
        itemCursor.addRow(new Object[] { id, volume, page });
        if (volume <= 8) {
          mResultsCount[0] += 1;
        } else if (volume >=9 && volume <= 33) {
          mResultsCount[1] += 1;
        } else {
          mResultsCount[2] += 1;
        }
        id += 1;
      }
    }

    MatrixCursor headCursor = new MatrixCursor(new String[] { "_id" , "total"});
    headCursor.addRow(new Object[] { 1, mResultsCount[0]});
    headCursor.addRow(new Object[] { 2, mResultsCount[1]});
    headCursor.addRow(new Object[] { 3, mResultsCount[2]});

    return new MergeCursor(new Cursor[] {headCursor, itemCursor});
  }

  public void loadHistory(final History history) {
    mCurrentHistory = history;
    mSearchInput.setText(history.getKeywords());
    mKeywords = history.getKeywords();
    new Thread(new Runnable() {
      @Override
      public void run() {
        final Cursor cursor = createCursorFromHistory(history);
        mHandler.post(new Runnable() {
          @Override
          public void run() {
            mAdapter.swapCursor(cursor);
          }
        });
      }
    }).start();
  }
}
