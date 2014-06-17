package com.watnapp.etipitaka.plus.model;
import android.provider.BaseColumns;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.SparseBooleanArray;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;

public class History extends ModelBase {
  private Context context;
  private int id;
  private String keywords;
  private int language;
  private boolean section1;
  private boolean section2;
  private boolean section3;
  private int result1;
  private int result2;
  private int result3;
  private String content;
  private int score;

  public History() {
    super();
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public void setId(int id) {
    this.id = id;
  }

  public String getKeywords() {
    return keywords;
  }

  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }

  public BookDatabaseHelper.Language getLanguage() {
    return BookDatabaseHelper.Language.values()[language];
  }

  public void setLanguage(BookDatabaseHelper.Language language) {
    this.language = language.ordinal();
  }

  public boolean isSection1() {
    return section1;
  }

  public void setSection1(boolean section1) {
    this.section1 = section1;
  }

  public boolean isSection2() {
    return section2;
  }

  public void setSection2(boolean section2) {
    this.section2 = section2;
  }

  public boolean isSection3() {
    return section3;
  }

  public void setSection3(boolean section3) {
    this.section3 = section3;
  }

  public void setSections(SparseBooleanArray sections) {
    setSection1(sections != null && sections.get(0, false));
    setSection2(sections != null && sections.get(1, false));
    setSection3(sections != null && sections.get(2, false));
  }

  public void setResults(int[] results) {
    setResult1(results.length > 0 ? results[0] : 0);
    setResult2(results.length > 1 ? results[1] : 0);
    setResult3(results.length > 2 ? results[2] : 0);
  }

  public int getResult1() {
    return result1;
  }

  public void setResult1(int result1) {
    this.result1 = result1;
  }

  public int getResult2() {
    return result2;
  }

  public void setResult2(int result2) {
    this.result2 = result2;
  }

  public int getResult3() {
    return result3;
  }

  public void setResult3(int result3) {
    this.result3 = result3;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

  @Override
  public void fromCursor(Cursor cursor, Context context) {
    this.id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
    this.keywords = cursor.getString(cursor.getColumnIndex(HistoryTable.HistoryColumns.KEYWORDS));
    this.language = cursor.getInt(cursor.getColumnIndex(HistoryTable.HistoryColumns.LANGUAGE));
    this.section1 = cursor.getInt(cursor.getColumnIndex(HistoryTable.HistoryColumns.SECTION1)) == 1;
    this.section2 = cursor.getInt(cursor.getColumnIndex(HistoryTable.HistoryColumns.SECTION2)) == 1;
    this.section3 = cursor.getInt(cursor.getColumnIndex(HistoryTable.HistoryColumns.SECTION3)) == 1;
    this.result1 = cursor.getInt(cursor.getColumnIndex(HistoryTable.HistoryColumns.RESULT1));
    this.result2 = cursor.getInt(cursor.getColumnIndex(HistoryTable.HistoryColumns.RESULT2));
    this.result3 = cursor.getInt(cursor.getColumnIndex(HistoryTable.HistoryColumns.RESULT3));
    this.score = cursor.getInt(cursor.getColumnIndex(HistoryTable.HistoryColumns.SCORE));
    this.content = cursor.getString(cursor.getColumnIndex(HistoryTable.HistoryColumns.CONTENT));
    this.context = context;
  }

  @Override
  public ContentValues toContentValues() {
    ContentValues values = new ContentValues();
    values.put(HistoryTable.HistoryColumns.KEYWORDS, this.keywords);
    values.put(HistoryTable.HistoryColumns.LANGUAGE, this.language);
    values.put(HistoryTable.HistoryColumns.SECTION1, this.section1);
    values.put(HistoryTable.HistoryColumns.SECTION2, this.section2);
    values.put(HistoryTable.HistoryColumns.SECTION3, this.section3);
    values.put(HistoryTable.HistoryColumns.RESULT1, this.result1);
    values.put(HistoryTable.HistoryColumns.RESULT2, this.result2);
    values.put(HistoryTable.HistoryColumns.RESULT3, this.result3);
    values.put(HistoryTable.HistoryColumns.SCORE, this.score);
    values.put(HistoryTable.HistoryColumns.CONTENT, this.content);
    return values;
  }

  public static History newInstance(Cursor cursor, Context context) {
    History history = new History();
    history.fromCursor(cursor, context);
    return history;
  }


}