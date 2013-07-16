package com.watnapp.etipitaka.plus.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.widget.MyWebView;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 22/5/2013
 * Time: 12:19
 */

public class PageFragment extends RoboFragment implements View.OnTouchListener, Handler.Callback {

  private static final int CLICK_ON_WEBVIEW = 1;
  private static final int CLICK_ON_URL     = 2;
  private static final String TAG = "PageFragment";
  private int mFontSize = Constants.DEFAULT_FONT_SIZE;
  private String mFontColor = Constants.DEFAULT_FONT_COLOR;
  private String mBackgroundColor = Constants.DEFAULT_BACKGROUND_COLOR;

  @InjectView(R.id.webview)
  private MyWebView mWebView;

  private Handler mHandler = new Handler(this);

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_page, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mWebView.getSettings().setJavaScriptEnabled(true);
    String text = getArguments().getString(Constants.CONTENT_KEY);
    String keywords = getArguments().getString(Constants.KEYWORDS_KEY);
    mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    mWebView.setOnTouchListener(this);
    mWebView.setWebViewClient(new WebViewClient() {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        mHandler.sendEmptyMessage(CLICK_ON_URL);
        return false;
      }

      @Override
      public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
      }
    });
    mWebView.setVerticalScrollBarEnabled(false);
    SharedPreferences prefs = getActivity()
        .getSharedPreferences(Constants.SETTING_PREFERENCES, Context.MODE_PRIVATE);
    int fontSize = prefs.getInt(Constants.FONT_SIZE_KEY, Constants.DEFAULT_FONT_SIZE);
    String fontColor = prefs.getString(Constants.FONT_COLOR_KEY, Constants.DEFAULT_FONT_COLOR);
    String backgroundColor = prefs.getString(Constants.BACKGROUND_COLOR_KEY, Constants.DEFAULT_BACKGROUND_COLOR);
    mWebView.loadDataWithBaseURL("http://etipitaka.com",
        getString(R.string.html_text_template,
            highlightItemNumbers(highlightKeywords(text, keywords)),
            String.format("%dpt", fontSize),
            getString(Build.VERSION.SDK_INT >= 15 ? R.string.font_family_new : R.string.font_family_old),
            fontColor, backgroundColor),
        "text/html", "UTF-8", null);
    mWebView.setOnScrollChangedListener((MyWebView.OnScrollChangedListener) getParentFragment());
  }

  public void setFontSize(int size) {
    mFontSize = size;
    SharedPreferences prefs = getActivity()
        .getSharedPreferences(Constants.SETTING_PREFERENCES, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt(Constants.FONT_SIZE_KEY, size);
    editor.commit();
    mWebView.loadUrl(String.format("javascript:(document.body.style.fontSize ='%dpt');", size));
  }

  public int getFontSize() {
    return mFontSize;
  }

  public void setColor(String font, String background) {
    mFontColor = font;
    mBackgroundColor = background;
    SharedPreferences prefs = getActivity()
        .getSharedPreferences(Constants.SETTING_PREFERENCES, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(Constants.FONT_COLOR_KEY, font);
    editor.putString(Constants.BACKGROUND_COLOR_KEY, background);
    editor.commit();
    mWebView.loadUrl(String.format(
        "javascript:(document.body.style.color ='%s'); (document.body.style.background ='%s');",
        font, background));
  }

  public String getFontColor() {
    return mFontColor;
  }

  public String getBackgroundColor() {
    return mBackgroundColor;
  }

  private String highlightItemNumbers(String text) {
    Matcher matcher = Pattern.compile(getString(R.string.regex_item_number)).matcher(text);
    while (matcher.find()) {
      String item = matcher.group().trim();
      text = text.replace(item, String.format("<font color='#EE00EE'><b id=\"i_%s\">%s</b></font>",
          Utils.convertToArabicNumber(getActivity(), item.replace("[","").replace("]", "")), item));
    }
    return text;
  }

  private String highlightKeywords(String text, String keywords) {
    if(keywords.trim().length() > 0) {
      keywords = keywords.replace('+', ' ');
      String [] tokens = keywords.split("\\s+");
      Arrays.sort(tokens, new StringLengthComparator());
      Collections.reverse(Arrays.asList(tokens));
      int count = 0;
      for(String token: tokens) {
        text = text.replace(token,
            String.format("<font color='#0000ff'><b id=\"keywords\">-:*%d*:-</b></font>", count));
        count++;
      }
      count = 0;
      for(String token: tokens) {
        text = text.replace(String.format("-:*%d*:-", count), token);
        count++;
      }
    }
    return text;
  }

  @Override
  public boolean onTouch(View v, MotionEvent event) {
    if (v.getId() == R.id.webview && event.getAction() == MotionEvent.ACTION_DOWN){
      mHandler.sendEmptyMessageDelayed(CLICK_ON_WEBVIEW, 300);
    }
    return false;
  }

  @Override
  public boolean handleMessage(Message msg) {
    if (msg.what == CLICK_ON_URL){
      mHandler.removeMessages(CLICK_ON_WEBVIEW);
      return true;
    }
    if (msg.what == CLICK_ON_WEBVIEW){
      return true;
    }
    return false;
  }

  public void scrollToKeywords() {
    mWebView.postDelayed(new Runnable() {
      @Override
      public void run() {
        mWebView.loadUrl("javascript:scrollToKeywords();");
      }
    }, 500);

    mWebView.postDelayed(new Runnable() {
      @Override
      public void run() {
        mWebView.getOnScrollChangedListener().onScrollDown(mWebView);
      }
    }, 800);
  }

  public void scrollToItem(final int number) {
    mWebView.postDelayed(new Runnable() {
      @Override
      public void run() {
        mWebView.loadUrl(String.format("javascript:scrollToItem(\"%d\");", number));
      }
    }, 500);

    mWebView.postDelayed(new Runnable() {
      @Override
      public void run() {
        mWebView.getOnScrollChangedListener().onScrollDown(mWebView);
      }
    }, 800);
  }

  private class StringLengthComparator implements Comparator<String> {
    public int compare(String o1, String o2) {
      if (o1.length() < o2.length()) {
        return -1;
      } else if (o1.length() > o2.length()) {
        return 1;
      } else {
        return 0;
      }
    }
  }
}
