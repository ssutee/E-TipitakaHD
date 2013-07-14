package com.watnapp.etipitaka.plus.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 10/7/2013
 * Time: 13:29
 */
public class CheckableLinearLayout extends LinearLayout implements Checkable {

  private boolean mChecked;

  public CheckableLinearLayout(Context context, AttributeSet attrs,
                               int defStyle) {
    super(context, attrs, defStyle);
  }

  public CheckableLinearLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CheckableLinearLayout(Context context) {
    super(context);
  }

  @Override
  public boolean isChecked() {
    return mChecked;
  }

  @Override
  public void setChecked(boolean checked) {
    mChecked = checked;
  }

  @Override
  public void toggle() {
    mChecked = !mChecked;
  }

}
