package com.watnapp.etipitaka.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import com.watnapp.etipitaka.R;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 22/5/2013
 * Time: 12:19
 */

public class PageFragment extends RoboFragment {

  @InjectView(R.id.webView)
  private WebView webView;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_page, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    String text = getArguments().getString("content");
    webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
    webView.loadDataWithBaseURL("http://etipitaka.com",
        String.format("<p style=\"white-space: pre-wrap;\">%s</p>", text),
        "text/html", "UTF-8", null);
  }
}
