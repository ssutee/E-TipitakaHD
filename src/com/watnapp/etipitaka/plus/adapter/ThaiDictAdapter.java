package com.watnapp.etipitaka.plus.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;
import com.google.inject.Inject;

/**
 * Created by sutee on 20/3/58.
 */
public class ThaiDictAdapter extends DictAdapter {

  @Inject
  public ThaiDictAdapter(Context context) {
    super(context);
  }

  @Override
  public String getHeadWordColumn() {
    return "head";
  }

  @Override
  public void setTextViewFontStyle(Context context, TextView textView) {
    Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/THSarabun.ttf");
    textView.setTypeface(font);
    textView.setTextSize(26);
  }
}
