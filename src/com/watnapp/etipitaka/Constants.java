package com.watnapp.etipitaka;

import android.net.Uri;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 29/5/2013
 * Time: 6:29
 */
public class Constants {
  public static final int MENU_ITEM_GOTO      = 1001;
  public static final int MENU_ITEM_GOTO_PAGE = 1002;
  public static final int MENU_ITEM_GOTO_ITEM = 1003;
  public static final int MENU_ITEM_SEARCH    = 1004;
  public static final int MENU_ITEM_COMPARE   = 1005;

  public static final int GOTO_PAGE_ID = 0;
  public static final int GOTO_ITEM_ID = 1;

  public static final int HISTORY_LOADER = 0;
  public static final Uri LANGUAGE_CHANGE_URI = Uri.parse("content://etipitaka.com/language_change");

  public static final String LANGUAGE_KEY = "language";
  public static final String VOLUME_KEY = "volume";
  public static final String ITEM_KEY = "item";
  public static final String KEYWORDS_KEY = "keywords";
  public static final String PAGE_KEY = "page";
  public static final String CONTENT_KEY = "content";
  public static final String NUMBER_KEY = "number";

}
