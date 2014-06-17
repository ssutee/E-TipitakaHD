package com.watnapp.etipitaka.plus.fragment;

import android.app.Activity;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockListFragment;
import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.E_TipitakaApplication;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.activity.MainActivity;
import com.watnapp.etipitaka.plus.adapter.BookListAdapter;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.plus.model.ETDataModel;
import com.watnapp.etipitaka.plus.model.ETDataModelCreator;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 22/5/2013
 * Time: 22:57

 */
public class BookListFragment extends RoboSherlockListFragment implements BookListAdapter.BookListAdapterDataSource {

  private E_TipitakaApplication application;
  private ContentObserver mContentObserver;
  private Handler mHandler = new Handler();
  private BookListAdapter mAdapter;
  private ETDataModel dataModel;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    application = (E_TipitakaApplication) activity.getApplication();
    dataModel = ETDataModelCreator.create(application.getLanguage(), getActivity());
    mContentObserver = new ContentObserver(mHandler) {
      @Override
      public void onChange(boolean selfChange) {
        mAdapter.notifyDataSetChanged();
        dataModel = ETDataModelCreator.create(application.getLanguage(), getActivity());
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
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mAdapter = new BookListAdapter(getActivity(), this);
    setListAdapter(mAdapter);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_book_list, container, false);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    MainActivity activity = (MainActivity) getActivity();
    activity.openBook(application.getLanguage(), position+1);
    application.setHistory(null);
  }

  @Override
  public int getTitlesArrayId() {
    if (application.getLanguage() == BookDatabaseHelper.Language.PALI) {
      return R.array.pali_book_titles_with_numbers;
    } else if (application.getLanguage() == BookDatabaseHelper.Language.THAIMM) {
      return R.array.thaimm_book_titles_with_numbers;
    } else if (application.getLanguage() == BookDatabaseHelper.Language.THAIBT) {
      return R.array.thaibt_book_titles_with_numbers;
    }
    return R.array.book_titles_with_number;
  }

  @Override
  public int getSectionsArrayId() {
    if (application.getLanguage() == BookDatabaseHelper.Language.PALI) {
      return R.array.pali_sections;
    } else if (application.getLanguage() == BookDatabaseHelper.Language.THAIBT) {
      return 0;
    }
    return R.array.sections;
  }

  @Override
  public int getSectionBoundary(int index) {
    return dataModel.getSectionBoundary(index);
  }
}
