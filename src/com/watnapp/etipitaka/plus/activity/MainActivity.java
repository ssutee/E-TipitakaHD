package com.watnapp.etipitaka.plus.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.E_TipitakaApplication;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.fragment.MenuFragment;
import com.watnapp.etipitaka.plus.fragment.PageFragment;
import com.watnapp.etipitaka.plus.fragment.ReaderFragment;
import com.watnapp.etipitaka.plus.fragment.TextEntryDialogFragment;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper.Language;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.model.*;
import org.json.JSONException;
import org.json.JSONObject;
import roboguice.inject.ContentView;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
  private static final int EXPORT_REQ = 1;
  private static final int IMPORT_REQ = 2;

  @Inject
  private BookDatabaseHelper mDatabaseHelper;

  @Inject
  private InputMethodManager mInputMethodManager;

  @Inject
  private HistoryItemDaoHelper mHistoryItemDaoHelper;

  @Inject
  private FavoriteDaoHelper mFavoriteDaoHelper;

  @Inject
  private HistoryDaoHelper mHistoryDaoHelper;

  private SlidingMenu mSlidingMenu;
  private MenuFragment mMenuFragment;
  private E_TipitakaApplication application;
  private Handler mHandler = new Handler();
  private int currentVolume, mSelectedPage, mSelectedItem;
  private String currentKeywords;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    application = (E_TipitakaApplication) getApplication();
    mDatabaseHelper.openDatabase();
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    try {
      String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
      getSupportActionBar().setTitle(getString(R.string.title_template, getString(R.string.app_name), versionName));
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    setupSlidingMenu();
    initReader();
  }

  @Override
  protected void onDestroy() {
    mDatabaseHelper.closeDatabase();
    SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt(Constants.LANGUAGE_KEY, application.getLanguage().getCode());
    editor.putInt(Constants.VOLUME_KEY, currentVolume);
    editor.putInt(Constants.PAGE_KEY, getReaderFragment().getCurrentPage());
    editor.commit();
    super.onDestroy();
  }

  private void initReader() {
    SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
    Language language =
        prefs.getInt(Constants.LANGUAGE_KEY,
            Language.THAI.getCode()) == Language.THAI.getCode() ? Language.THAI : Language.PALI;
    application.setLanguage(language);
    currentVolume = prefs.getInt(Constants.VOLUME_KEY, 1);
    int page = prefs.getInt(Constants.PAGE_KEY, 1);
    currentKeywords = "";
    getSupportFragmentManager()
        .beginTransaction()
        .add(R.id.reader_fragment, ReaderFragment.newInstance(
            language, currentVolume, page, ""), READER_FRAG_TAG)
        .commit();
  }

  public void openBook(BookDatabaseHelper.Language language, int volume, int page, String keywords, int item) {
    currentKeywords = keywords;
    currentVolume = volume;
    application.setLanguage(language);

    if (item == 0) {
      getReaderFragment().openBook(language, volume, page, keywords);
    } else {
      getReaderFragment().openBook(language, volume, page, item);
    }

    mSlidingMenu.showContent();
    getSupportActionBar().setTitle(application.getLanguage() == BookDatabaseHelper.Language.THAI
        ? R.string.thai_full_name : R.string.pali_full_name);
  }


  public void openBook(BookDatabaseHelper.Language language, int volume, int page, String keywords) {
    openBook(language, volume, page, keywords, 0);
  }

  public void openBook(BookDatabaseHelper.Language language, int volume, int page) {
    openBook(language, volume, page, "");
  }

  public void openBook(BookDatabaseHelper.Language language, int volume) {
    openBook(language, volume, 1);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(Menu.NONE, Constants.MENU_ITEM_SEARCH, Menu.NONE,
        R.string.search)
        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        .setIcon(android.R.drawable.ic_menu_search);

    menu.add(Menu.NONE, Constants.MENU_ITEM_SAVE, Menu.NONE,
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

    SubMenu preferencesMenu = menu.addSubMenu(R.string.preferences);
    preferencesMenu.add(Menu.NONE, Constants.MENU_ITEM_INCREASE_FONT_SIZE,
        Menu.NONE, R.string.increase_font_size)
        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    preferencesMenu.add(Menu.NONE, Constants.MENU_ITEM_DECREASE_FONT_SIZE,
        Menu.NONE, R.string.decrease_font_size)
        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    preferencesMenu.add(Menu.NONE, Constants.MENU_ITEM_IMPORT_DATA,
        Menu.NONE, R.string.import_data)
        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    preferencesMenu.add(Menu.NONE, Constants.MENU_ITEM_EXPORT_DATA,
        Menu.NONE, R.string.export_data)
        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

    preferencesMenu.setIcon(android.R.drawable.ic_menu_preferences);
    preferencesMenu.getItem().setIcon(android.R.drawable.ic_menu_preferences)
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
      case Constants.MENU_ITEM_INCREASE_FONT_SIZE:
        increaseFontSize();
        return true;
      case Constants.MENU_ITEM_DECREASE_FONT_SIZE:
        decreaseFontSize();
        return true;
      case Constants.MENU_ITEM_SAVE:
        takeNote();
        return true;
      case Constants.MENU_ITEM_EXPORT_DATA:
        exportData();
        return true;
      case Constants.MENU_ITEM_IMPORT_DATA:
        importData();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void exportData() {
    Intent intent = new Intent(this, FileExplorerActivity.class);
    intent.putExtra(Constants.TITLE_KEY, getString(R.string.export_title));
    intent.putExtra(Constants.SELECT_MODE_KEY, Constants.SELECT_MODE_FOLDER);
    startActivityForResult(intent, EXPORT_REQ);
  }

  private void importData() {
    Intent intent = new Intent(this, FileExplorerActivity.class);
    intent.putExtra(Constants.TITLE_KEY, getString(R.string.import_title));
    intent.putExtra(Constants.SELECT_MODE_KEY, Constants.SELECT_MODE_FILE);
    startActivityForResult(intent, IMPORT_REQ);
  }

  private void takeNote() {
    mSelectedPage = getReaderFragment().getCurrentPage();
    mDatabaseHelper.getItemsAtPage(application.getLanguage(), currentVolume, mSelectedPage,
        new BookDatabaseHelper.OnGetItemsListener() {
          @Override
          public void onGetItemsFinish(final Integer[] items, Integer[] sections) {
            final String[] choices = new String[items.length];
            for (int i = 0; i < items.length; ++i) {
              choices[i] = getString(R.string.go_to_item) + " "
                  + Utils.convertToThaiNumber(MainActivity.this, items[i]);
            }
            if (items.length > 1) {
              mHandler.post(new Runnable() {
                @Override
                public void run() {
                  new AlertDialog.Builder(MainActivity.this).setTitle(R.string.select_item)
                      .setItems(choices, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                          mSelectedItem = items[which];
                          String message = Utils.getSubtitle(MainActivity.this,
                              application.getLanguage(), currentVolume, mSelectedPage,
                              Utils.convertToThaiNumber(MainActivity.this, mSelectedItem));
                          TextEntryDialogFragment.newInstance(R.string.enter_note, message,
                              Constants.TAKE_NOTE_ID, 5, TextEntryDialogFragment.InputMode.TEXT)
                              .show(getSupportFragmentManager(), "take_note_dialog");
                        }
                      }).create().show();
                }
              });
            } else if (items.length == 1) {
              mSelectedItem = items[0];
              String message = Utils.getSubtitle(MainActivity.this,
                  application.getLanguage(), currentVolume, mSelectedPage,
                  Utils.convertToThaiNumber(MainActivity.this, mSelectedItem));
              TextEntryDialogFragment.newInstance(R.string.enter_note, message,
                  Constants.TAKE_NOTE_ID, 5, TextEntryDialogFragment.InputMode.TEXT)
                  .show(getSupportFragmentManager(), "take_note_dialog");
            }
          }
        });
  }

  private void takeNote(Language language, int volume, int page, int item, String text) {
    Favorite favorite = new Favorite();
    favorite.setLanguage(language);
    favorite.setVolume(volume);
    favorite.setPage(page);
    favorite.setItem(item);
    favorite.setNote(text);
    mFavoriteDaoHelper.insert(favorite);
    Toast.makeText(this, R.string.save_complete, Toast.LENGTH_SHORT).show();
  }

  private void increaseFontSize() {
    int size = getSharedPreferences(Constants.SETTING_PREFERENCES, Context.MODE_PRIVATE)
        .getInt(Constants.FONT_SIZE_KEY, Constants.DEFAULT_FONT_SIZE);
    size += Constants.FONT_SIZE_STEP;
    getReaderFragment().getCurrentPageFragment().setFontSize(size);
  }

  private void decreaseFontSize() {
    int size = getSharedPreferences(Constants.SETTING_PREFERENCES, Context.MODE_PRIVATE)
        .getInt(Constants.FONT_SIZE_KEY, Constants.DEFAULT_FONT_SIZE);
    size -= Constants.FONT_SIZE_STEP;
    getReaderFragment().getCurrentPageFragment().setFontSize(size);
  }

  private void compare(final Integer[] items, final Integer[] sections) {
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
        intent.putExtra(Constants.LANGUAGE_KEY, application.getLanguage().getCode());
        intent.putExtra(Constants.VOLUME_KEY, currentVolume);
        intent.putExtra(Constants.ITEM_KEY, items[which]);
        intent.putExtra(Constants.SECTION_KEY, sections[which]);
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
          public void onGetItemsFinish(final Integer[] items, final Integer[] sections) {
            mHandler.post(new Runnable() {
              @Override
              public void run() {
                compare(items, sections);
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
        .show(getSupportFragmentManager(), "goto_page_dialog");
  }

  private void showGotoItemDialog() {
    int minItem = mDatabaseHelper.getMinimumItemNumber(application.getLanguage(), currentVolume);
    int maxItem = mDatabaseHelper.getMaximumItemNumber(application.getLanguage(), currentVolume);

    TextEntryDialogFragment.newInstance(R.string.goto_item_title,
        getString(R.string.goto_item_message, minItem, maxItem), Constants.GOTO_ITEM_ID)
        .show(getSupportFragmentManager(), "goto_item_dialog");
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
      case Constants.TAKE_NOTE_ID:
        takeNote(application.getLanguage(), currentVolume, mSelectedPage, mSelectedItem, text);
        break;
    }
  }

  @Override
  public void onTextEntryDialogNegativeButtonClick() {
  }

  private void gotoItem(final int item) {
    final Integer[] pages = mDatabaseHelper.getPagesByItem(application.getLanguage(), currentVolume, item);
    if (pages.length == 1) {
      getReaderFragment().setCurrentPage(pages[0], true);
      PageFragment fragment = getReaderFragment().getPageFragment(pages[0]);
      if (fragment != null) {
        fragment.scrollToItem(item);
      }
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
              PageFragment fragment = getReaderFragment().getPageFragment(pages[0]);
              if (fragment != null) {
                fragment.scrollToItem(item);
              }
            }
          })
          .create().show();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == COMPARE_REQ && resultCode == RESULT_OK) {
      Language language = Language.PALI;
      if (data.getIntExtra(Constants.LANGUAGE_KEY, Language.THAI.getCode()) == Language.THAI.getCode()) {
        language = Language.THAI;
      }
      openBook(language, data.getIntExtra(Constants.VOLUME_KEY, currentVolume),
          data.getIntExtra(Constants.PAGE_KEY, getReaderFragment().getCurrentPage()),
          currentKeywords);
    } else if (requestCode == EXPORT_REQ && resultCode == RESULT_OK) {
      Log.d(TAG, data.getStringExtra(Constants.PATH_KEY));
      exportData(data.getStringExtra(Constants.PATH_KEY));
    } else if (requestCode == IMPORT_REQ && resultCode == RESULT_OK) {
      Log.d(TAG, data.getStringExtra(Constants.PATH_KEY));
      importData(data.getStringExtra(Constants.PATH_KEY));
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  private void importData(final String path) {
    if (!(new File(path).exists())) {
      Toast.makeText(this, R.string.file_not_found, Toast.LENGTH_SHORT).show();
      return;
    }
    final ProgressDialog dialog = new ProgressDialog(this);
    dialog.setIndeterminate(false);
    dialog.setCancelable(false);
    dialog.setTitle(null);
    dialog.setMessage(getString(R.string.importing_data));
    dialog.show();
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          JSONObject jsonObject = new JSONObject(Utils.readTextFile(path));
          Log.d(TAG, jsonObject.toString());

          mFavoriteDaoHelper.restoreJSONArray(jsonObject.getJSONArray(FavoriteTable.TABLE_NAME));
          mHistoryDaoHelper.restoreJSONArray(jsonObject.getJSONArray(HistoryTable.TABLE_NAME));

          mHandler.post(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(MainActivity.this, R.string.import_complete, Toast.LENGTH_SHORT).show();
            }
          });

        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        } catch (JSONException e) {
          e.printStackTrace();
        }
        mHandler.post(new Runnable() {
          @Override
          public void run() {
            dialog.dismiss();
          }
        });
      }
    }).start();
  }

  private void exportData(final String path) {
    final ProgressDialog dialog = new ProgressDialog(this);
    dialog.setIndeterminate(false);
    dialog.setCancelable(false);
    dialog.setTitle(null);
    dialog.setMessage(getString(R.string.exporting_data));
    dialog.show();
    new Thread(new Runnable() {
      @Override
      public void run() {
        JSONObject jsonObject = new JSONObject();
        Date now = new Date();
        try {
          jsonObject.put(FavoriteTable.TABLE_NAME, mFavoriteDaoHelper.dumpJSONArray());
          jsonObject.put(HistoryTable.TABLE_NAME, mHistoryDaoHelper.dumpJSONArray());
          (new File(path)).mkdirs();
          SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
          String filename = String.format("edata-%s", dateFormat.format(now));
          String expectedFilename = filename;
          int count = 0;
          while ((new File(path, expectedFilename+".js")).exists()) {
            count += 1;
            expectedFilename = filename + "_" + count;
          }
          try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new File(path, expectedFilename+".js"))));
            bw.write(jsonObject.toString());
            bw.flush();
            bw.close();
            mHandler.post(new Runnable() {
              @Override
              public void run() {
                Toast.makeText(MainActivity.this, R.string.export_complete, Toast.LENGTH_SHORT).show();
              }
            });
          } catch (FileNotFoundException e) {
            e.printStackTrace();
          } catch (IOException e) {
            e.printStackTrace();
          }
        } catch (JSONException e) {
          e.printStackTrace();
        }
        mHandler.post(new Runnable() {
          @Override
          public void run() {
            dialog.dismiss();
          }
        });
      }
    }).start();
  }
}