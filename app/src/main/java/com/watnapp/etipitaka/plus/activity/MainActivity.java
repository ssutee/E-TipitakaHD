package com.watnapp.etipitaka.plus.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.ETipitakaApplication;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.databinding.ActivityMainBinding;
import com.watnapp.etipitaka.plus.fragment.*;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper.Language;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.model.*;
import com.watnapp.etipitaka.plus.vm.SharedViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import static org.koin.java.KoinJavaComponent.*;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 22/5/2013
 * Time: 8:03
 */

public class MainActivity extends AppCompatActivity implements
    TextEntryDialogFragment.TextEntryDialogButtonClickListener, FontDialogFragment.FontDialogListener, FontDialogFragment.FontDialogDataSource {

  protected static final String TAG = "MainActivity";
  private static final String READER_FRAG_TAG = "reader";

  private HistoryItemDaoHelper mHistoryItemDaoHelper;
  private FavoriteDaoHelper mFavoriteDaoHelper;
  private HistoryDaoHelper mHistoryDaoHelper;

  private SlidingMenu mSlidingMenu;
  private MenuFragment mMenuFragment;
  private ETipitakaApplication application;
  private final Handler mHandler = new Handler(Looper.getMainLooper());
  private int mVolume, mSelectedPage, mSelectedItem;
  private String mKeywords;
  private boolean mIsBuddhawaj;
  private Language mLanguage;
  private ETDataModel dataModel, previousDataModel;
  private int mItemIndexSystem = 1;
  private ActivityMainBinding binding;
  private SharedViewModel viewModel;
  private final ActivityResultLauncher<Intent> compareActivityLauncher =
      registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != RESULT_OK || result.getData() == null) {
          return;
        }
        handleComparisonResult(result.getData());
      });
  private final ActivityResultLauncher<Intent> exportActivityLauncher =
      registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != RESULT_OK || result.getData() == null) {
          return;
        }
        String path = result.getData().getStringExtra(Constants.PATH_KEY);
        if (path == null || path.length() == 0) {
          Toast.makeText(this, R.string.file_not_found, Toast.LENGTH_SHORT).show();
          return;
        }
        Log.d(TAG, path);
        exportData(path);
      });
  private final ActivityResultLauncher<Intent> importActivityLauncher =
      registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != RESULT_OK || result.getData() == null) {
          return;
        }
        String path = result.getData().getStringExtra(Constants.PATH_KEY);
        if (path == null || path.length() == 0) {
          Toast.makeText(this, R.string.file_not_found, Toast.LENGTH_SHORT).show();
          return;
        }
        Log.d(TAG, path);
        importData(path);
      });

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    viewModel = new ViewModelProvider(this).get(SharedViewModel.class);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_menu_open_24);

    mHistoryItemDaoHelper = get(HistoryItemDaoHelper.class);
    mFavoriteDaoHelper = get(FavoriteDaoHelper.class);
    mHistoryDaoHelper = get(HistoryDaoHelper.class);

    binding = ActivityMainBinding.inflate(getLayoutInflater());
    View view = binding.getRoot();
    setContentView(view);
    application = (ETipitakaApplication) getApplication();
    viewModel.getSelected().observe(this, language -> {
      previousDataModel = dataModel;
      dataModel = ETDataModelCreator.create(language, MainActivity.this);
      if (viewModel.getResetPage()) {
        ETDataModel sourceDataModel = previousDataModel;
        final ETDataModel targetDataModel = dataModel;

        if (sourceDataModel.getLanguage() == targetDataModel.getLanguage()) {
          return;
        }
        sourceDataModel.convertToPivot(mVolume, 1, 1,
                (volume1, item, section) -> targetDataModel.convertFromPivot(volume1, item, section,
                        (volume2, page) -> {
                  mVolume = volume2;
                  dataModel = targetDataModel;
                  mLanguage = language;
                  mHandler.post(() -> getReaderFragment().openBook(mLanguage, mVolume, 1, "", false));
                }));
        mLanguage = language;
      }
    });

    mLanguage = application.getLanguage();
    dataModel = ETDataModelCreator.create(application.getLanguage(), this);

//    try {
//      String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
//      getSupportActionBar().setTitle(getString(R.string.title_template, getString(R.string.app_name), versionName));
//    } catch (PackageManager.NameNotFoundException e) {
//      e.printStackTrace();
//    }

    setupSlidingMenu();
    initReader();
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  protected void onStop() {
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt(Constants.LANGUAGE_KEY, application.getLanguage().getCode());
    editor.putInt(Constants.VOLUME_KEY, mVolume);
    editor.putInt(Constants.PAGE_KEY, getReaderFragment().getCurrentPage());
    Log.d(TAG, "save page = " + getReaderFragment().getCurrentPage());
    editor.apply();
    super.onDestroy();
  }

  private void initReader() {
    SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
    Language language = Language.values()[prefs.getInt(Constants.LANGUAGE_KEY, Language.THAI.getCode())];
    application.setLanguage(language);
    mVolume = prefs.getInt(Constants.VOLUME_KEY, 1);
    int page = prefs.getInt(Constants.PAGE_KEY, 1);
    Log.d(TAG, "load page = " + page);
    mKeywords = "";
    getSupportFragmentManager()
        .beginTransaction()
        .add(R.id.reader_fragment, ReaderFragment.newInstance(
            language, mVolume, page, "", false), READER_FRAG_TAG)
        .commit();
  }

  public void openBook(Language language, int volume, int page, String keywords, boolean isBuddhawaj, int item) {
    mKeywords = keywords;
    mIsBuddhawaj = isBuddhawaj;
    mVolume = volume;
    mLanguage = language;
    application.setLanguage(language);

    if (item == 0 && keywords.length() > 0) {
      getReaderFragment().openBook(language, volume, page, keywords, isBuddhawaj);
    } else {
      getReaderFragment().openBook(language, volume, page, item);
    }
    mSlidingMenu.showContent();
  }

  public void openBook(Language language, int volume, int page, String keywords, boolean isBuddhawaj) {
    openBook(language, volume, page, keywords, isBuddhawaj, 0);
  }

  public void openBook(Language language, int volume, int page) {
    openBook(language, volume, page, "", false);
  }

  public void openBook(Language language, int volume) {
    openBook(language, volume, 1);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(Menu.NONE, Constants.MENU_ITEM_SEARCH, Menu.NONE,
        R.string.search)
        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
        .setIcon(android.R.drawable.ic_menu_search);

    SubMenu gotoMenu = menu.addSubMenu(R.string.go_to);
    gotoMenu.add(Menu.NONE, Constants.MENU_ITEM_GOTO_PAGE,
        Menu.NONE, R.string.go_to_page).setShowAsActionFlags(
        MenuItem.SHOW_AS_ACTION_ALWAYS);
    gotoMenu.add(Menu.NONE, Constants.MENU_ITEM_GOTO_ITEM,
        Menu.NONE, R.string.go_to_item).setShowAsActionFlags(
        MenuItem.SHOW_AS_ACTION_ALWAYS);
    gotoMenu.setIcon(R.drawable.ic_menu_goto);
    gotoMenu.getItem().setIcon(R.drawable.ic_menu_goto)
        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);

    menu.add(Menu.NONE, Constants.MENU_ITEM_COMPARE, Menu.NONE,
            R.string.compare)
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            .setIcon(R.drawable.ic_menu_refresh);

    menu.add(Menu.NONE, Constants.MENU_ITEM_SAVE, Menu.NONE,
            R.string.save)
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            .setIcon(android.R.drawable.ic_menu_save);

    SubMenu dictMenu = menu.addSubMenu(R.string.dictionary);
    dictMenu.add(Menu.NONE, Constants.MENU_ITEM_PALI_DICT,
            Menu.NONE, R.string.pali_dict)
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    dictMenu.add(Menu.NONE, Constants.MENU_ITEM_THAI_DICT,
            Menu.NONE, R.string.thai_dict)
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    dictMenu.add(Menu.NONE, Constants.MENU_ITEM_ENG_DICT,
            Menu.NONE, R.string.english_dict)
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
    dictMenu.setIcon(R.drawable.baseline_menu_book_24);
    dictMenu.getItem().setIcon(R.drawable.baseline_menu_book_24)
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);

    SubMenu preferencesMenu = menu.addSubMenu(R.string.preferences);
    SubMenu dataMenu = preferencesMenu.addSubMenu(R.string.manage_data);
    dataMenu.add(Menu.NONE, Constants.MENU_ITEM_IMPORT_DATA,
        Menu.NONE, R.string.import_data);
    dataMenu.add(Menu.NONE, Constants.MENU_ITEM_EXPORT_DATA,
        Menu.NONE, R.string.export_data);
    dataMenu.getItem().setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

    preferencesMenu.add(Menu.NONE, Constants.MENU_ITEM_ADJUST_FONT_SIZE, Menu.NONE, R.string.adjust_font_size);

    SubMenu colorMenu = preferencesMenu.addSubMenu(R.string.adjust_font_color);
    colorMenu.add(Menu.NONE, Constants.MENU_ITEM_BLACK_COLOR,
        Menu.NONE, R.string.black_color);
    colorMenu.add(Menu.NONE, Constants.MENU_ITEM_WHITE_COLOR,
        Menu.NONE, R.string.white_color);
    colorMenu.add(Menu.NONE, Constants.MENU_ITEM_SEPIA_COLOR,
        Menu.NONE, R.string.sepia_color);
    colorMenu.getItem().setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);

    preferencesMenu.setIcon(android.R.drawable.ic_menu_preferences);
    preferencesMenu.getItem().setIcon(android.R.drawable.ic_menu_preferences)
        .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);

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
        if (mLanguage == Language.THAIMC) {
          showItemsIndexSystemDialog();
        } else if (mLanguage != Language.THAIBT && mLanguage != Language.THAIPB) {
          mItemIndexSystem = 1;
          showGotoItemDialog();
        }
        return true;
      case Constants.MENU_ITEM_COMPARE:
        chooseLanguage();
        return true;
      case Constants.MENU_ITEM_SAVE:
        takeNote();
        return true;
      case Constants.MENU_ITEM_PALI_DICT:
        showPaliDict();
        return true;
      case Constants.MENU_ITEM_THAI_DICT:
        showThaiDict();
        return true;
      case Constants.MENU_ITEM_ENG_DICT:
        showEnglishDict();
        return true;
      case Constants.MENU_ITEM_EXPORT_DATA:
        exportData();
        return true;
      case Constants.MENU_ITEM_IMPORT_DATA:
        importData();
        return true;
      case Constants.MENU_ITEM_INCREASE_FONT_SIZE:
        increaseFontSize();
        return true;
      case Constants.MENU_ITEM_DECREASE_FONT_SIZE:
        decreaseFontSize();
        return true;
      case Constants.MENU_ITEM_BLACK_COLOR:
        setColor("#010101", "#FEFEFE");
        return true;
      case Constants.MENU_ITEM_WHITE_COLOR:
        setColor("#FEFEFE", "#010101");
        return true;
      case Constants.MENU_ITEM_SEPIA_COLOR:
        setColor("#5E4933", "#F9EFD8");
        return true;
      case Constants.MENU_ITEM_ADJUST_FONT_SIZE:
        showFontDialog();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void showFontDialog() {
    FontDialogFragment dialog = new FontDialogFragment();
    dialog.show(getSupportFragmentManager(), "FontDialogFragment");
  }

  private void showPaliDict() {
    startActivity(new Intent(this, PaliDictActivity.class));
  }

  private void showThaiDict() {
    startActivity(new Intent(this, ThaiDictActivity.class));
  }

  private void showEnglishDict() {
    startActivity(new Intent(this, EnglishDictActivity.class));
  }

  private void exportData() {
    Intent intent = new Intent(this, FileExplorerActivity.class);
    intent.putExtra(Constants.TITLE_KEY, getString(R.string.export_title));
    intent.putExtra(Constants.SELECT_MODE_KEY, Constants.SELECT_MODE_FOLDER);
    exportActivityLauncher.launch(intent);
  }

  private void importData() {
    Intent intent = new Intent(this, FileExplorerActivity.class);
    intent.putExtra(Constants.TITLE_KEY, getString(R.string.import_title));
    intent.putExtra(Constants.SELECT_MODE_KEY, Constants.SELECT_MODE_FILE);
    importActivityLauncher.launch(intent);
  }

  private void takeNote() {
    mSelectedPage = getReaderFragment().getCurrentPage() + dataModel.getMinimumPageNumber(mVolume) - 1;
    dataModel.getItemsAtPage(mVolume, mSelectedPage, new BookDatabaseHelper.OnGetItemsListener() {
      @Override
      public void onGetItemsFinish(final Integer[] items, Integer[] sections) {
        final String[] choices = items != null ? new String[items.length] : null;
        if (items != null) {
          for (int i = 0; i < items.length; ++i) {
            choices[i] = getString(R.string.go_to_item) + " "
                + Utils.convertToThaiNumber(MainActivity.this, items[i]);
          }
        }

        if (items != null && items.length > 1) {
          mHandler.post(new Runnable() {
            @Override
            public void run() {
              new AlertDialog.Builder(MainActivity.this).setTitle(R.string.select_item)
                  .setItems(choices, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                      mSelectedItem = items[which];
                      String message = Utils.getSubtitle(MainActivity.this,
                              application.getLanguage(), mVolume, mSelectedPage,
                          Utils.convertToThaiNumber(MainActivity.this, mSelectedItem));
                      TextEntryDialogFragment.newInstance(R.string.enter_note, message,
                          Constants.TAKE_NOTE_ID, 5, TextEntryDialogFragment.InputMode.TEXT)
                          .show(getSupportFragmentManager(), "take_note_dialog");
                    }
                  }).create().show();
            }
          });
        } else if (items != null && items.length == 1) {
          mSelectedItem = items[0];
          String message = Utils.getSubtitle(MainActivity.this,
                  application.getLanguage(), mVolume, mSelectedPage,
              Utils.convertToThaiNumber(MainActivity.this, mSelectedItem));
          TextEntryDialogFragment.newInstance(R.string.enter_note, message,
              Constants.TAKE_NOTE_ID, 5, TextEntryDialogFragment.InputMode.TEXT)
              .show(getSupportFragmentManager(), "take_note_dialog");
        } else {
          mSelectedItem = 0;
          String message = Utils.getSubtitle(MainActivity.this,
                  application.getLanguage(), mVolume, mSelectedPage, "");
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

  private void setColor(String font, String background) {
    getReaderFragment().getCurrentPageFragment().setColor(font, background);
  }

  private void chooseLanguage() {
    final ArrayList<Pair<String, Pair<Integer, Integer>>> references = new ArrayList<Pair<String, Pair<Integer, Integer>>>();

    if (dataModel.getLanguage() == Language.THAIBT || dataModel.getLanguage() == Language.THAIPB) {
      Pattern pattern = Pattern.compile(Constants.REFS_PATTERN);
      Matcher matcher = pattern.matcher(getReaderFragment().getCurrentPageFragment().getContent());
      while (matcher.find()) {
        references.add(new Pair<String, Pair<Integer, Integer>>(matcher.group(0),
            new Pair<Integer, Integer>(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(3)))));

      }
      if (references.size() == 0) {
        return;
      }
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.select_langauge);
    builder.setItems(Constants.COMPARE_TITLES,
            (dialog, which) -> {
              int page = getReaderFragment().getCurrentPage();
              Language targetLanguage = Constants.COMPARE_LANGUAGES[which];
              String dbPath = Utils.getDatabasePath(MainActivity.this, targetLanguage);
              if (new File(dbPath).exists()) {
                compare(references, page, targetLanguage);
              } else {
                Toast.makeText(MainActivity.this,
                        R.string.database_not_found, Toast.LENGTH_LONG).show();
              }
            });
    builder.create().show();
  }

  private void compare(ArrayList<Pair<String, Pair<Integer, Integer>>> references,
                       int page, Language targetLanguage) {
    dataModel.getComparingItemsAtPage(mVolume, page, (items, sections) -> {
      if (dataModel.getLanguage() == Language.THAIBT || dataModel.getLanguage() == Language.THAIPB) {
        compare(references, targetLanguage);
      } else {
        mHandler.post(() -> {
          compare(items, sections, targetLanguage);
        });
      }
    });
  }

  private void startComparisonActivity(int item, int section, Language language) {
    Intent intent = new Intent(MainActivity.this, ComparisonActivity.class);
    intent.putExtra(Constants.LANGUAGE_KEY, application.getLanguage().getCode());
    intent.putExtra(Constants.COMPARING_LANGUAGE_KEY, language.getCode());
    intent.putExtra(Constants.VOLUME_KEY, mVolume);
    intent.putExtra(Constants.KEYWORDS_KEY, mKeywords);
    intent.putExtra(Constants.BUDDHAWAJ_KEY, mIsBuddhawaj);
    intent.putExtra(Constants.PAGE_KEY, getReaderFragment().getCurrentPage());
    intent.putExtra(Constants.ITEM_KEY, item);
    intent.putExtra(Constants.SECTION_KEY, section);
    compareActivityLauncher.launch(intent);
  }

  private void startComparisonActivityWithReference(int volume, int item, Language language) {
    Intent intent = new Intent(MainActivity.this, ComparisonActivity.class);
    intent.putExtra(Constants.LANGUAGE_KEY, application.getLanguage().getCode());
    intent.putExtra(Constants.COMPARING_LANGUAGE_KEY, language.getCode());
    intent.putExtra(Constants.VOLUME_KEY, mVolume);
    intent.putExtra(Constants.KEYWORDS_KEY, mKeywords);
    intent.putExtra(Constants.BUDDHAWAJ_KEY, mIsBuddhawaj);
    intent.putExtra(Constants.PAGE_KEY, getReaderFragment().getCurrentPage());
    intent.putExtra(Constants.ITEM_KEY, item);
    intent.putExtra(Constants.SECTION_KEY, 1);
    intent.putExtra(Constants.COMPARING_VOLUME_KEY, volume);
    compareActivityLauncher.launch(intent);
  }

  private void compare(final ArrayList<Pair<String, Pair<Integer, Integer>>> references, final Language language) {
    CharSequence[] choices = new CharSequence[references.size()];
    for (int i=0; i < references.size(); ++i) {
      choices[i] = references.get(i).first;
    }

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.select_item);
    builder.setItems(choices, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Log.d(TAG, "compare to");
        Log.d(TAG, references.get(which).first + "");
        Log.d(TAG, references.get(which).second.first + "");
        Log.d(TAG, references.get(which).second.second + "");
        startComparisonActivityWithReference(references.get(which).second.first,
            references.get(which).second.second, language);
      }
    });
    builder.create().show();
  }

  private void compare(final Integer[] items, final Integer[] sections, final Language language) {
    CharSequence[] choices = new CharSequence[items.length];
    for (int i=0; i < items.length; ++i) {
      choices[i] = String.format("%s %s", getString(R.string.go_to_item),
          Utils.convertToThaiNumber(this, items[i]));
    }

    if (items.length > 1) {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(R.string.select_item);
      builder.setItems(choices, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          startComparisonActivity(items[which], sections[which], language);
        }
      });
      builder.create().show();
    } else {
      startComparisonActivity(items[0], sections[0], language);
    }

  }

  private ReaderFragment getReaderFragment() {
    return (ReaderFragment) getSupportFragmentManager().findFragmentByTag(READER_FRAG_TAG);
  }

  @Override
  public void onBackPressed() {
    if (mSlidingMenu.isMenuShowing()) {
      mSlidingMenu.showContent();
    } else {
      new AlertDialog.Builder(this)
          .setTitle(R.string.exit_program)
          .setMessage(R.string.are_you_sure)
          .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              MainActivity.super.onBackPressed();
            }
          })
          .setNegativeButton(android.R.string.no, null)
          .create()
          .show();
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
    int minPage = dataModel.getMinimumPageNumber(mVolume);
    int maxPage = dataModel.getMaximumPageNumber(mVolume);

    TextEntryDialogFragment.newInstance(R.string.goto_page_title,
        getString(R.string.goto_page_message, minPage, maxPage), Constants.GOTO_PAGE_ID)
        .show(getSupportFragmentManager(), "goto_page_dialog");
  }

  private void showGotoItemDialog() {
    int minItem, maxItem;

    if (mItemIndexSystem == 0) {
      ETDataModel dm = ETDataModelCreator.create(Language.THAI, this);
      minItem = dm.getMinimumItemNumber(mVolume);
      maxItem = dm.getMaximumItemNumber(mVolume);
    } else {
      minItem = dataModel.getMinimumItemNumber(mVolume);
      maxItem = dataModel.getMaximumItemNumber(mVolume);
    }

    TextEntryDialogFragment.newInstance(R.string.goto_item_title,
        getString(R.string.goto_item_message, minItem, maxItem), Constants.GOTO_ITEM_ID)
        .show(getSupportFragmentManager(), "goto_item_dialog");
  }

  private void showItemsIndexSystemDialog() {
    new AlertDialog.Builder(this).setTitle(R.string.choose_items_index_system)
        .setItems(new String[]{getString(R.string.siamrat), dataModel.getShortTitle()},
                (dialog, which) -> {
                  mItemIndexSystem = which;
                  showGotoItemDialog();
                }).setNegativeButton(android.R.string.cancel, null).create().show();
  }

  @Override
  public void onTextEntryDialogPositiveButtonClick(String text, int id) {
    switch (id) {
      case Constants.GOTO_PAGE_ID:
        try {
          getReaderFragment().setCurrentPage(Integer.parseInt(text) - dataModel.getMinimumPageNumber(mVolume) + 1, true);
        } catch (NumberFormatException e) {
          e.printStackTrace();
        }
        break;
      case Constants.GOTO_ITEM_ID:
        try {
          gotoItem(Integer.parseInt(text));
        } catch (NumberFormatException e) {
          e.printStackTrace();
        }
        break;
      case Constants.TAKE_NOTE_ID:
        takeNote(application.getLanguage(), mVolume, mSelectedPage, mSelectedItem, text);
        break;
    }
  }

  @Override
  public void onTextEntryDialogNegativeButtonClick() {
  }

  private void gotoItem(final int item) {
    final Integer[] pages = dataModel.getPagesByItem(mVolume, item, mItemIndexSystem==0);

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

  private void handleComparisonResult(Intent data) {
    int code = data.getIntExtra(Constants.LANGUAGE_KEY, Language.THAI.getCode());
    Language language = Language.values()[code];
    Log.d(TAG, language.getStringCode());

    mMenuFragment.setRadioButton(language);

    Log.d(TAG, language.toString() + ":" + data.getIntExtra(Constants.VOLUME_KEY, mVolume) +
        ":" + data.getIntExtra(Constants.PAGE_KEY, getReaderFragment().getCurrentPage()));

    openBook(language, data.getIntExtra(Constants.VOLUME_KEY, mVolume),
        data.getIntExtra(Constants.PAGE_KEY, getReaderFragment().getCurrentPage()),
        mKeywords, mIsBuddhawaj);
  }


  private String unpackAppleImportData(String filePath) {
    InputStream is;
    ZipInputStream zis;
    ByteArrayOutputStream sout = new ByteArrayOutputStream();
    StringBuffer sb = new StringBuffer();
    try {
      is = new FileInputStream(filePath);
      zis = new ZipInputStream(new BufferedInputStream(is));
      ZipEntry ze;
      byte[] buffer = new byte[1024];
      int count;
      while ((ze = zis.getNextEntry()) != null) {
        while ((count = zis.read(buffer)) != -1) {
          sout.write(buffer, 0, count);
          sb.append(sout.toString("utf-8"));
          sout.reset();
        }
        zis.closeEntry();
      }
      sout.close();
      zis.close();
      return sb.toString();
    } catch(IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private void importAndroidData(String path) {
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
  }

  private JSONArray convertBookmarksAppleData(JSONArray bookmarks) throws JSONException {
    JSONArray results = new JSONArray();
    for (int i=0; i < bookmarks.length(); ++i) {
      JSONObject originObj = (JSONObject) bookmarks.get(i);
      JSONObject convertedObj = new JSONObject();
      convertedObj.put(FavoriteTable.FavoriteColumns.VOLUME, originObj.getInt("volume"));
      convertedObj.put(FavoriteTable.FavoriteColumns.PAGE, originObj.getInt("page"));
      convertedObj.put(FavoriteTable.FavoriteColumns.LANGUAGE, originObj.getInt("code")-1);
      convertedObj.put(FavoriteTable.FavoriteColumns.ITEM, 0);
      convertedObj.put(FavoriteTable.FavoriteColumns.NOTE, originObj.getString("note"));
      convertedObj.put(FavoriteTable.FavoriteColumns.SCORE, originObj.getInt("rank"));
      results.put(convertedObj);
    }
    return results;
  }

  private JSONArray convertHistoriesAppleData(JSONArray histories) throws JSONException {
    JSONArray results = new JSONArray();
    for (int i=0; i < histories.length(); ++i) {
      JSONObject originObj = (JSONObject) histories.get(i);
      JSONObject convertedObj = new JSONObject();
      convertedObj.put(HistoryTable.HistoryColumns.KEYWORDS, originObj.getString("keywords"));
      convertedObj.put(HistoryTable.HistoryColumns.SECTION1, true);
      convertedObj.put(HistoryTable.HistoryColumns.SECTION2, true);
      convertedObj.put(HistoryTable.HistoryColumns.SECTION3, true);
      convertedObj.put(HistoryTable.HistoryColumns.CONTENT, originObj.getString("items").replace(' ', ','));
      convertedObj.put(HistoryTable.HistoryColumns.LANGUAGE, originObj.getInt("code")-1);
      convertedObj.put(HistoryTable.HistoryColumns.SCORE, originObj.getBoolean("starred") ? 1 : 0);
      int b1 = 8, b2 = 33;
      if (originObj.getInt("code") == 3) {
        b1 = 10;
        b2 = 74;
      }
      int result1 = 0, result2 = 0, result3 = 0;
      if (originObj.getString("items").length() > 0) {
        for (String token : originObj.getString("items").split("\\s+")) {
          int volume = Integer.parseInt(token.split(":")[0]);
          if (volume <= b1) {
            result1 += 1;
          } else if (volume <= b2) {
            result2 += 1;
          } else {
            result3 += 1;
          }
        }
      }
      convertedObj.put(HistoryTable.HistoryColumns.RESULT1, result1);
      convertedObj.put(HistoryTable.HistoryColumns.RESULT2, result2);
      convertedObj.put(HistoryTable.HistoryColumns.RESULT3, result3);
      JSONArray items = new JSONArray();
      if (originObj.getString("items").length() > 0) {
        String[] tokens = originObj.getString("items").split("\\s+");
        for (String index : originObj.getString("read").split("\\s+")) {
          if (index.length() == 0) {
            continue;
          }
          String[] pair = tokens[Integer.parseInt(index)].split(":");
          JSONObject item =  new JSONObject();
          item.put(HistoryItemTable.HistoryItemColumns.VOLUME, Integer.parseInt(pair[0]));
          item.put(HistoryItemTable.HistoryItemColumns.PAGE, Integer.parseInt(pair[1]));
          item.put(HistoryItemTable.HistoryItemColumns.STATUS, 1);
          items.put(item);
        }
        for (String index : originObj.getString("skimmed").split("\\s+")) {
          if (index.length() == 0) {
            continue;
          }
          String[] pair = tokens[Integer.parseInt(index)].split(":");
          JSONObject item =  new JSONObject();
          item.put(HistoryItemTable.HistoryItemColumns.VOLUME, Integer.parseInt(pair[0]));
          item.put(HistoryItemTable.HistoryItemColumns.PAGE, Integer.parseInt(pair[1]));
          item.put(HistoryItemTable.HistoryItemColumns.STATUS, 2);
          items.put(item);
        }
      }
      convertedObj.put(HistoryItemTable.TABLE_NAME, items);
      results.put(convertedObj);
    }
    return results;
  }

  private void importAppleData(String path) {
    try {
      JSONObject jsonObject = new JSONObject(unpackAppleImportData(path));
      if (jsonObject.has("version") && jsonObject.getInt("version") > 1) {
        JSONArray bookmarks = convertBookmarksAppleData(jsonObject.getJSONArray("bookmarks"));
        JSONArray histories = convertHistoriesAppleData(jsonObject.getJSONArray("histories"));
        mFavoriteDaoHelper.restoreJSONArray(bookmarks);
        mHistoryDaoHelper.restoreJSONArray(histories);
        mHandler.post(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(MainActivity.this, R.string.import_complete, Toast.LENGTH_SHORT).show();
          }
        });
      } else {
        mHandler.post(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(MainActivity.this, R.string.unsupported_old_version_file, Toast.LENGTH_SHORT).show();
          }
        });
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  private void importData(final String path) {
    final AlertDialog dialog = createBlockingProgressDialog(R.string.importing_data);
    dialog.show();
    new Thread(new Runnable() {
      @Override
      public void run() {
        if (!(new File(path).exists())) {
          mHandler.post(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(MainActivity.this, R.string.file_not_found, Toast.LENGTH_SHORT).show();
            }
          });
        } else if (path.endsWith(".js")) {
          importAndroidData(path);
        } else if (path.endsWith(".json.etz")) {
          importAppleData(path);
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
    final AlertDialog dialog = createBlockingProgressDialog(R.string.exporting_data);
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

  private AlertDialog createBlockingProgressDialog(int messageResId) {
    int padding = (int) (16 * getResources().getDisplayMetrics().density);
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.HORIZONTAL);
    layout.setPadding(padding, padding, padding, padding);

    ProgressBar progressBar = new ProgressBar(this);
    layout.addView(progressBar);

    TextView message = new TextView(this);
    message.setText(messageResId);
    message.setPadding(padding, 0, 0, 0);
    layout.addView(message);

    return new AlertDialog.Builder(this)
        .setView(layout)
        .setCancelable(false)
        .create();
  }

  @Override
  public int getFontSize() {
    return getSharedPreferences(Constants.SETTING_PREFERENCES, Context.MODE_PRIVATE)
        .getInt(Constants.FONT_SIZE_KEY, Constants.DEFAULT_FONT_SIZE);
  }

  @Override
  public String getContent() {
    return getReaderFragment().getCurrentPageFragment().getContent();
  }

  @Override
  public void onDialogPositiveClick(DialogFragment dialog, int fontSize) {
    getReaderFragment().getCurrentPageFragment().setFontSize(fontSize);
  }

  @Override
  public void onDialogNegativeClick(DialogFragment dialog) {

  }
}
