package com.watnapp.etipitaka.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;
import com.meetup.adapter.CursorPagerAdapter;
import com.watnapp.etipitaka.Constants;
import com.watnapp.etipitaka.E_TipitakaApplication;
import com.watnapp.etipitaka.R;
import com.watnapp.etipitaka.Utils;
import com.watnapp.etipitaka.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.model.HistoryItem;
import com.watnapp.etipitaka.model.HistoryItemDaoHelper;
import roboguice.inject.InjectView;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 2/7/2013
 * Time: 21:58
 */

public class ReaderFragment extends RoboSherlockFragment {

  private static final String TAG = "ReaderFragment";

  private CursorPagerAdapter<PageFragment> mPagerAdapter;

  @InjectView(R.id.viewpager)
  private ViewPager mViewPager;

  @InjectView(R.id.txt_subtitle)
  private TextView mTextSubtitle;

  @InjectView(R.id.seekbar)
  private SeekBar mSeekBar;

  @Inject
  private BookDatabaseHelper mDatabaseHelper;

  @Inject
  private HistoryItemDaoHelper mHistoryItemDaoHelper;

  private Handler mHandler = new Handler();

  private E_TipitakaApplication application;
  private String mKeywords;
  private BookDatabaseHelper.Language mLanguage;
  private int mVolume;
  private int mPage;


  public static ReaderFragment newInstance(BookDatabaseHelper.Language language, int volume, int page, String keywords) {
    ReaderFragment fragment = new ReaderFragment();
    Bundle args = new Bundle();
    args.putInt(Constants.LANGUAGE_KEY, language.ordinal());
    args.putInt(Constants.VOLUME_KEY, volume);
    args.putInt(Constants.PAGE_KEY, page);
    args.putString(Constants.KEYWORDS_KEY, keywords);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    application = (E_TipitakaApplication) getActivity().getApplication();

    if (savedInstanceState == null) {
      savedInstanceState = getArguments();
    }

    mKeywords = savedInstanceState.getString(Constants.KEYWORDS_KEY);
    mVolume = savedInstanceState.getInt(Constants.VOLUME_KEY);
    mPage = savedInstanceState.getInt(Constants.PAGE_KEY);
    mLanguage = BookDatabaseHelper.Language.values()[savedInstanceState.getInt(Constants.LANGUAGE_KEY)];

  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(Constants.KEYWORDS_KEY, mKeywords);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_reader, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mPagerAdapter = new CursorPagerAdapter<PageFragment>(getChildFragmentManager(),
        PageFragment.class, null) {
      @Override
      public Bundle buildArguments(Cursor cursor) {
        Bundle args = new Bundle();
        args.putString(Constants.KEYWORDS_KEY, mKeywords);
        args.putString(Constants.CONTENT_KEY, cursor.getString(cursor.getColumnIndex("content")));
        args.putInt(Constants.NUMBER_KEY, cursor.getInt(cursor.getColumnIndex("number")));
        return args;
      }
    };

    mViewPager.setAdapter(mPagerAdapter);

    mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
          updateNonItemSubtitle(mVolume, seekBar.getProgress() + 1);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        mViewPager.setCurrentItem(seekBar.getProgress(), false);
        updateSubtitle(mVolume, seekBar.getProgress()+1);
        if (application.getHistory() != null) {
          mHistoryItemDaoHelper.insertOrUpdate(application.getHistory().getId(), mVolume,
              seekBar.getProgress()+1, HistoryItem.Status.SKIMMED);
        }
      }
    });

    openBook(mLanguage, mVolume, mPage, mKeywords);

  }

  public PageFragment getPageFragment(int page) {
    return (PageFragment) mPagerAdapter.getFragment(page-1);
  }

  public int getCurrentPage() {
    return mViewPager.getCurrentItem() + 1;
  }

  public void setCurrentPage(int page, boolean smoothScroll) {
    mViewPager.setCurrentItem(page - 1, smoothScroll);
  }


  public void openBook(BookDatabaseHelper.Language language, int volume, int page, String keywords) {
    mKeywords = keywords;
    mVolume = volume;
    Cursor cursor = mDatabaseHelper.read(language, volume);
    cursor.moveToFirst();
    mPagerAdapter.swapCursor(cursor);
    mSeekBar.setProgress(0);
    if (page <= cursor.getCount()) {
      mViewPager.setCurrentItem(page-1, false);
      mSeekBar.setMax(cursor.getCount() - 1);
      mSeekBar.setProgress(page - 1);
    }

    updateSubtitle(volume, page);

    if (keywords != null && keywords.length() > 0) {
//      PageFragment fragment = (PageFragment) mPagerAdapter.getFragment(page-1);
//      fragment.scrollToKeywords();
      Log.d(TAG, "keywords = " + keywords);
    }

  }

  public void openBook(BookDatabaseHelper.Language language, int volume, int page) {
    openBook(language, volume, page, "");
  }

  public void openBook(BookDatabaseHelper.Language language, int volume) {
    openBook(language, volume, 1);
  }

  private void updateNonItemSubtitle(int volume, int page) {
    mTextSubtitle.setText(getString(R.string.non_item_subtitle_template,
        Utils.convertToThaiNumber(getActivity(), volume),
        Utils.convertToThaiNumber(getActivity(), page)));
  }

  private void updateSubtitle(final int volume, final int page) {

    mDatabaseHelper.getItemsAtPage(mLanguage, volume, page,
        new BookDatabaseHelper.OnGetItemsListener() {
          @Override
          public void onGetItemsFinish(final Integer[] items) {
            final String thaiItem;
            if (items.length > 1) {
              thaiItem = Utils.convertToThaiNumber(getActivity(), items[0]) + "-"
                  + Utils.convertToThaiNumber(getActivity(), items[items.length - 1]);
            } else {
              thaiItem = Utils.convertToThaiNumber(getActivity(), items[0]);
            }

            mHandler.post(new Runnable() {
              @Override
              public void run() {
                mTextSubtitle.setText(getString(R.string.subtitle_template,
                    Utils.convertToThaiNumber(getActivity(), volume),
                    Utils.convertToThaiNumber(getActivity(), page), thaiItem));
              }
            });
          }
        });
  }

}
