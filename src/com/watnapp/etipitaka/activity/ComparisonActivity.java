package com.watnapp.etipitaka.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;
import com.watnapp.etipitaka.Constants;
import com.watnapp.etipitaka.R;
import com.watnapp.etipitaka.fragment.ReaderFragment;
import com.watnapp.etipitaka.helper.BookDatabaseHelper;
import roboguice.inject.ContentView;
import roboguice.inject.InjectExtra;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 24/6/2013
 * Time: 22:20
 */

@ContentView(R.layout.activity_comparison)
public class ComparisonActivity extends RoboSherlockFragmentActivity {

  protected static final String TAG = "ComparisonActivity";

  @InjectExtra(Constants.LANGUAGE_KEY)
  private int mLanguage;

  @InjectExtra(Constants.VOLUME_KEY)
  private int mVolume;

  @InjectExtra(Constants.ITEM_KEY)
  private int mItem;

  @InjectExtra(Constants.SECTION_KEY)
  private int mSection;

  @Inject
  private BookDatabaseHelper mBookDatabaseHelper;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    int page1 = mBookDatabaseHelper.getPageById(
        mBookDatabaseHelper.getPageIdByItem(BookDatabaseHelper.Language.THAI, mVolume, mItem, mSection));
    int page2 = mBookDatabaseHelper.getPageById(
        mBookDatabaseHelper.getPageIdByItem(BookDatabaseHelper.Language.PALI, mVolume, mItem, mSection));

    BookDatabaseHelper.Language language1 = BookDatabaseHelper.Language.THAI;
    BookDatabaseHelper.Language language2 = BookDatabaseHelper.Language.PALI;
    if (mLanguage == BookDatabaseHelper.Language.PALI.getCode()) {
      int tmp = page2;
      page2 = page1;
      page1 = tmp;
      language1 = BookDatabaseHelper.Language.PALI;
      language2 = BookDatabaseHelper.Language.THAI;
    }

    getSupportFragmentManager().beginTransaction()
        .add(R.id.left_reader_fragment,
            ReaderFragment.newInstance(language1, mVolume, page1, ""), "left").commit();

    getSupportFragmentManager().beginTransaction()
        .add(R.id.right_reader_fragment,
            ReaderFragment.newInstance(language2, mVolume, page2, ""), "right").commit();

  }
}