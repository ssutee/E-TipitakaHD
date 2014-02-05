package com.watnapp.etipitaka.plus.model;

import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;

/**
 * Created by sutee on 4/2/14.
 */

public class ETDataModelCreator {
  public static ETDataModel create(BookDatabaseHelper.Language language) {
    switch (language) {
      case PALI:
        return new ETPaliSiamratDataModel();
      case THAI:
        return new ETThaiSiamratDataModel();
    }
    return null;
  }
}
