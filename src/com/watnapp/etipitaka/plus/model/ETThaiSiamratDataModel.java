package com.watnapp.etipitaka.plus.model;

import android.content.Context;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;

/**
 * Created by sutee on 4/2/14.
 */

public class ETThaiSiamratDataModel extends ETSiamratDataModel {
  public ETThaiSiamratDataModel(Context context) {
    super(context);
  }

  @Override
  public BookDatabaseHelper.Language getLanguage() {
    return BookDatabaseHelper.Language.THAI;
  }


}
