package com.watnapp.etipitaka.plus.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.inject.Inject;
import com.meetup.adapter.CursorPagerAdapter;
import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.E_TipitakaApplication;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper.Language;
import com.watnapp.etipitaka.plus.model.HistoryItem;
import com.watnapp.etipitaka.plus.model.HistoryItemDaoHelper;
import com.watnapp.etipitaka.plus.widget.MyWebView;
import roboguice.inject.InjectView;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 2/7/2013
 * Time: 21:58
 */

public class ReaderFragment extends RoboSherlockFragment implements MyWebView.OnScrollChangedListener {

  private static final String TAG = "ReaderFragment";

  private CursorPagerAdapter<PageFragment> mPagerAdapter;

  @InjectView(R.id.viewpager)
  private ViewPager mViewPager;

  @InjectView(R.id.txt_subtitle)
  private TextView mTextSubtitle;

  @InjectView(R.id.seekbar)
  private SeekBar mSeekBar;

  @InjectView(R.id.layout_buttons)
  private View mButtons;

  @InjectView(R.id.btn_compare)
  private ImageView mCompareButton;

  @InjectView(R.id.btn_return)
  private ImageView returnButton;

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
  private boolean mShowButtons = false;

  private boolean mShowingSeekBar = false;
  private boolean mHidingSeekBar = false;

  private boolean mShowingButtons = false;
  private boolean mHidingButtons = false;

  public interface OnMenuButtonClickListener {
    public void onCompareButtonClick(Language language, int volume, int page);
    public void onReturnButtonClick(Language language, int volume, int page);
  }

  private OnMenuButtonClickListener onMenuButtonClickListener;

  public void setOnMenuButtonClickListener(OnMenuButtonClickListener onMenuButtonClickListener) {
    this.onMenuButtonClickListener = onMenuButtonClickListener;
  }

  public static ReaderFragment newInstance(Language language, int volume, int page,
                                           String keywords) {
    return ReaderFragment.newInstance(language, volume, page, keywords, false);
  }

  public static ReaderFragment newInstance(Language language, int volume, int page,
                                           String keywords, boolean compareButton) {
    ReaderFragment fragment = new ReaderFragment();
    Bundle args = new Bundle();
    args.putInt(Constants.LANGUAGE_KEY, language.getCode());
    args.putInt(Constants.VOLUME_KEY, volume);
    args.putInt(Constants.PAGE_KEY, page);
    args.putString(Constants.KEYWORDS_KEY, keywords);
    args.putBoolean(Constants.BUTTON_KEY, compareButton);
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
    mShowButtons = savedInstanceState.getBoolean(Constants.BUTTON_KEY);

  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(Constants.KEYWORDS_KEY, mKeywords);
    outState.putInt(Constants.VOLUME_KEY, mVolume);
    outState.putInt(Constants.PAGE_KEY, mPage);
    outState.putInt(Constants.LANGUAGE_KEY, mLanguage.getCode());
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_reader, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if (!mShowButtons) {
      mButtons.setVisibility(View.GONE);
    }

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
    mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      }

      @Override
      public void onPageSelected(int position) {
        mSeekBar.setProgress(position);
        updateSubtitle(mVolume, position + 1);
        mHidingSeekBar = mShowingSeekBar = false;
        mHidingButtons = mShowingButtons = false;
        hideSeekBar();
        if (application.getHistory() != null) {
          mHistoryItemDaoHelper.insertOrUpdate(application.getHistory().getId(), mVolume,
              position + 1, HistoryItem.Status.SKIMMED);
        }
        SharedPreferences prefs = getActivity()
            .getSharedPreferences(Constants.SETTING_PREFERENCES, Context.MODE_PRIVATE);
        PageFragment fragment = getPageFragment(position+1);
        int fontSize = prefs.getInt(Constants.FONT_SIZE_KEY, Constants.DEFAULT_FONT_SIZE);
        if (fragment != null && fontSize != fragment.getFontSize()) {
          fragment.setFontSize(fontSize);
        }
        String fontColor = prefs.getString(Constants.FONT_COLOR_KEY, Constants.DEFAULT_FONT_COLOR);
        String backgroundColor = prefs.getString(Constants.BACKGROUND_COLOR_KEY, Constants.DEFAULT_BACKGROUND_COLOR);
        if (fragment != null && !fontColor.equalsIgnoreCase(fragment.getFontColor())
            && !backgroundColor.equalsIgnoreCase(fragment.getBackgroundColor())) {
          fragment.setColor(fontColor, backgroundColor);
        }
      }

      @Override
      public void onPageScrollStateChanged(int state) {
      }
    });

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

    mCompareButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        doCompare(v);
      }
    });

    returnButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        doReturn(v);
      }
    });

    openBook(mLanguage, mVolume, mPage, mKeywords);

  }

  public PageFragment getCurrentPageFragment() {
    return getPageFragment(getCurrentPage());
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
    mLanguage = language;
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
      if (keywords != null && keywords.length() > 0) {
        PageFragment fragment = (PageFragment) mPagerAdapter.getFragment(page-1);
        if (fragment != null) {
          fragment.scrollToKeywords();
        }
      }
      updateSubtitle(volume, page);
    }
  }

  public void openBook(BookDatabaseHelper.Language language, int volume, int page, int item) {
    openBook(language, volume, page, "");
    PageFragment fragment = (PageFragment) mPagerAdapter.getFragment(page-1);
    if (fragment != null) {
      fragment.scrollToItem(item);
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
        getString(mLanguage == BookDatabaseHelper.Language.THAI
            ? R.string.thai_full_name : R.string.pali_full_name),
        Utils.convertToThaiNumber(getActivity(), volume),
        Utils.convertToThaiNumber(getActivity(), page)));
  }

  private void updateSubtitle(final int volume, final int page) {

    mDatabaseHelper.getItemsAtPage(mLanguage, volume, page,
        new BookDatabaseHelper.OnGetItemsListener() {
          @Override
          public void onGetItemsFinish(final Integer[] items, final Integer[] sections) {
            final String thaiItem;
            if (items.length > 1) {
              thaiItem = Utils.convertToThaiNumber(getActivity(), items[0]) + "-"
                  + Utils.convertToThaiNumber(getActivity(), items[items.length - 1]);
            } else if (items.length == 1) {
              thaiItem = Utils.convertToThaiNumber(getActivity(), items[0]);
            } else {
              thaiItem = Utils.convertToThaiNumber(getActivity(), 0);
            }
            mHandler.post(new Runnable() {
              @Override
              public void run() {
                mTextSubtitle.setText(Utils.getSubtitle(getActivity(), mLanguage, volume, page, thaiItem));
              }
            });
          }
        });
  }

  @Override
  public void onScrollUp(View v) {
    showSeekBar();
    hideButtons();
  }

  @Override
  public void onScrollDown(View v) {
    hideSeekBar();
    showButtons();
  }

  private void showButtons() {
    if (!mShowButtons || mButtons.getVisibility() == View.VISIBLE || mShowingButtons) {
      return;
    }

    mShowingButtons = true;
    TranslateAnimation animation = new TranslateAnimation(0, 0, mButtons.getHeight(), 0);
    animation.setDuration(100);
    animation.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {
      }

      @Override
      public void onAnimationEnd(Animation animation) {
        mButtons.setVisibility(View.VISIBLE);
        mShowingButtons = false;
      }

      @Override
      public void onAnimationRepeat(Animation animation) {
      }
    });
    mButtons.setVisibility(View.VISIBLE);
    mButtons.startAnimation(animation);
  }

  private void hideButtons() {
    if (!mShowButtons || mButtons.getVisibility() == View.GONE || mHidingButtons) {
      return;
    }

    mHidingButtons = true;
    TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mButtons.getHeight());
    animation.setDuration(100);
    animation.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {
      }

      @Override
      public void onAnimationEnd(Animation animation) {
        mButtons.setVisibility(View.GONE);
        mHidingButtons = false;
      }

      @Override
      public void onAnimationRepeat(Animation animation) {
      }
    });
    mButtons.startAnimation(animation);
  }

  private void showSeekBar() {
    if (mSeekBar.getVisibility() == View.VISIBLE || mShowingSeekBar) {
      return;
    }

    mShowingSeekBar = true;
    TranslateAnimation animation = new TranslateAnimation(0, 0, -mSeekBar.getHeight(), 0);
    animation.setDuration(100);
    animation.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {
      }

      @Override
      public void onAnimationEnd(Animation animation) {
        mSeekBar.setVisibility(View.VISIBLE);
        mShowingSeekBar = false;
      }

      @Override
      public void onAnimationRepeat(Animation animation) {
      }
    });
    mSeekBar.setVisibility(View.VISIBLE);
    mSeekBar.startAnimation(animation);
  }

  private void hideSeekBar() {
    if (mSeekBar.getVisibility() == View.GONE || mHidingSeekBar) {
      return;
    }

    mHidingSeekBar = true;
    TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -mSeekBar.getHeight());
    animation.setDuration(100);
    animation.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {
      }

      @Override
      public void onAnimationEnd(Animation animation) {
        mSeekBar.setVisibility(View.GONE);
        mHidingSeekBar = false;
      }

      @Override
      public void onAnimationRepeat(Animation animation) {
      }
    });
    mSeekBar.startAnimation(animation);
  }

  public void doCompare(View view) {
    try {
      ((OnMenuButtonClickListener)getActivity())
          .onCompareButtonClick(mLanguage, mVolume, mViewPager.getCurrentItem() + 1);
    } catch (ClassCastException e) {
    }
  }

  public void doReturn(View view) {
    try {
      ((OnMenuButtonClickListener)getActivity())
          .onReturnButtonClick(mLanguage, mVolume, mViewPager.getCurrentItem() + 1);
    } catch (ClassCastException e) {
    }
  }
}
