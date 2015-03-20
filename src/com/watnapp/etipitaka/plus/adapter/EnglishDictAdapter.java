package com.watnapp.etipitaka.plus.adapter;

import android.content.Context;
import android.widget.TextView;
import com.google.inject.Inject;

/**
 * Created by sutee on 20/3/58.
 */
public class EnglishDictAdapter extends DictAdapter {

  @Inject
  public EnglishDictAdapter(Context context) {
    super(context);
  }

  @Override
  public String getHeadWordColumn() {
    return "head";
  }

  @Override
  public void setTextViewFontStyle(Context context, TextView textView) {
    textView.setTextSize(18);
  }
}
