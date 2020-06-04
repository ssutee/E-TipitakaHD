package com.watnapp.etipitaka.plus.model;

import android.content.Context;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;

/**
 * Created by sutee on 4/2/14.
 */

public class ETDataModelCreator {
  public static ETDataModel create(BookDatabaseHelper.Language language, Context context) {
    switch (language) {
      case PALI:
        return new ETPaliSiamratDataModel(context);
      case THAI:
        return new ETThaiSiamratDataModel(context);
      case THAIMM:
        return new ETThaiMahaMakutDataModel(context);
      case THAIMC:
        return new ETThaiMahaChulaDataModel(context);
      case THAIBT:
        return new ETThaiFiveBooksDataModel(context);
      case THAIWN:
        return new ETThaiWatnaDataModel(context);
      case THAIPB:
        return new ETThaiPocketBookDataModel(context);
      case ROMANCT:
        return new ETRomanScriptDataModel(context);
      case THAIVN:
        return new ETThaiVinayaDataModel(context);
      case PALINEW:
        return new ETPaliSiamratNewDataModel(context);
      case THAIMC2:
        return new ETThaiMahaChula2DataModel(context);
      case THAIMS:
        return new ETThaiSupremeDataModel(context);
    }
    return null;
  }
}
