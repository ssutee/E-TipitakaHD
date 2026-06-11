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
import com.watnapp.etipitaka.plus.helper.DownloadDatabaseKt;

import java.io.File;

import dart.Dart;
import dart.DartModel;
import kotlin.Unit;

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
                // convertToPivot/convertFromPivot run on a background thread,
                // so this can be posted (or still be queued) after the user
                // backed out — committing a transaction then throws
                // "FragmentManager has been destroyed".
                if (isFinishing() || isDestroyed()) {
                  return;
                }
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
                  scrollRightFragmentToItem(navigationModel.mItem, 0);
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

  private static final int SCROLL_MAX_TRIES = 40;
  private static final long SCROLL_RETRY_MS = 50L;

  /**
   * Scroll the right ReaderFragment to {@code item} once its view exists.
   * beginTransaction().commit() is async, so right after committing the
   * fragment its view (and binding) does not exist yet — a fixed delay
   * raced the fragment's onCreateView on slow devices and NPE'd inside
   * ReaderFragment.getCurrentPage. Poll for view readiness instead;
   * onViewCreated (which positions the pager via openBook) runs in the
   * same transaction pass as onCreateView, so a non-null getView() means
   * getCurrentPage is safe to call.
   */
  private void scrollRightFragmentToItem(final int item, final int tries) {
    if (isFinishing() || isDestroyed() || mRightFragment == null) {
      return;
    }
    if (mRightFragment.getView() == null) {
      if (tries >= SCROLL_MAX_TRIES) {
        Log.w(TAG, "scrollRightFragmentToItem: right fragment view never appeared");
        return;
      }
      mHandler.postDelayed(() -> scrollRightFragmentToItem(item, tries + 1), SCROLL_RETRY_MS);
      return;
    }
    mRightFragment.scrollToItemAtPage(mRightFragment.getCurrentPage(), item);
  }

  @Override
  protected void onDestroy() {
    // Drop any queued fragment-commit / scroll runnables so they cannot
    // run against a destroyed activity.
    mHandler.removeCallbacksAndMessages(null);
    mDataModel1.closeDatabase();
    mDataModel2.closeDatabase();
    super.onDestroy();
  }

  @Override
  public void onCompareButtonClick(final Language language, final int volume, final int page) {
    final ETDataModel sourceModel = language == mDataModel1.getLanguage() ? mDataModel1 : mDataModel2;

    if (sourceModel.getLanguage() == Language.THAIBT || sourceModel.getLanguage() == Language.THAIPB) {
      return;
    }

    new AlertDialog.Builder(this).setTitle(R.string.select_langauge)
        .setItems(Constants.COMPARE_TITLES, (dialog, which) -> {
          final Language targetLanguage = Constants.COMPARE_LANGUAGES[which];
          if (targetLanguage == sourceModel.getLanguage()) {
            return;
          }
          String dbPath = Utils.getDatabasePath(ComparisonActivity.this, targetLanguage);
          if (new File(dbPath).exists()) {
            proceedCompare(sourceModel, language, targetLanguage, volume, page);
          } else {
            // Target DB missing — offer download, then resume on success.
            DownloadDatabaseKt.confirmAndDownloadDatabase(
                ComparisonActivity.this, targetLanguage, () -> {
                  if (!isFinishing() && !isDestroyed()) {
                    proceedCompare(sourceModel, language, targetLanguage, volume, page);
                  }
                  return Unit.INSTANCE;
                });
          }
        }).create().show();
  }

  /**
   * Fetch the comparable items at (volume, page) from sourceModel and present
   * the item picker / launch. Shared by the normal path and the
   * post-download continuation so the resume behaves identically.
   */
  private void proceedCompare(final ETDataModel sourceModel, final Language sourceLanguage,
                              final Language targetLanguage, final int volume, final int page) {
    sourceModel.getComparingItemsAtPage(volume, page, (items, sections) ->
        mHandler.post(() -> pickItemAndLaunch(sourceLanguage, targetLanguage, volume, page, items, sections)));
  }

  private void pickItemAndLaunch(final Language sourceLanguage, final Language targetLanguage,
                                 final int volume, final int page,
                                 final Integer[] items, final Integer[] sections) {
    if (items == null || items.length == 0) {
      return;
    }
    if (items.length == 1) {
      launchComparison(sourceLanguage, targetLanguage, volume, page, items[0], sections[0]);
      return;
    }
    String[] choices = new String[items.length];
    for (int i = 0; i < items.length; ++i) {
      choices[i] = getString(R.string.go_to_item) + " " +
          Utils.convertToThaiNumber(ComparisonActivity.this, items[i]);
    }
    new AlertDialog.Builder(this).setTitle(R.string.select_item)
        .setItems(choices, (dialog, which) ->
            launchComparison(sourceLanguage, targetLanguage, volume, page,
                items[which], sections[which]))
        .create().show();
  }

  private void launchComparison(Language sourceLanguage, Language targetLanguage,
                                int volume, int page, int item, int section) {
    Intent intent = new Intent(this, ComparisonActivity.class);
    intent.putExtra(Constants.LANGUAGE_KEY, sourceLanguage.getCode());
    intent.putExtra(Constants.COMPARING_LANGUAGE_KEY, targetLanguage.getCode());
    intent.putExtra(Constants.VOLUME_KEY, volume);
    intent.putExtra(Constants.PAGE_KEY, page);
    intent.putExtra(Constants.ITEM_KEY, item);
    intent.putExtra(Constants.SECTION_KEY, section);
    intent.putExtra(Constants.KEYWORDS_KEY, "");
    intent.putExtra(Constants.BUDDHAWAJ_KEY, navigationModel.mIsBuddhawaj);
    startActivity(intent);
    finish();
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
