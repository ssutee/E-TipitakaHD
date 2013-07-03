package com.watnapp.etipitaka.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.meetup.adapter.CursorPagerAdapter;
import com.watnapp.etipitaka.Constants;
import com.watnapp.etipitaka.E_TipitakaApplication;
import com.watnapp.etipitaka.Utils;
import com.watnapp.etipitaka.fragment.MenuFragment;
import com.watnapp.etipitaka.fragment.PageFragment;
import com.watnapp.etipitaka.fragment.ReaderFragment;
import com.watnapp.etipitaka.fragment.TextEntryDialogFragment;
import com.watnapp.etipitaka.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.R;
import com.watnapp.etipitaka.model.HistoryItem;
import com.watnapp.etipitaka.model.HistoryItemDaoHelper;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 22/5/2013
 * Time: 8:03
 */

@ContentView(R.layout.activity_main)
public class MainActivity extends RoboSherlockFragmentActivity implements
    TextEntryDialogFragment.TextEntryDialogButtonClickListener {

  protected static final String TAG = "MainActivity";
  private static final String READER_FRAG_TAG = "reader";

  private static final int COMPARE_REQ = 0;

  @Inject
  private BookDatabaseHelper mDatabaseHelper;

  @Inject
  private InputMethodManager mInputMethodManager;

  @Inject
  private HistoryItemDaoHelper mHistoryItemDaoHelper;

  private SlidingMenu mSlidingMenu;
  private MenuFragment mMenuFragment;
  private E_TipitakaApplication application;
  private Handler mHandler = new Handler();
  private int currentVolume;
  private String currentKeywords;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    application = (E_TipitakaApplication) getApplication();

    mDatabaseHelper.openDatabase();

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    setupSlidingMenu();

    getSupportFragmentManager()
        .beginTransaction()
        .add(R.id.reader_fragment, ReaderFragment.newInstance(
            BookDatabaseHelper.Language.THAI, 1, 1, READER_FRAG_TAG), "")
        .commit();
  }

  public void openBook(BookDatabaseHelper.Language language, int volume, int page, String keywords) {
    currentKeywords = keywords;
    currentVolume = volume;

    getReaderFragment().openBook(language, volume, page, keywords);

    mSlidingMenu.showContent();
    getSupportActionBar().setTitle(application.getLanguage() == BookDatabaseHelper.Language.THAI
        ? R.string.thai_full_name : R.string.pali_full_name);
  }

  public void openBook(BookDatabaseHelper.Language language, int volume, int page) {
    openBook(language, volume, page, "");
  }

  public void openBook(BookDatabaseHelper.Language language, int volume) {
    openBook(language, volume, 1);
  }

  @Override
  protected void onDestroy() {
    mDatabaseHelper.closeDatabase();
    super.onDestroy();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(Menu.NONE, Constants.MENU_ITEM_SEARCH, Menu.NONE,
        R.string.search)
        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        .setIcon(android.R.drawable.ic_menu_search);

    menu.add(Menu.NONE, Constants.MENU_ITEM_SEARCH, Menu.NONE,
        R.string.save)
        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        .setIcon(android.R.drawable.ic_menu_save);

    menu.add(Menu.NONE, Constants.MENU_ITEM_COMPARE, Menu.NONE,
        R.string.compare)
        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        .setIcon(R.drawable.ic_menu_refresh);

    SubMenu gotoMenu = menu.addSubMenu(R.string.go_to);
    gotoMenu.add(Menu.NONE, Constants.MENU_ITEM_GOTO_PAGE,
        Menu.NONE, R.string.go_to_page).setShowAsActionFlags(
        MenuItem.SHOW_AS_ACTION_ALWAYS);
    gotoMenu.add(Menu.NONE, Constants.MENU_ITEM_GOTO_ITEM,
        Menu.NONE, R.string.go_to_item).setShowAsActionFlags(
        MenuItem.SHOW_AS_ACTION_ALWAYS);
    gotoMenu.setIcon(R.drawable.ic_menu_goto);
    gotoMenu.getItem().setIcon(R.drawable.ic_menu_goto)
        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case Constants.MENU_ITEM_SEARCH:
        mMenuFragment.setCurrentTab(1);
        mSlidingMenu.showMenu();
        return true;
      case android.R.id.home:
        mSlidingMenu.showMenu();
        return true;
      case Constants.MENU_ITEM_GOTO_PAGE:
        showGotoPageDialog();
        return true;
      case Constants.MENU_ITEM_GOTO_ITEM:
        showGotoItemDialog();
        return true;
      case Constants.MENU_ITEM_COMPARE:
        compare();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void compare(final Integer[] items) {
    CharSequence[] choices = new CharSequence[items.length];
    for (int i=0; i < items.length; ++i) {
      choices[i] = String.format("%s %s", getString(R.string.go_to_item),
          Utils.convertToThaiNumber(this, items[i]));
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.select_item);
    builder.setItems(choices, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Intent intent = new Intent(MainActivity.this, ComparisonActivity.class);
        intent.putExtra(Constants.LANGUAGE_KEY, application.getLanguage().ordinal());
        intent.putExtra(Constants.VOLUME_KEY, currentVolume);
        intent.putExtra(Constants.ITEM_KEY, items[which]);
        startActivityForResult(intent, COMPARE_REQ);
      }
    });
    builder.create().show();
  }

  private ReaderFragment getReaderFragment() {
    return (ReaderFragment) getSupportFragmentManager().findFragmentByTag(READER_FRAG_TAG);
  }

  private void compare() {
    int page = getReaderFragment().getCurrentPage();
    mDatabaseHelper.getItemsAtPage(application.getLanguage(), currentVolume, page,
        new BookDatabaseHelper.OnGetItemsListener() {
      @Override
      public void onGetItemsFinish(final Integer[] items) {
        mHandler.post(new Runnable() {
          @Override
          public void run() {
            compare(items);
          }
        });
      }
    });
  }

  @Override
  public void onBackPressed() {
    if (mSlidingMenu.isMenuShowing()) {
      mSlidingMenu.showContent();
    } else {
      super.onBackPressed();
    }
  }

  private void setupSlidingMenu() {
    mSlidingMenu = new SlidingMenu(this);
    mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
    mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
    mSlidingMenu.setShadowDrawable(R.drawable.shadow);
    mSlidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
    mSlidingMenu.setFadeDegree(0.35f);
    mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
    mSlidingMenu.setMenu(R.layout.menu_frame);
    mMenuFragment = new MenuFragment();
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.menu_frame, mMenuFragment)
        .commit();
  }

  private void showGotoPageDialog() {
    int minPage = mDatabaseHelper.getMinimumPageNumber(application.getLanguage(), currentVolume);
    int maxPage = mDatabaseHelper.getMaximumPageNumber(application.getLanguage(), currentVolume);

    TextEntryDialogFragment.newInstance(R.string.goto_page_title,
        getString(R.string.goto_page_message, minPage, maxPage), Constants.GOTO_PAGE_ID)
        .show(getSupportFragmentManager(), "dialog");
  }

  private void showGotoItemDialog() {
    int minItem = mDatabaseHelper.getMinimumItemNumber(application.getLanguage(), currentVolume);
    int maxItem = mDatabaseHelper.getMaximumItemNumber(application.getLanguage(), currentVolume);

    TextEntryDialogFragment.newInstance(R.string.goto_item_title,
        getString(R.string.goto_item_message, minItem, maxItem), Constants.GOTO_ITEM_ID)
        .show(getSupportFragmentManager(), "dialog");
  }

  @Override
  public void onTextEntryDialogPositiveButtonClick(String text, int id) {
    switch (id) {
      case Constants.GOTO_PAGE_ID:
        getReaderFragment().setCurrentPage(Integer.parseInt(text), true);
        break;
      case Constants.GOTO_ITEM_ID:
        gotoItem(Integer.parseInt(text));
        break;
    }
  }

  private void gotoItem(final int item) {
    final Integer[] pages = mDatabaseHelper.getPagesByItem(application.getLanguage(), currentVolume, item);
    final PageFragment fragment = getReaderFragment().getPageFragment(pages[0]);
    if (pages.length == 1) {
      getReaderFragment().setCurrentPage(pages[0], true);
      fragment.scrollToItem(item);
    } else if (pages.length > 1) {
      String[] choices = new String[pages.length];
      for (int i=0; i < pages.length; ++i) {
        choices[i] = String.format("%s %s", getString(R.string.go_to_page), Utils.convertToThaiNumber(this, pages[i]));
      }
      new AlertDialog.Builder(this).setTitle(R.string.select_item)
          .setItems(choices, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              getReaderFragment().setCurrentPage(pages[which], true);
              fragment.scrollToItem(item);
            }
          })
          .create().show();
    }
  }

  @Override
  public void onTextEntryDialogNegativeButtonClick() {

  }
}