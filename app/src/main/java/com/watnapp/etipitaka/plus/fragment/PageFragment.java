package com.watnapp.etipitaka.plus.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.fragment.app.Fragment;

import com.google.common.base.Joiner;
import com.watnapp.etipitaka.plus.Constants;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.databinding.FragmentPageBinding;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.plus.widget.MyWebView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 22/5/2013
 * Time: 12:19
 */

public class PageFragment extends Fragment implements View.OnTouchListener, Handler.Callback {

  private static final int CLICK_ON_WEBVIEW = 1;
  private static final int CLICK_ON_URL     = 2;
  private static final String TAG = "PageFragment";
  private int mFontSize = Constants.DEFAULT_FONT_SIZE;
  private String mFontColor = Constants.DEFAULT_FONT_COLOR;
  private String mBackgroundColor = Constants.DEFAULT_BACKGROUND_COLOR;
  private String mText, mHtml, mFooter, mKeywords;
  private BookDatabaseHelper.Language mLanguage;
  private boolean mIsBuddhawaj;
  private FragmentPageBinding binding;

  private Handler mHandler = new Handler(this);

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentPageBinding.inflate(inflater, container, false);
    View view = binding.getRoot();
    return view;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    binding.webview.getSettings().setJavaScriptEnabled(true);
    binding.webview.getSettings().setDomStorageEnabled(true);
    binding.webview.getSettings().setDefaultTextEncodingName("utf-8");

    mText = getArguments().containsKey(Constants.HTML_CONTENT_KEY) ?
        getArguments().getString(Constants.HTML_CONTENT_KEY) : getArguments().getString(Constants.CONTENT_KEY);
    mFooter = getArguments().containsKey(Constants.FOOTER_KEY) ? getArguments().getString(Constants.FOOTER_KEY) : "";
    mKeywords = getArguments().getString(Constants.KEYWORDS_KEY);
    mIsBuddhawaj = getArguments().getBoolean(Constants.BUDDHAWAJ_KEY);
    mLanguage = BookDatabaseHelper.Language.values()[getArguments().getInt(Constants.LANGUAGE_KEY)];
    binding.webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    binding.webview.setOnTouchListener(this);
    binding.webview.setWebChromeClient(new WebChromeClient());
    binding.webview.setWebViewClient(new WebViewClient() {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        mHandler.sendEmptyMessage(CLICK_ON_URL);
        return false;
      }

      @Override
      public void onPageFinished(WebView view, String url) {
        if(mKeywords != null && mKeywords.trim().length() > 0) {
          ArrayList<String> terms = new ArrayList<String>();
          for (String term : mKeywords.split("\\s+")) {
            terms.add(term.replace('+', ' '));
          }
          Log.d(TAG, "searchType = " + (mIsBuddhawaj ? 2 : 1));
          String script = String.format(Locale.ENGLISH, "javascript:search(\"%s\", %d);",
                  Joiner.on("|").join(terms), mIsBuddhawaj ? 2 : 1);
          view.loadUrl(script);
          scrollToKeywords();
        }
      }
    });
    binding.webview.setVerticalScrollBarEnabled(false);
    SharedPreferences prefs = getActivity()
        .getSharedPreferences(Constants.SETTING_PREFERENCES, Context.MODE_PRIVATE);
    int fontSize = prefs.getInt(Constants.FONT_SIZE_KEY, Constants.DEFAULT_FONT_SIZE);
    String fontColor = prefs.getString(Constants.FONT_COLOR_KEY, Constants.DEFAULT_FONT_COLOR);
    String backgroundColor = prefs.getString(Constants.BACKGROUND_COLOR_KEY, Constants.DEFAULT_BACKGROUND_COLOR);
    String fontFamily = getString(Build.VERSION.SDK_INT >= 15 ? R.string.font_family_new : R.string.font_family_old);
    mHtml = getString(R.string.html_text_template,
        highlightItemNumbers(mText),
        String.format("%dpt", fontSize),
        fontFamily,
        fontColor, backgroundColor, mFooter,
        mLanguage != BookDatabaseHelper.Language.ROMANCT ? "font-family:'TH SarabunPSK'" : "");
    mHtml = mHtml.replace("\t", "&#9;");
    binding.webview.loadDataWithBaseURL("file:///android_asset/", mHtml, "text/html", "UTF-8", null);
    binding.webview.setOnScrollChangedListener((MyWebView.OnScrollChangedListener) getParentFragment());
  }

  public void setFontSize(int size) {
    mFontSize = size;

    if (getActivity() == null) {
      return;
    }

    SharedPreferences prefs = getActivity()
        .getSharedPreferences(Constants.SETTING_PREFERENCES, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putInt(Constants.FONT_SIZE_KEY, size);
    editor.apply();
    binding.webview.loadUrl(String.format("javascript:$('body').css('font-size','%dpt');", size));
  }

  public int getFontSize() {
    return mFontSize;
  }

  public String getContent() {
    return mHtml;
  }

  public void setColor(String font, String background) {
    mFontColor = font;
    mBackgroundColor = background;

    if (getActivity() == null) {
      return;
    }
    SharedPreferences prefs = getActivity()
        .getSharedPreferences(Constants.SETTING_PREFERENCES, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(Constants.FONT_COLOR_KEY, font);
    editor.putString(Constants.BACKGROUND_COLOR_KEY, background);
    editor.apply();
    binding.webview.loadUrl(String.format("javascript:$('body').css('background-color','%s'); $('body').css('color','%s'); ",
        background, font));
  }

  public String getFontColor() {
    return mFontColor;
  }

  public String getBackgroundColor() {
    return mBackgroundColor;
  }

  private String highlightItemNumbers(String text) {
    StringBuffer sb = new StringBuffer();
    for (String line  : text.split("\\r?\\n")) {
      Matcher matcher1 = Pattern.compile(getString(R.string.regex_item_number_1), Pattern.MULTILINE).matcher(line);
      Matcher matcher2 = Pattern.compile(getString(R.string.regex_item_number_2), Pattern.MULTILINE).matcher(line);
      Matcher matcher3 = Pattern.compile(getString(R.string.regex_item_number_3), Pattern.MULTILINE).matcher(line);
      if (matcher1.find()) {
        if (matcher1.groupCount() == 4 && matcher1.group(2) != null) {
          line = matcher1.replaceFirst(String.format("%s<span style='color:#89C200;' id=\"i2_%s\">%s</span>%s<span style='color:#EE00EE;' id=\"i_%s\">[%s]</span>",
              matcher1.group(1), Utils.convertToArabicNumber(getActivity(), matcher1.group(2).replace("{", "").replace("}", "")),
              matcher1.group(2), matcher1.group(3), Utils.convertToArabicNumber(getActivity(), matcher1.group(4)), matcher1.group(4)));
        } else {
          line = matcher1.replaceFirst(String.format("<span style='color:#EE00EE;' id=\"i_%s\">%s[%s]</span>",
              Utils.convertToArabicNumber(getActivity(), matcher1.group(4)), matcher1.group(1), matcher1.group(4)));
        }
      } else if (matcher2.find()) {
        line = matcher2.replaceFirst(String.format("<span style='color:#89C200;' id=\"i2_%s\">%s{%s}</span>",
            Utils.convertToArabicNumber(getActivity(), matcher2.group(2)), matcher2.group(1), matcher2.group(2)));
      } else if (matcher3.find()) {
        String mark = matcher3.group(1);
        if (mark.contains(":")) {
          mark = mark.split(":")[1];
          if (mark.contains(".")) {
            mark = mark.split("\\.")[0];
          }
        }
        line = matcher3.replaceFirst(String.format("<span style='color:#89C200;' id=\"i2_%s\">{%s}</span>",
            mark, matcher3.group(1)));
      }
      sb.append(line+"\n");
    }
    return sb.toString();
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
    binding.webview.postDelayed(new Runnable() {
      @Override
      public void run() {
        binding.webview.loadUrl("javascript:scrollToKeywords();");
      }
    }, 500);

    binding.webview.postDelayed(new Runnable() {
      @Override
      public void run() {
        binding.webview.getOnScrollChangedListener().onScrollDown(binding.webview);
      }
    }, 800);
  }

  public void scrollToItem(final int number) {
    binding.webview.postDelayed(new Runnable() {
      @Override
      public void run() {
        binding.webview.loadUrl(String.format("javascript:scrollToItem(\"%d\");", number));
      }
    }, 500);

    binding.webview.postDelayed(new Runnable() {
      @Override
      public void run() {
        binding.webview.getOnScrollChangedListener().onScrollDown(binding.webview);
      }
    }, 800);
  }

}
