package com.watnapp.etipitaka.plus;

import android.net.Uri;
import android.os.Environment;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 29/5/2013
 * Time: 6:29
 */
public class Constants {
  public static final int MENU_ITEM_GOTO_PAGE           = 1001;
  public static final int MENU_ITEM_GOTO_ITEM           = 1002;
  public static final int MENU_ITEM_SEARCH              = 1003;
  public static final int MENU_ITEM_COMPARE             = 1004;
  public static final int MENU_ITEM_SAVE                = 1005;
  public static final int MENU_ITEM_INCREASE_FONT_SIZE  = 1006;
  public static final int MENU_ITEM_DECREASE_FONT_SIZE  = 1007;
  public static final int MENU_ITEM_OPEN                = 1008;
  public static final int MENU_ITEM_DELETE              = 1009;
  public static final int MENU_ITEM_IMPORT_DATA         = 1010;
  public static final int MENU_ITEM_EXPORT_DATA         = 1011;

  public static final int GOTO_PAGE_ID      = 0;
  public static final int GOTO_ITEM_ID      = 1;
  public static final int TAKE_NOTE_ID      = 2;
  public static final int OPEN_NOTE_ID      = 3;

  public static final int HISTORY_LOADER    = 0;
  public static final int FAVORITE_LOADER   = 1;

  public static final Uri LANGUAGE_CHANGE_URI = Uri.parse("content://etipitaka.com/language_change");
  public static final String DATABASE_URL     = "http://download.watnapahpong.org/data/etipitaka/android/etipitaka_plus.db.zip";
  public static final String CHECK_SUM_DB_ZIP = "16510601dfc383e45cec485e0f5835fa";
  public static final String CHECK_SUM_DB     = "2d093a4b06517efb7689a4d6b0135ec0";

  public static final String DATABASE_FOLDER    = Environment.getExternalStorageDirectory().getPath() + "/ETPK";
  public static final String DATABASE_PATH      = DATABASE_FOLDER + "/etipitaka_plus.db";
  public static final String DATABASE_ZIP_PATH  = DATABASE_FOLDER + "/etipitaka_plus.db.zip";
  public static final long DATABASE_SIZE        = 158919680;

  public static final String LANGUAGE_KEY     = "language";
  public static final String VOLUME_KEY       = "volume";
  public static final String ITEM_KEY         = "item";
  public static final String SECTION_KEY      = "section";
  public static final String KEYWORDS_KEY     = "keywords";
  public static final String PAGE_KEY         = "page";
  public static final String CONTENT_KEY      = "content";
  public static final String NUMBER_KEY       = "number";
  public static final String BUTTON_KEY       = "button";
  public static final String FONT_SIZE_KEY    = "font_size";
  public static final String TITLE_KEY        = "title";
  public static final String SELECT_MODE_KEY  = "select_mode";
  public static final String PATH_KEY         = "path";

  public static final int DEFAULT_FONT_SIZE   = 20;
  public static final int FONT_SIZE_STEP      = 2;

  public static final int SELECT_MODE_FILE    = 1;
  public static final int SELECT_MODE_FOLDER  = 2;

  public static final String SETTING_PREFERENCES = "setting_preferences";
}
