package com.watnapp.etipitaka.plus.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.fragment.ReaderFragment;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper.Language;
import com.watnapp.etipitaka.plus.model.ComparisonActivityNavigationModel;
import com.watnapp.etipitaka.plus.model.ETDataModel;
import com.watnapp.etipitaka.plus.model.ETDataModelCreator;

import dart.Dart;
import dart.DartModel;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 24/6/2013
 * Time: 22:20
 */

public class ComparisonActivity extends AppCompatActivity
    implements ReaderFragment.OnMenuButtonClickListener {

  protected static final String TAG = "ComparisonActivity";

  private Handler mHandler = new Handler();
  private Language mLanguage1;
  private Language mLanguage2;
  private ReaderFragment mLeftFragment;
  private ReaderFragment mRightFragment;
  private ETDataModel mDataModel1;
  private ETDataModel mDataModel2;
  @DartModel ComparisonActivityNavigationModel navigationModel;


  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Dart.bind(this);
    ComparisonActivityContentBridge.render(this);

    mLanguage1 = Language.values()[navigationModel.mLanguageCode];
    mLanguage2 = Language.values()[navigationModel.mComparingLanguageCode];

    mDataModel1 = ETDataModelCreator.create(mLanguage1, this);
    mDataModel2 = ETDataModelCreator.create(mLanguage2, this);

    mDataModel1.convertToPivot(navigationModel.mVolume, navigationModel.mPage,
            navigationModel.mItem, new BookDatabaseHelper.OnConvertToPivotListener() {
      @Override
      public void onConvertToPivotFinish(int volume, int item, int section) {
        mDataModel2.convertFromPivot(navigationModel.mComparingVolume == 0 ? volume : navigationModel.mComparingVolume, item, section,
            new BookDatabaseHelper.OnConvertFromPivotListener() {
          @Override
          public void onConvertFromPivotFinish(final int volume, final int page) {

            mHandler.post(new Runnable() {
              @Override
              public void run() {
                mLeftFragment = ReaderFragment.newInstance(
                        mLanguage1, navigationModel.mVolume, navigationModel.mPage,
                        navigationModel.mKeywords, navigationModel.mIsBuddhawaj, true);
                getSupportFragmentManager().beginTransaction()
                    .add(R.id.left_reader_fragment,
                        mLeftFragment, "left").commit();

                Log.d(TAG, "volume = " + volume);
                Log.d(TAG, "page = " + page);

                mRightFragment = ReaderFragment.newInstance(mLanguage2, volume, page, "", navigationModel.mIsBuddhawaj, true);
                getSupportFragmentManager().beginTransaction()
                    .add(R.id.right_reader_fragment,
                        mRightFragment, "right").commit();

                if (page > 0) {
                  mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                      mRightFragment.getPageFragment(mRightFragment.getCurrentPage()).scrollToItem(navigationModel.mItem);
                    }
                  }, 500);
                } else {
                  finish();
                }

              }
            });
          }
        });
      }
    });

  }

  @Override
  protected void onDestroy() {
    mDataModel1.closeDatabase();
    mDataModel2.closeDatabase();
    super.onDestroy();
  }

  @Override
  public void onCompareButtonClick(final Language language, final int volume, final int page) {
    final ETDataModel sourceModel = language == mDataModel1.getLanguage() ? mDataModel1 : mDataModel2;
    final ETDataModel targetModel = language == mDataModel1.getLanguage() ? mDataModel2 : mDataModel1;

    if (sourceModel.getLanguage() == Language.THAIBT || targetModel.getLanguage() == Language.THAIBT ||
        sourceModel.getLanguage() == Language.THAIPB || targetModel.getLanguage() == Language.THAIPB) {
      return;
    }

    sourceModel.getComparingItemsAtPage(volume, page, new BookDatabaseHelper.OnGetItemsListener() {
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
                  public void onClick(DialogInterface dialog, final int which) {
                    final Language targetLanguage = (language.getCode() == navigationModel.mLanguageCode)
                        ? mLanguage2 : mLanguage1;
                    final ReaderFragment targetFragment = (language.getCode() == navigationModel.mLanguageCode)
                        ? mRightFragment : mLeftFragment;

                    sourceModel.convertToPivot(volume, page, items[which], new BookDatabaseHelper.OnConvertToPivotListener() {
                      @Override
                      public void onConvertToPivotFinish(int aVolume, int aItem, int aSection) {
                        targetModel.convertFromPivot(aVolume, aItem, aSection, new BookDatabaseHelper.OnConvertFromPivotListener() {
                          @Override
                          public void onConvertFromPivotFinish(final int aVolume, final int aPage) {
                            mHandler.post(new Runnable() {
                              @Override
                              public void run() {
                                targetFragment.openBook(targetLanguage, aVolume, aPage);
                                targetFragment.getCurrentPageFragment().scrollToItem(items[which]);
                              }
                            });
                          }
                        });
                      }
                    });
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

    Log.d(TAG, language.toString() + ":" + volume + ":" + page);

    setResult(RESULT_OK, data);
    finish();
  }
}
