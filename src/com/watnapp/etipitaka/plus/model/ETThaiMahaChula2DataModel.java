package com.watnapp.etipitaka.plus.model;

import android.content.Context;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;

/**
 * Created by sutee on 19/2/19.
 */
public class ETThaiMahaChula2DataModel extends ETThaiMahaChulaDataModel {

  public ETThaiMahaChula2DataModel(Context context) {
    super(context);
  }

  @Override
  public String pageFormat(int page) {
    return String.format("%d", page);
  }

  @Override
  public String volumeFormat(int volume) {
    return String.format("%d", volume);
  }

  @Override
  protected String getDatabasePath() {
    return Utils.getDatabasePath(BookDatabaseHelper.Language.THAIMC2);
  }

  @Override
  public BookDatabaseHelper.Language getLanguage() {
    return BookDatabaseHelper.Language.THAIMC;
  }

  @Override
  public boolean hasHtmlContent() {
    return true;
  }

  @Override
  public boolean hasFooter() {
    return false;
  }

  @Override
  public String getShortTitle() {
    return mContext.getString(R.string.thaimc2_short_name);
  }
}
