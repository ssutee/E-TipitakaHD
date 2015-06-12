package com.watnapp.etipitaka.plus.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.E_TipitakaApplication;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.plus.model.History;
import roboguice.inject.InjectView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 22/5/2013
 * Time: 22:03
 */

public class MenuFragment extends RoboSherlockFragment implements HistoryFragment.OnHistorySelectedListener {
  private static final String TAG = "MenuFragment";
  private TabHost mTabHost;
  private ViewPager  mViewPager;
  private TabsAdapter mTabsAdapter;
  private E_TipitakaApplication application;

  @InjectView(R.id.spn_languages)
  private Spinner spinner;

  public void setCurrentTab(int index) {
    mTabHost.setCurrentTab(index);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    application = (E_TipitakaApplication) getActivity().getApplication();
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Resources res = getResources();
    float scale = res.getDisplayMetrics().density;

    for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {
      TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
      tv.setTextSize(getResources().getDimension(R.dimen.tabwidget_text_size)/scale);
    }

    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
        R.array.full_languages, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);

    int selection = 0;
    for (int code : getResources().getIntArray(R.array.full_languages_code)) {
      if (code == application.getLanguage().getCode()) {
        break;
      }
      selection += 1;
    }
    spinner.setSelection(selection);
    spinner.setTag(R.id.pos, -1);
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int code = getResources().getIntArray(R.array.full_languages_code)[position];
        application.setLanguage(BookDatabaseHelper.Language.values()[code]);
        getActivity().getContentResolver().notifyChange(Constants.LANGUAGE_CHANGE_URI, null);
        if (spinner.getTag() != null && (Integer)spinner.getTag(R.id.pos) != position) {
          getActivity().getContentResolver().notifyChange(Constants.RESET_PAGE_URI, null);
        }
        spinner.setTag(R.id.pos, position);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });
  }

  public void setRadioButton(BookDatabaseHelper.Language language) {
    spinner.setTag(R.id.pos, language.getCode());
    int position = getResources().getIntArray(R.array.full_languages_position)[language.getCode()];
    spinner.setSelection(position, false);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mTabHost = (TabHost) inflater.inflate(R.layout.fragment_menu, container, false);
    mTabHost.setup();

    mViewPager = (ViewPager)mTabHost.findViewById(R.id.pager);
    mViewPager.setOffscreenPageLimit(4);

    mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);

    mTabsAdapter.addTab(mTabHost.newTabSpec("volume").setIndicator(getString(R.string.volume)),
        BookListFragment.class, null);
    mTabsAdapter.addTab(mTabHost.newTabSpec("search").setIndicator(getString(R.string.search)),
        SearchFragment.class, null);
    mTabsAdapter.addTab(mTabHost.newTabSpec("history").setIndicator(getString(R.string.history)),
        HistoryFragment.class, null);
    mTabsAdapter.addTab(mTabHost.newTabSpec("favorite").setIndicator(getString(R.string.favorite)),
        FavoriteFragment.class, null);

    if (savedInstanceState != null) {
      mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
    }

    return mTabHost;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString("tab", mTabHost.getCurrentTabTag());
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mTabHost = null;
  }

  @Override
  public void onHistorySelected(History history) {
    Log.d(TAG, history.getKeywords());
    SearchFragment fragment = (SearchFragment) mTabsAdapter.getFragmentByTag("search");
    fragment.loadHistory(history);
  }

  /**
   * This is a helper class that implements the management of tabs and all
   * details of connecting a ViewPager with associated TabHost.  It relies on a
   * trick.  Normally a tab host has a simple API for supplying a View or
   * Intent that each tab will show.  This is not sufficient for switching
   * between pages.  So instead we make the content part of the tab host
   * 0dp high (it is not shown) and the TabsAdapter supplies its own dummy
   * view to show as the tab content.  It listens to changes in tabs, and takes
   * care of switch to the correct paged in the ViewPager whenever the selected
   * tab changes.
   */
  public static class TabsAdapter extends FragmentStatePagerAdapter
      implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
    private final Context mContext;
    private final TabHost mTabHost;
    private final ViewPager mViewPager;
    private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
    private final HashMap<String, Fragment> mHash = new HashMap<String, Fragment>();

    static final class TabInfo {
      private final String tag;
      private final Class<?> clss;
      private final Bundle args;

      TabInfo(String _tag, Class<?> _class, Bundle _args) {
        tag = _tag;
        clss = _class;
        args = _args;
      }
    }

    static class DummyTabFactory implements TabHost.TabContentFactory {
      private final Context mContext;

      public DummyTabFactory(Context context) {
        mContext = context;
      }

      @Override
      public View createTabContent(String tag) {
        View v = new View(mContext);
        v.setMinimumWidth(0);
        v.setMinimumHeight(0);
        return v;
      }
    }

    public TabsAdapter(Fragment fragment, TabHost tabHost, ViewPager pager) {
      super(fragment.getChildFragmentManager());
      mContext = fragment.getActivity();
      mTabHost = tabHost;
      mViewPager = pager;
      mTabHost.setOnTabChangedListener(this);
      mViewPager.setAdapter(this);
      mViewPager.setOnPageChangeListener(this);
    }

    public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
      tabSpec.setContent(new DummyTabFactory(mContext));
      String tag = tabSpec.getTag();

      TabInfo info = new TabInfo(tag, clss, args);
      mTabs.add(info);
      mTabHost.addTab(tabSpec);
      notifyDataSetChanged();
    }

    @Override
    public int getCount() {
      return mTabs.size();
    }

    @Override
    public Fragment getItem(int position) {
      TabInfo info = mTabs.get(position);
      Fragment fragment = Fragment.instantiate(mContext, info.clss.getName(), info.args);
      mHash.put(info.tag, fragment);
      return fragment;
    }

    @Override
    public void onTabChanged(String tabId) {
      int position = mTabHost.getCurrentTab();
      mViewPager.setCurrentItem(position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
      // Unfortunately when TabHost changes the current tab, it kindly
      // also takes care of putting focus on it when not in touch mode.
      // The jerk.
      // This hack tries to prevent this from pulling focus out of our
      // ViewPager.
      TabWidget widget = mTabHost.getTabWidget();
      int oldFocusability = widget.getDescendantFocusability();
      widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
      mTabHost.setCurrentTab(position);
      widget.setDescendantFocusability(oldFocusability);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public int getItemPosition(Object object) {
      return POSITION_NONE;
    }

    public Fragment getFragmentByTag(String tab) {
      return mHash.get(tab);
    }
  }
}
