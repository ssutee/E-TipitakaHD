package com.watnapp.etipitaka.plus.model;

import android.content.Context;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;

/**
 * Created by sutee on 19/2/19.
 */
public class ETPaliSiamratNewDataModel extends ETPaliSiamratDataModel {

  @Override
  public String pageFormat(int page) {
    return String.format("%d", page);
  }

  @Override
  public String volumeFormat(int volume) {
    return String.format("%d", volume);
  }

  public ETPaliSiamratNewDataModel(Context context) {
    super(context);
  }

  @Override
  protected String getDatabasePath() {
    return Utils.getDatabasePath(mContext, BookDatabaseHelper.Language.PALINEW);
  }

  @Override
  public BookDatabaseHelper.Language getLanguage() {
    return BookDatabaseHelper.Language.PALINEW;
  }

  @Override
  public String getShortTitle() {
    return mContext.getString(R.string.palinew_short_name);
  }

  @Override
  public boolean hasHtmlContent() {
    return true;
  }

}
