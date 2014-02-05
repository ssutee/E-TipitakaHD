package com.watnapp.etipitaka.plus.model;

import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;

/**
 * Created by sutee on 4/2/14.
 */
public class ETPaliSiamratDataModel extends ETSiamratDataModel {

  @Override
  public BookDatabaseHelper.Language getLanguage() {
    return BookDatabaseHelper.Language.PALI;
  }

  @Override
  public void search(String keywords, BookDatabaseHelper.OnSearchListener listener) {
    search(keywords, listener,
        new Integer[] {
            1,2,3,4,5,6,7,8,9,10,
            11,12,13,14,15,16,17,18,19,20,
            21,22,23,24,25,26,27,28,29,30,
            31,32,33,34,35,36,37,38,39,40,
            41,42,43,44,45
        }
    );
  }
}
