package com.watnapp.etipitaka.plus.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 3/7/2013
 * Time: 23:27
 */

public class MyWebView extends WebView {
  private static final String TAG = "MyWebView";

  public MyWebView(Context context) {
    super(context);
  }

  public MyWebView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public MyWebView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }
  public OnScrollChangedListener mOnScrollChangedListener = null;

  public void setOnScrollChangedListener(OnScrollChangedListener scrollChangedListener) {
    mOnScrollChangedListener = scrollChangedListener;
  }

  public OnScrollChangedListener getOnScrollChangedListener() {
    return mOnScrollChangedListener;
  }

  public interface OnScrollChangedListener {
    void onScrollUp(View v);
    void onScrollDown(View v);
  }

  @Override
  protected void onScrollChanged(int left, int top, int oldLeft, int oldTop) {
    if ( mOnScrollChangedListener != null ) {
      if (top - oldTop > 10) {
        mOnScrollChangedListener.onScrollUp(this);
      } else if (top - oldTop < -10) {
        mOnScrollChangedListener.onScrollDown(this);
      }
    }
    super.onScrollChanged(left, top, oldLeft, oldTop);
  }
}
