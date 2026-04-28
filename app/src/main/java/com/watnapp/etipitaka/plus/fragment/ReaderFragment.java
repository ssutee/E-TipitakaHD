package com.watnapp.etipitaka.plus.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.meetup.adapter.CursorPagerAdapter;
import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.ETipitakaApplication;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper.Language;
import com.watnapp.etipitaka.plus.model.ETDataModel;
import com.watnapp.etipitaka.plus.model.ETDataModelCreator;
import com.watnapp.etipitaka.plus.model.History;
import com.watnapp.etipitaka.plus.model.HistoryItem;
import com.watnapp.etipitaka.plus.model.HistoryItemDaoHelper;
import com.watnapp.etipitaka.plus.widget.MyWebView;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 2/7/2013
 * Time: 21:58
 */

public class ReaderFragment extends Fragment implements MyWebView.OnScrollChangedListener {

  private static final String TAG = "ReaderFragment";

  private CursorPagerAdapter<PageFragment> mPagerAdapter;

  private HistoryItemDaoHelper mHistoryItemDaoHelper;

  private Handler mHandler = new Handler();

  private ETipitakaApplication application;
  private String mKeywords;
  private boolean mIsBuddhawaj;
  private Language mLanguage;
  private int mVolume;
  private int mPage;
  private boolean mShowButtons = false;

  private boolean mShowingSeekBar = false;
  private boolean mHidingSeekBar = false;

  private boolean mShowingButtons = false;
  private boolean mHidingButtons = false;
  private int mSeekBarProgress = 0;
  private int mSeekBarMax = 0;
  private ETDataModel dataModel;

  private ReaderFragmentViews binding;

  public interface OnMenuButtonClickListener {
    public void onCompareButtonClick(Language language, int volume, int page);
    public void onReturnButtonClick(Language language, int volume, int page);
  }

  private OnMenuButtonClickListener onMenuButtonClickListener;

  public void setOnMenuButtonClickListener(OnMenuButtonClickListener onMenuButtonClickListener) {
    this.onMenuButtonClickListener = onMenuButtonClickListener;
  }

  public static ReaderFragment newInstance(Language language, int volume, int page,
                                           String keywords, boolean isBuddhawaj) {
    return ReaderFragment.newInstance(language, volume, page, keywords, isBuddhawaj, false);
  }

  public static ReaderFragment newInstance(Language language, int volume, int page,
                                           String keywords, boolean isBuddhawaj, boolean compareButton) {
    ReaderFragment fragment = new ReaderFragment();
    Bundle args = new Bundle();
    args.putInt(Constants.LANGUAGE_KEY, language.getCode());
    args.putInt(Constants.VOLUME_KEY, volume);
    args.putInt(Constants.PAGE_KEY, page);
    args.putString(Constants.KEYWORDS_KEY, keywords);
    args.putBoolean(Constants.BUTTON_KEY, compareButton);
    args.putBoolean(Constants.BUDDHAWAJ_KEY, isBuddhawaj);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    application = (ETipitakaApplication) Objects.requireNonNull(getActivity()).getApplication();
    mHistoryItemDaoHelper = new HistoryItemDaoHelper(getContext());


    if (savedInstanceState == null) {
      savedInstanceState = getArguments();
    }

    mKeywords = savedInstanceState.getString(Constants.KEYWORDS_KEY);
    mIsBuddhawaj = savedInstanceState.getBoolean(Constants.BUDDHAWAJ_KEY);
    mVolume = savedInstanceState.getInt(Constants.VOLUME_KEY);
    mPage = savedInstanceState.getInt(Constants.PAGE_KEY);
    Log.d(TAG, "restore volume = " + mVolume);
    Log.d(TAG, "restore page = " + mPage);
    mLanguage = Language.values()[savedInstanceState.getInt(Constants.LANGUAGE_KEY)];
    mShowButtons = savedInstanceState.getBoolean(Constants.BUTTON_KEY);
    dataModel = ETDataModelCreator.create(mLanguage, getActivity());
  }

  @Override
  public void onDestroy() {
    dataModel.closeDatabase();
    super.onDestroy();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(Constants.KEYWORDS_KEY, mKeywords);
    outState.putBoolean(Constants.BUDDHAWAJ_KEY, mIsBuddhawaj);
    outState.putInt(Constants.VOLUME_KEY, mVolume);
    outState.putInt(Constants.PAGE_KEY, mPage);
    outState.putBoolean(Constants.BUTTON_KEY, mShowButtons);
    outState.putInt(Constants.LANGUAGE_KEY, mLanguage.getCode());
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = ReaderFragmentContentBridge.create(requireContext());
    return binding.root;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    ReaderChromeBridge.renderSubtitle(binding.txtSubtitle, "");
    renderSeekBar();
    ReaderChromeBridge.renderBottomControls(
            binding.layoutButtons,
            () -> doCompare(binding.layoutButtons),
            () -> doReturn(binding.layoutButtons));

    if (!mShowButtons) {
      binding.layoutButtons.setVisibility(View.GONE);
    }
    updatePagerBottomMargin();

    try {
      final Field recyclerViewField = ViewPager2.class.getDeclaredField("mRecyclerView");
      recyclerViewField.setAccessible(true);

      final RecyclerView recyclerView = (RecyclerView) recyclerViewField.get(binding.viewpager);

      final Field touchSlopField = RecyclerView.class.getDeclaredField("mTouchSlop");
      touchSlopField.setAccessible(true);

      final int touchSlop = (int) touchSlopField.get(recyclerView);
      touchSlopField.set(recyclerView, touchSlop * 6);//6 is empirical value
    } catch (Exception ignore) {}

    mPagerAdapter = createPagerAdapter(null);
    binding.viewpager.setAdapter(mPagerAdapter);
    binding.viewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        super.onPageScrolled(position, positionOffset, positionOffsetPixels);
      }

      @Override
      public void onPageSelected(int position) {
        setSeekBarProgress(position);
        updateSubtitle(mVolume, position + 1);
        mHidingSeekBar = mShowingSeekBar = false;
        mHidingButtons = mShowingButtons = false;
        hideSeekBar();
        History history = application.getHistory();
        if (history != null) {
          mHistoryItemDaoHelper.insertOrUpdate(history.getId(), mVolume,
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
        super.onPageScrollStateChanged(state);
      }
    });

    Log.d(TAG, "open volume = " + mVolume);
    Log.d(TAG, "open page = " + mPage);

    openBook(mLanguage, mVolume, mPage, mKeywords, mIsBuddhawaj);

  }

  private CursorPagerAdapter<PageFragment> createPagerAdapter(Cursor cursor) {
    return new CursorPagerAdapter<PageFragment>(getChildFragmentManager(), getLifecycle(),
            PageFragment.class, cursor) {
      @Override
      public Bundle buildArguments(Cursor cursor) {
        Bundle args = new Bundle();
        args.putString(Constants.KEYWORDS_KEY, mKeywords);
        Log.d(TAG, "buildArguments = " + mIsBuddhawaj);
        args.putBoolean(Constants.BUDDHAWAJ_KEY, mIsBuddhawaj);
        String content = StringUtils.strip(cursor.getString(cursor.getColumnIndex(dataModel.getContentColumn())), "\n");

        args.putString(Constants.CONTENT_KEY, content);
        if (dataModel.hasHtmlContent()) {
          args.putString(Constants.HTML_CONTENT_KEY, cursor.getString(cursor.getColumnIndex("html")));
        }

        args.putInt(Constants.NUMBER_KEY, dataModel.getPageNumber(cursor));
        if (dataModel.hasFooter()) {
          args.putString(Constants.FOOTER_KEY, cursor.getString(cursor.getColumnIndex(dataModel.getFooterColumn())).trim());
        }

        args.putInt(Constants.LANGUAGE_KEY, dataModel.getLanguage().getCode());
        return args;
      }
    };
  }

  private void updatePagerBottomMargin() {
    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.viewpager.getLayoutParams();
    params.bottomMargin = mShowButtons
            ? getResources().getDimensionPixelSize(R.dimen.reader_bottom_controls_space)
            : 0;
    binding.viewpager.setLayoutParams(params);
  }

  public PageFragment getCurrentPageFragment() {
    return getPageFragment(getCurrentPage());
  }

  public PageFragment getPageFragment(int page) {
    return (PageFragment) mPagerAdapter.getFragment(page-1);
  }

  public int getCurrentPage() {
    return binding.viewpager.getCurrentItem() + 1;
  }

  public void setCurrentPage(int page, boolean smoothScroll) {
    binding.viewpager.setCurrentItem(page - 1, smoothScroll);
  }

  public void openBook(Language language, int volume, int page, String keywords, boolean isBuddhawaj) {
    if (mLanguage != language) {
      if (dataModel != null) {
        dataModel.closeDatabase();
      }
      dataModel = ETDataModelCreator.create(language, getActivity());
      mLanguage = language;
    }

    page = page - dataModel.getMinimumPageNumber(volume) + 1;
    mKeywords = keywords;
    mVolume = volume;
    mIsBuddhawaj = isBuddhawaj;
    Cursor cursor = dataModel.read(volume);
    if (cursor.getCount() == 0) {
      Toast toast = Toast.makeText(this.getActivity(), R.string.no_data, Toast.LENGTH_LONG);
      toast.setGravity(Gravity.CENTER, 0, 0);
      toast.show();
    }
    Log.d(TAG, "open book");
    Log.d(TAG, dataModel.getLanguage().getStringCode());
    Log.d(TAG, "volume = " + volume);
    Log.d(TAG, "total pages = " + cursor.getCount() + "");
    Log.d(TAG, "page = " + page);

    cursor.moveToFirst();

    mPagerAdapter = createPagerAdapter(cursor);
    binding.viewpager.setAdapter(mPagerAdapter);

    setSeekBarProgress(0);
    if (page <= cursor.getCount()) {
      binding.viewpager.setCurrentItem(page-1, false);
      setSeekBarMax(cursor.getCount() - 1);
      setSeekBarProgress(page - 1);
      binding.seekbar.setVisibility(View.VISIBLE);
    } else {
      binding.seekbar.setVisibility(View.GONE);
    }
    updateSubtitle(volume, page);
  }

  public void openBook(Language language, int volume, int page, int item) {
    openBook(language, volume, page, "", false);
    PageFragment fragment = (PageFragment) mPagerAdapter.getFragment(page-1);
    if (fragment != null && item > 0) {
      fragment.scrollToItem(item);
    }
  }

  public void openBook(Language language, int volume, int page) {
    openBook(language, volume, page, "", false);
  }

  public void openBook(Language language, int volume) {
    openBook(language, volume, 1);
  }

  private void updateNonItemSubtitle(int volume, int page) {
    String fullName = dataModel.getLanguage().getFullName(getActivity());
    ReaderChromeBridge.renderSubtitle(binding.txtSubtitle, getString(R.string.non_item_subtitle_template,
            fullName, Utils.convertToThaiNumber(getActivity(), volume),
            Utils.convertToThaiNumber(getActivity(), page)));
  }

  private void updateSubtitle(final int volume, final int page) {
    if (getActivity() == null) {
      return;
    }
    dataModel.getItemsAtPage(volume, page, new BookDatabaseHelper.OnGetItemsListener() {
      @Override
      public void onGetItemsFinish(final Integer[] items, final Integer[] sections) {
        final String thaiItem;
        if (items != null && items.length > 1) {
          thaiItem = Utils.convertToThaiNumber(getActivity(), items[0]) + "-"
              + Utils.convertToThaiNumber(getActivity(), items[items.length - 1]);
        } else if (items != null && items.length == 1) {
          thaiItem = Utils.convertToThaiNumber(getActivity(), items[0]);
        } else if (items != null) {
          thaiItem = Utils.convertToThaiNumber(getActivity(), 0);
        } else {
          thaiItem = "";
        }
        mHandler.post(new Runnable() {
          @Override
          public void run() {
            ReaderChromeBridge.renderSubtitle(binding.txtSubtitle, Utils.getSubtitle(getActivity(), mLanguage, volume,
                    page + dataModel.getMinimumPageNumber(volume) - 1, thaiItem));
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
    if (!mShowButtons || binding.layoutButtons.getVisibility() == View.VISIBLE || mShowingButtons) {
      return;
    }

    mShowingButtons = true;
    TranslateAnimation animation = new TranslateAnimation(0, 0,
            binding.layoutButtons.getHeight(), 0);
    animation.setDuration(100);
    animation.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {
      }

      @Override
      public void onAnimationEnd(Animation animation) {
        binding.layoutButtons.setVisibility(View.VISIBLE);
        mShowingButtons = false;
      }

      @Override
      public void onAnimationRepeat(Animation animation) {
      }
    });
    binding.layoutButtons.setVisibility(View.VISIBLE);
    binding.layoutButtons.startAnimation(animation);
  }

  private void hideButtons() {
    if (!mShowButtons || binding.layoutButtons.getVisibility() == View.GONE || mHidingButtons) {
      return;
    }

    mHidingButtons = true;
    TranslateAnimation animation = new TranslateAnimation(0, 0, 0,
            binding.layoutButtons.getHeight());
    animation.setDuration(100);
    animation.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {
      }

      @Override
      public void onAnimationEnd(Animation animation) {
        binding.layoutButtons.setVisibility(View.GONE);
        mHidingButtons = false;
      }

      @Override
      public void onAnimationRepeat(Animation animation) {
      }
    });
    binding.layoutButtons.startAnimation(animation);
  }

  private void showSeekBar() {
    if (binding.seekbar.getVisibility() == View.VISIBLE || mShowingSeekBar) {
      return;
    }

    mShowingSeekBar = true;
    TranslateAnimation animation = new TranslateAnimation(0, 0,
            -binding.seekbar.getHeight(), 0);
    animation.setDuration(100);
    animation.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {
      }

      @Override
      public void onAnimationEnd(Animation animation) {
        binding.seekbar.setVisibility(View.VISIBLE);
        mShowingSeekBar = false;
      }

      @Override
      public void onAnimationRepeat(Animation animation) {
      }
    });
    binding.seekbar.setVisibility(View.VISIBLE);
    binding.seekbar.startAnimation(animation);
  }

  private void hideSeekBar() {
    if (binding.seekbar.getVisibility() == View.GONE || mHidingSeekBar) {
      return;
    }

    mHidingSeekBar = true;
    TranslateAnimation animation = new TranslateAnimation(0, 0, 0,
            -binding.seekbar.getHeight());
    animation.setDuration(100);
    animation.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {
      }

      @Override
      public void onAnimationEnd(Animation animation) {
        binding.seekbar.setVisibility(View.GONE);
        mHidingSeekBar = false;
      }

      @Override
      public void onAnimationRepeat(Animation animation) {
      }
    });
    binding.seekbar.startAnimation(animation);
  }

  private void setSeekBarProgress(int progress) {
    mSeekBarProgress = progress;
    renderSeekBar();
  }

  private void setSeekBarMax(int max) {
    mSeekBarMax = max;
    if (mSeekBarProgress > max) {
      mSeekBarProgress = max;
    }
    renderSeekBar();
  }

  private void renderSeekBar() {
    ReaderChromeBridge.renderSeekBar(binding.seekbar, mSeekBarProgress, mSeekBarMax,
            (progress, fromUser) -> {
              mSeekBarProgress = progress;
              if (fromUser) {
                updateNonItemSubtitle(mVolume, progress + 1);
              } else {
                binding.viewpager.setCurrentItem(progress, false);
                updateSubtitle(mVolume, progress + 1);
                History history = application.getHistory();
                if (history != null) {
                  mHistoryItemDaoHelper.insertOrUpdate(history.getId(), mVolume,
                          progress + 1, HistoryItem.Status.SKIMMED);
                }
              }
            });
  }

  public void doCompare(View view) {
    try {
      ((OnMenuButtonClickListener)getActivity())
          .onCompareButtonClick(mLanguage, mVolume, binding.viewpager.getCurrentItem() + 1);
    } catch (ClassCastException e) {
    }
  }

  public void doReturn(View view) {
    try {
      ((OnMenuButtonClickListener)getActivity())
          .onReturnButtonClick(mLanguage, mVolume, binding.viewpager.getCurrentItem() + 1);
    } catch (ClassCastException e) {
    }
  }
}
