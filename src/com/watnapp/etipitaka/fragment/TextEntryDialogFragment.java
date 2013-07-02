package com.watnapp.etipitaka.fragment;

import android.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.DialerKeyListener;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.text.method.NumberKeyListener;
import android.widget.EditText;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockDialogFragment;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 29/5/2013
 * Time: 20:14
 */

public class TextEntryDialogFragment extends RoboSherlockDialogFragment {

  public interface TextEntryDialogButtonClickListener {
    public void onTextEntryDialogPositiveButtonClick(String text, int id);
    public void onTextEntryDialogNegativeButtonClick();
  }

  public static TextEntryDialogFragment newInstance(int title, String message, int id) {
    TextEntryDialogFragment frag = new TextEntryDialogFragment();
    Bundle args = new Bundle();
    args.putInt("title", title);
    args.putString("message", message);
    args.putInt("id", id);
    frag.setArguments(args);
    return frag;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    int title = getArguments().getInt("title");
    String message = getArguments().getString("message");
    final int id = getArguments().getInt("id");
    final EditText input = new EditText(getActivity());
    input.setKeyListener(new DigitsKeyListener());

    return new AlertDialog.Builder(getActivity())
        .setView(input)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            ((TextEntryDialogButtonClickListener)getActivity())
                .onTextEntryDialogPositiveButtonClick(input.getText().toString(), id);
          }
        })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            ((TextEntryDialogButtonClickListener)getActivity())
                .onTextEntryDialogNegativeButtonClick();
          }
        })
        .create();
  }
}
