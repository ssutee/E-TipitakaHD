package com.watnapp.etipitaka.plus.model;

import androidx.annotation.Nullable;

import com.watnapp.etipitaka.plus.Constants;

import dart.BindExtra;
import dart.DartModel;

@DartModel
public class ComparisonActivityNavigationModel {
    @BindExtra(Constants.LANGUAGE_KEY)
    public int mLanguageCode;

    @BindExtra(Constants.COMPARING_LANGUAGE_KEY)
    public int mComparingLanguageCode;

    @BindExtra(Constants.VOLUME_KEY)
    public int mVolume;

    @BindExtra(Constants.ITEM_KEY)
    public int mItem;

    @BindExtra(Constants.SECTION_KEY)
    public int mSection;

    @BindExtra(Constants.KEYWORDS_KEY)
    public String mKeywords;

    @BindExtra(Constants.BUDDHAWAJ_KEY)
    public boolean mIsBuddhawaj;

    @BindExtra(Constants.PAGE_KEY)
    public int mPage;

    @BindExtra(Constants.COMPARING_VOLUME_KEY)
    @Nullable
    public int mComparingVolume = 0;
}
