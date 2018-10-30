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
  public static final int MENU_ITEM_PALI_DICT           = 1012;
  public static final int MENU_ITEM_ADJUST_FONT_SIZE    = 1013;
  public static final int MENU_ITEM_MANAGE_DATA         = 1014;
  public static final int MENU_ITEM_BLACK_COLOR         = 1015;
  public static final int MENU_ITEM_WHITE_COLOR         = 1016;
  public static final int MENU_ITEM_SEPIA_COLOR         = 1017;
  public static final int MENU_ITEM_EDIT                = 1018;
  public static final int MENU_ITEM_SORT                = 1019;
  public static final int MENU_ITEM_MARK                = 1020;
  public static final int MENU_ITEM_THAI_DICT           = 1021;
  public static final int MENU_ITEM_ENG_DICT            = 1022;


  public static final int GOTO_PAGE_ID      = 0;
  public static final int GOTO_ITEM_ID      = 1;
  public static final int TAKE_NOTE_ID      = 2;
  public static final int OPEN_NOTE_ID      = 3;
  public static final int EDIT_NOTE_ID      = 4;

  public static final int HISTORY_LOADER    = 0;
  public static final int FAVORITE_LOADER   = 1;

  public static final Uri LANGUAGE_CHANGE_URI = Uri.parse("content://etipitaka.com/language_change");
  public static final Uri RESET_PAGE_URI = Uri.parse("content://etipitaka.com/reset_page");

  public static final String S3_HOST = "https://s3.amazonaws.com/watnapahpong/android";
  public static final String THAI_HOST = "http://download.watnapahpong.org/data/etipitaka/android";
  public static final String UPDATE_URL = "http://etipitaka.com/update/android.json";

  public static final String LANGUAGE_KEY           = "language";
  public static final String COMPARING_LANGUAGE_KEY = "comparing_language";
  public static final String COMPARING_VOLUME_KEY   = "comparing_volume";
  public static final String VOLUME_KEY             = "volume";
  public static final String ITEM_KEY               = "item";
  public static final String SECTION_KEY            = "section";
  public static final String KEYWORDS_KEY           = "keywords";
  public static final String BUDDHAWAJ_KEY          = "buddhawaj";
  public static final String PAGE_KEY               = "page";
  public static final String CONTENT_KEY            = "content";
  public static final String HTML_CONTENT_KEY       = "html_content";
  public static final String FOOTER_KEY             = "footer";
  public static final String NUMBER_KEY             = "number";
  public static final String BUTTON_KEY             = "button";
  public static final String FONT_SIZE_KEY          = "font_size";
  public static final String TITLE_KEY              = "title";
  public static final String SELECT_MODE_KEY        = "select_mode";
  public static final String PATH_KEY               = "path";
  public static final String FONT_COLOR_KEY         = "font_color";
  public static final String BACKGROUND_COLOR_KEY   = "background_color";

  public static final int DEFAULT_FONT_SIZE   = 20;
  public static final int FONT_SIZE_STEP      = 2;

  public static final int SELECT_MODE_FILE    = 1;
  public static final int SELECT_MODE_FOLDER  = 2;

  public static final String SETTING_PREFERENCES = "setting_preferences";


  public static final String DEFAULT_FONT_COLOR       = "#010101";
  public static final String DEFAULT_BACKGROUND_COLOR = "#FEFEFE";

  public static final String LANGUAGE_TITLES[] = {"ภาษาไทยฉบับหลวง", "ภาษาบาลีฉบับสยามรัฐ", "ภาษาไทยฉบับมหามกุฏฯ", "ภาษาไทยฉบับมหาจุฬาฯ", "พุทธวจนปิฎก ๓๓ เล่ม"};
  public static final String REFS_PATTERN = "([๐๑๒๓๔๕๖๗๘๙][–๐๑๒๓๔๕๖๗๘๙\\s\\-,]+)/([–๐๑๒๓๔๕๖๗๘๙\\s\\-,]+)/([–๐๑๒๓๔๕๖๗๘๙\\s\\-,]+[๐๑๒๓๔๕๖๗๘๙])";

  public static final String FAV_SORTING_KEY = "fav_sorting_key";
  public static final String FAV_ORDERING_KEY = "fav_ordering_key";
  public static final String HIS_SORTING_KEY = "his_sorting_key";
  public static final String HIS_ORDERING_KEY = "his_ordering_key";

}
