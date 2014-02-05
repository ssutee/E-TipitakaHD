package com.watnapp.etipitaka.plus.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.google.inject.Inject;
import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.fragment.ReaderFragment;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper.Language;
import com.watnapp.etipitaka.plus.model.ETDataModel;
import com.watnapp.etipitaka.plus.model.ETDataModelCreator;
import roboguice.inject.ContentView;
import roboguice.inject.InjectExtra;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 24/6/2013
 * Time: 22:20
 */

@ContentView(R.layout.activity_comparison)
public class ComparisonActivity extends RoboSherlockFragmentActivity
    implements ReaderFragment.OnMenuButtonClickListener {

  protected static final String TAG = "ComparisonActivity";

  @InjectExtra(Constants.LANGUAGE_KEY)
  private int mLanguageCode;

  @InjectExtra(Constants.COMPARING_LANGUAGE_KEY)
  private int mComparingLanguageCode;

  @InjectExtra(Constants.VOLUME_KEY)
  private int mVolume;

  @InjectExtra(Constants.ITEM_KEY)
  private int mItem;

  @InjectExtra(Constants.SECTION_KEY)
  private int mSection;

  @InjectExtra(Constants.KEYWORDS_KEY)
  private String mKeywords;

  @InjectExtra(Constants.PAGE_KEY)
  private int mPage;

  @Inject
  private BookDatabaseHelper mBookDatabaseHelper;

  private Handler mHandler = new Handler();
  private BookDatabaseHelper.Language mLanguage1;
  private BookDatabaseHelper.Language mLanguage2;
  private ReaderFragment mLeftFragment;
  private ReaderFragment mRightFragment;
  private ETDataModel mDataModel1;
  private ETDataModel mDataModel2;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    int page1 = mPage;
    mLanguage1 = Language.values()[mLanguageCode];
    mLanguage2 = Language.values()[mComparingLanguageCode];

    mDataModel1 = ETDataModelCreator.create(mLanguage1);
    mDataModel2 = ETDataModelCreator.create(mLanguage2);

    int page2 = mDataModel2.getPageById(mDataModel2.getPageIdByItem(mVolume, mItem, mSection));

    mLeftFragment = ReaderFragment.newInstance(mLanguage1, mVolume, page1, mKeywords, true);
    getSupportFragmentManager().beginTransaction()
        .add(R.id.left_reader_fragment,
            mLeftFragment, "left").commit();

    mRightFragment = ReaderFragment.newInstance(mLanguage2, mVolume, page2, "", true);
    getSupportFragmentManager().beginTransaction()
        .add(R.id.right_reader_fragment,
            mRightFragment, "right").commit();

    mHandler.postDelayed(new Runnable() {
      @Override
      public void run() {
        mRightFragment.getPageFragment(mRightFragment.getCurrentPage()).scrollToItem(mItem);
      }
    }, 500);

  }

  @Override
  protected void onDestroy() {
    mDataModel1.closeDatabase();
    mDataModel2.closeDatabase();
    super.onDestroy();
  }

  @Override
  public void onCompareButtonClick(final Language language, final int volume, int page) {
    final ETDataModel sourceModel = language == mDataModel1.getLanguage() ? mDataModel1 : mDataModel2;
    final ETDataModel targetModel = language == mDataModel1.getLanguage() ? mDataModel2 : mDataModel1;
    sourceModel.getItemsAtPage(volume, page, new BookDatabaseHelper.OnGetItemsListener() {
      @Override
      public void onGetItemsFinish(final Integer[] items, final Integer[] sections) {
        mHandler.post(new Runnable() {
          @Override
          public void run() {
            String[] choices = new String[items.length];
            for (int i=0; i<items.length; ++i) {
              choices[i] = getString(R.string.go_to_item) + " " +
                  Utils.convertToThaiNumber(ComparisonActivity.this, items[i]);
            }
            new AlertDialog.Builder(ComparisonActivity.this).setTitle(R.string.select_item)
                .setItems(choices, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    Language targetLanguage = (language.getCode() == mLanguageCode)
                        ? mLanguage2 : mLanguage1;
                    ReaderFragment targetFragment = (language.getCode() == mLanguageCode)
                        ? mRightFragment : mLeftFragment;
                    int pageId = targetModel.getPageIdByItem(volume, items[which], sections[which]);
                    targetFragment.openBook(targetLanguage, volume, mBookDatabaseHelper.getPageById(pageId));
                    targetFragment.getCurrentPageFragment().scrollToItem(items[which]);
                  }
                }).create().show();
          }
        });
      }
    });
  }

  @Override
  public void onReturnButtonClick(Language language, int volume, int page) {
    Intent data = new Intent();
    data.putExtra(Constants.LANGUAGE_KEY, language.getCode());
    data.putExtra(Constants.VOLUME_KEY, volume);
    data.putExtra(Constants.PAGE_KEY, page);
    setResult(RESULT_OK, data);
    finish();
  }
}