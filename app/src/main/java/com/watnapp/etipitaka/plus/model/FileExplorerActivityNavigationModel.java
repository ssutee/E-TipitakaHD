package com.watnapp.etipitaka.plus.model;

import com.watnapp.etipitaka.plus.Constants;

import dart.BindExtra;
import dart.DartModel;

@DartModel
public class FileExplorerActivityNavigationModel {
    @BindExtra(Constants.TITLE_KEY)
    public String mTitle;

    @BindExtra(Constants.SELECT_MODE_KEY)
    public int mSelectMode;

}
