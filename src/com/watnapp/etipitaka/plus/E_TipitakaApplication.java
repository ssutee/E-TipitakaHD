package com.watnapp.etipitaka.plus;

import android.app.Application;
import android.content.Context;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.plus.model.History;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 27/5/2013
 * Time: 14:39

 */
public class E_TipitakaApplication extends Application {

  private BookDatabaseHelper.Language language = BookDatabaseHelper.Language.THAI;
  private History history;
  private static Context context;

  public BookDatabaseHelper.Language getLanguage() {
    return language;
  }

  public void setLanguage(BookDatabaseHelper.Language language) {
    this.language = language;
  }

  public History getHistory() {
    return history;
  }

  public void setHistory(History history) {
    this.history = history;
  }

  public void onCreate(){
    super.onCreate();
    E_TipitakaApplication.context = getApplicationContext();
  }

  public static Context getAppContext() {
    return ETipitakaApplication.context;
  }

}
