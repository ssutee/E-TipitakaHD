package com.watnapp.etipitaka.plus.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 15/7/2013
 * Time: 14:27
 */

public class PaliDictAdapter extends DictAdapter {

  public PaliDictAdapter(Context context) {
    super(context);
  }

  @Override
  public String getHeadWordColumn() {
    return "headword";
  }

  @Override
  public void setTextViewFontStyle(Context context, TextView textView) {
    Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/THSarabun.ttf");
    textView.setTypeface(font);
    textView.setTextSize(26);
  }

}
