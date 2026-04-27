package com.watnapp.etipitaka.plus.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.compose.ui.platform.ComposeView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.ETipitakaApplication;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.activity.MainActivity;
import com.watnapp.etipitaka.plus.adapter.SearchResultAdapter;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.plus.model.*;
import com.watnapp.etipitaka.plus.vm.SharedViewModel;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static org.koin.java.KoinJavaComponent.*;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 30/5/2013
 * Time: 10:38
 */

public class SearchFragment extends Fragment implements BookDatabaseHelper.OnSearchListener,
    AdapterView.OnItemClickListener {

  private static final String TAG = "SearchFragment";

  private InputMethodManager mInputMethodManager;
  private HistoryDaoHelper mHistoryDaoHelper;
  private HistoryItemDaoHelper mHistoryItemDaoHelper;
  private Handler mHandler = new Handler();

  private ETipitakaApplication application;
  private AlertDialog mProgressDialog;
  private ProgressBar mProgressBar;
  private Integer[] mSelectedVolumes;
  private SearchResultAdapter mAdapter;
  private int[] mResultsCount;
  private String mKeywords;
  private SparseBooleanArray mCheckedCategories;
  private History mCurrentHistory;
  private List<HistoryItem> mCurrentHistoryItems;
  private ETDataModel dataModel;

  private ContentObserver mContentObserver;
  private boolean mIsBuddhawaj;
  private ComposeView composeView;
  private StickyListHeadersListView listView;
  private String mSearchInputText = "";
  private SharedViewModel viewModel;

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    final Activity activity = (Activity) context;
    application = (ETipitakaApplication) activity.getApplication();
    dataModel = ETDataModelCreator.create(application.getLanguage(), activity);

    mContentObserver = new ContentObserver(mHandler) {
      @Override
      public void onChange(boolean selfChange, Uri uri) {
        if (uri.compareTo(DatabaseProvider.HISTORY_ITEM_CONTENT_URI) == 0) {
          if (listView != null && mAdapter != null) {
            listView.post(() -> mAdapter.notifyDataSetChanged());
          }
        }
      }
    };

    activity.getContentResolver()
        .registerContentObserver(DatabaseProvider.HISTORY_ITEM_CONTENT_URI, false, mContentObserver);

    createProgressDialog(activity);
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
    mInputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    mHistoryDaoHelper = new HistoryDaoHelper(getContext());
    mHistoryItemDaoHelper = get(HistoryItemDaoHelper.class);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    composeView = new ComposeView(requireContext());
    listView = new StickyListHeadersListView(requireContext());
    listView.setFastScrollEnabled(true);
    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    renderSearchScreen();
    return composeView;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
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
        return dataModel.getLanguage();
      }

      @Override
      public HistoryItem.Status getStatus(int volume, int page) {
        HistoryItem item = mHistoryItemDaoHelper.get(mCurrentHistory.getId(), volume, page);
        return item != null ? item.getStatus() : HistoryItem.Status.NONE;
      }

      @Override
      public ETDataModel getDataModel() {
        return dataModel;
      }

    };
    listView.setAdapter(mAdapter);
    listView.setOnItemClickListener(this);
    viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    viewModel.getSelected().observe(getViewLifecycleOwner(), language -> {
      dataModel = ETDataModelCreator.create(language, getActivity());
      listView.post(() -> {
        mAdapter.swapCursor(null);
        mAdapter.notifyDataSetChanged();
      });
    });

  }

  private void onSearchButtonClick() {
    if (mSearchInputText.trim().length() > 0) {
      startSearch();
    } else {
      Toast.makeText(getActivity(), R.string.please_enter_keywords, Toast.LENGTH_SHORT).show();
    }
  }

  private void renderSearchScreen() {
    if (composeView != null && listView != null) {
      SearchScreenBridge.render(
          composeView,
          mSearchInputText,
          listView,
          query -> mSearchInputText = query,
          this::onSearchButtonClick);
    }
  }

  private void startSearch() {
    if (dataModel.getLanguage() == BookDatabaseHelper.Language.THAIBT) {
      search(createTotalVolumesArray(dataModel.getTotalVolumes()));
    } else if (dataModel.getLanguage() == BookDatabaseHelper.Language.THAIWN ||
        dataModel.getLanguage() == BookDatabaseHelper.Language.THAIPB) {
      showBuddhawajDialog();
    } else {
      showBookCategorySelectionDialog();
    }
  }

  private Integer[] createTotalVolumesArray(int total) {
    ArrayList<Integer> volumes = new ArrayList<Integer>();
    for (int volume=1; volume <= total; ++volume) {
      volumes.add(volume);
    }
    return volumes.toArray(new Integer[] {});
  }

  private void showBuddhawajDialog() {
    AlertDialog dialog = new AlertDialog.Builder(getActivity())
        .setTitle(R.string.choose_search_type)
        .setSingleChoiceItems(R.array.buddhawaj_choices, -1, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            if (which == 0) {
              search(createTotalVolumesArray(dataModel.getTotalVolumes()), BookDatabaseHelper.SearchType.ALL);
            } else {
              search(createTotalVolumesArray(dataModel.getTotalVolumes()), BookDatabaseHelper.SearchType.BUDDHAWAJ);
            }
            dialog.dismiss();
          }
        })
        .setNegativeButton(android.R.string.cancel, null)
        .create();

    dialog.show();
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
              for (int volume = 1; volume <= dataModel.getSectionBoundary(0); volume++) {
                volumes.add(volume);
              }
            }
            if (mCheckedCategories.get(1, false)) {
              for (int volume = dataModel.getSectionBoundary(0)+1; volume <= dataModel.getSectionBoundary(1); volume++) {
                volumes.add(volume);
              }
            }
            if (mCheckedCategories.get(2, false)) {
              for (int volume = dataModel.getSectionBoundary(1)+1; volume <= dataModel.getSectionBoundary(2); volume++) {
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

  private boolean search(Integer[] volumes, BookDatabaseHelper.SearchType searchType) {
    if (mSearchInputText.trim().length() > 0) {
      mResultsCount = new int[] {0, 0, 0};
      mIsBuddhawaj = searchType == BookDatabaseHelper.SearchType.BUDDHAWAJ;
      dataModel.search(mSearchInputText, this, volumes, searchType);
      mProgressBar.setMax(volumes.length);
      mProgressBar.setProgress(0);
      mProgressDialog.show();
      return true;
    }
    return false;
  }

  private boolean search(Integer[] volumes) {
    return search(volumes, BookDatabaseHelper.SearchType.ALL);
  }

  @Override
  public void onSearchProgress(String keywords, int volume, int progress, Cursor cursor) {
    mProgressBar.setProgress(progress);
    if (volume <= dataModel.getSectionBoundary(0)) {
      mResultsCount[0] += cursor.getCount();
    } else if (volume >= dataModel.getSectionBoundary(0)+1 && volume <= dataModel.getSectionBoundary(1)) {
      mResultsCount[1] += cursor.getCount();
    } else {
      mResultsCount[2] += cursor.getCount();
    }
  }

  @Override
  public void onSearchFinish(String keywords, final Cursor cursor, int[] totalPages) {
    mKeywords = mSearchInputText;

    if (!mHistoryDaoHelper.contains(keywords, application.getLanguage(), mCheckedCategories, mIsBuddhawaj)) {
      History history = new History();
      history.setKeywords(keywords);
      history.setLanguage(application.getLanguage());
      history.setSections(mCheckedCategories);
      history.setResults(totalPages);
      history.setBuddhawaj(mIsBuddhawaj);
      StringBuilder sb = new StringBuilder();
      int start = Utils.isTipitaka(application.getLanguage()) ? 3 : 0;
      if (cursor.getCount() > start) {
        cursor.moveToPosition(start);
        while (!cursor.isAfterLast()) {
          sb.append(dataModel.getVolume(cursor));
          sb.append(':');
          sb.append(dataModel.getPageNumber(cursor));
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

    mCurrentHistory = mHistoryDaoHelper.get(keywords, application.getLanguage(), mCheckedCategories, mIsBuddhawaj);
    mCurrentHistoryItems = mHistoryItemDaoHelper.getByHistoryId(mCurrentHistory.getId());

    mProgressDialog.dismiss();
    mInputMethodManager.hideSoftInputFromWindow(composeView.getWindowToken(), 0);
    listView.post(new Runnable() {
      @Override
      public void run() {
        cursor.moveToFirst();
        mAdapter.swapCursor(cursor);
      }
    });

  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    if (position <= 2 && Utils.isTipitaka(dataModel.getLanguage())) {
      scrollToSection(position+1);
    } else {
      application.setHistory(mCurrentHistory);
      Cursor cursor = mAdapter.getCursor();
      cursor.moveToPosition(position);

      int volume = dataModel.getVolume(cursor);
      int page = dataModel.getPageNumber(cursor);

      MainActivity activity = (MainActivity) getActivity();
      Log.d(TAG, "openBook = " + mIsBuddhawaj);
      activity.openBook(application.getLanguage(), volume, page, mKeywords, mIsBuddhawaj);
      mHistoryItemDaoHelper.insertOrUpdate(mCurrentHistory.getId(), volume, page, HistoryItem.Status.READ);
    }
  }

  private void scrollToSection(int section) {
    if (section == 1 && mResultsCount[0] > 0) {
      listView.setSelectionFromTop(3, 0);
    } else if (section == 2 && mResultsCount[1] > 0) {
      listView.setSelectionFromTop(mResultsCount[0]+3, 0);
    } else if (section == 3 && mResultsCount[2] > 0) {
      listView.setSelectionFromTop(mResultsCount[0]+mResultsCount[1]+3, 0);
    }
  }

  private Cursor createCursorFromHistory(History history) {
    MatrixCursor itemCursor = new MatrixCursor(new String[] { "_id", dataModel.getVolumeColumn(), dataModel.getPageNumberColumn() });
    int id = 4;
    mResultsCount = new int[] { 0,0,0 };
    if (history.getContent().length() > 0) {
      Log.d(TAG, history.getContent().split(",").length + "");
      for (String item : history.getContent().split(",")) {
        String[] tokens = item.split(":");
        int volume = Integer.parseInt(tokens[0]);
        int page = Integer.parseInt(tokens[1]);
        itemCursor.addRow(new Object[] { id, volume, page });
        if (volume <= dataModel.getSectionBoundary(0)) {
          mResultsCount[0] += 1;
        } else if (volume >= dataModel.getSectionBoundary(0)+1 && volume <= dataModel.getSectionBoundary(1)) {
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

    return Utils.isTipitaka(dataModel.getLanguage()) ? new MergeCursor(new Cursor[] {headCursor, itemCursor}) : itemCursor;
  }

  public void loadHistory(final History history) {
    mCurrentHistory = history;
    mSearchInputText = history.getKeywords();
    renderSearchScreen();
    mKeywords = history.getKeywords();
    mIsBuddhawaj = history.isBuddhawaj();
    Log.d(TAG, "load history = " + mIsBuddhawaj);
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

  private void createProgressDialog(Activity activity) {
    int padding = (int) (16 * activity.getResources().getDisplayMetrics().density);
    LinearLayout layout = new LinearLayout(activity);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(padding, padding, padding, padding);

    TextView message = new TextView(activity);
    message.setText(R.string.please_wait);
    layout.addView(message);

    mProgressBar = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
    mProgressBar.setIndeterminate(false);
    layout.addView(mProgressBar);

    mProgressDialog = new AlertDialog.Builder(activity)
        .setTitle(R.string.searching)
        .setView(layout)
        .setCancelable(false)
        .create();
  }
}
