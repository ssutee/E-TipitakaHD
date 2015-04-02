package com.watnapp.etipitaka.plus.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SeekBar;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockDialogFragment;
import com.watnapp.etipitaka.plus.R;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 5/12/13
 * Time: 12:57
 */

public class FontDialogFragment extends RoboSherlockDialogFragment {

  static final String TAG = "FontDialogFragment";

  public interface FontDialogListener {
    public void onDialogPositiveClick(RoboSherlockDialogFragment dialog, int fontSize);
    public void onDialogNegativeClick(RoboSherlockDialogFragment dialog);
  }

  public interface FontDialogDataSource {
    public int getFontSize();
    public String getContent();
  }

  FontDialogListener mListener;
  FontDialogDataSource mDataSource;

  SeekBar mSeekBar;
  WebView mWebView;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (FontDialogListener)activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement FontDialogListener");
    }

    try {
      mDataSource = (FontDialogDataSource)activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement FontDialogDataSource");
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_font_dialog, null);
    mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);
    mSeekBar.setProgress(mDataSource.getFontSize());
    mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mWebView.loadUrl(String.format("javascript:$('body').css('font-size','%dpt');", progress < 1 ? 1 : progress));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    mWebView = (WebView) view.findViewById(R.id.webview);
    mWebView.getSettings().setJavaScriptEnabled(true);
    mWebView.loadDataWithBaseURL("file:///android_asset/", mDataSource.getContent(), "text/html", "UTF-8", null);
    mWebView.setWebViewClient(new WebViewClient() {
      @Override
      public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        mWebView.loadUrl(String.format("javascript:$('body').css('font-size','%dpt');", mDataSource.getFontSize()));
      }
    });

    builder.setView(view);
    builder.setTitle(R.string.adjust_font_size);
    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        mListener.onDialogPositiveClick(FontDialogFragment.this, mSeekBar.getProgress());
      }
    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        mListener.onDialogNegativeClick(FontDialogFragment.this);
      }
    });

    return builder.create();
  }
}
