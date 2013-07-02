package com.watnapp.etipitaka;

import android.app.Application;
import com.watnapp.etipitaka.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.model.History;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 27/5/2013
 * Time: 14:39

 */
public class E_TipitakaApplication extends Application {

  private BookDatabaseHelper.Language language = BookDatabaseHelper.Language.THAI;
  private History history;

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
}
