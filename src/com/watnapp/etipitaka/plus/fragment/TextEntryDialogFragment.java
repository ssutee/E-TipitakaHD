package com.watnapp.etipitaka.plus.fragment;

import android.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockDialogFragment;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 29/5/2013
 * Time: 20:14
 */

public class TextEntryDialogFragment extends RoboSherlockDialogFragment {

  public enum InputMode {
    DIGIT, TEXT
  }

  public interface TextEntryDialogButtonClickListener {
    public void onTextEntryDialogPositiveButtonClick(String text, int id);
    public void onTextEntryDialogNegativeButtonClick();
  }

  public static TextEntryDialogFragment newInstance(int title, String message, int id, int lines,
                                                    InputMode mode, String note) {
    TextEntryDialogFragment frag = new TextEntryDialogFragment();
    Bundle args = new Bundle();
    args.putInt("title", title);
    args.putString("message", message);
    args.putInt("id", id);
    args.putInt("lines", lines);
    args.putInt("mode", mode.ordinal());
    args.putString("note", note);
    frag.setArguments(args);
    return frag;
  }

  public static TextEntryDialogFragment newInstance(int title, String message, int id, int lines, InputMode mode) {
    return TextEntryDialogFragment.newInstance(title, message, id, 1, mode, "");
  }

  public static TextEntryDialogFragment newInstance(int title, String message, int id, int lines) {
    return TextEntryDialogFragment.newInstance(title, message, id, 1, InputMode.DIGIT);
  }

  public static TextEntryDialogFragment newInstance(int title, String message, int id) {
    return TextEntryDialogFragment.newInstance(title, message, id, 1);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    int title = getArguments().getInt("title");
    String message = getArguments().getString("message");
    final int id = getArguments().getInt("id");
    final EditText input = new EditText(getActivity());
    int lines = getArguments().getInt("lines");
    if (lines > 1) {
      input.setSingleLine(false);
      input.setLines(lines);
    }
    int mode = getArguments().getInt("mode");
    if (mode == InputMode.DIGIT.ordinal()) {
      input.setKeyListener(new DigitsKeyListener());
    }
    input.setText(getArguments().getString("note"));

    return new AlertDialog.Builder(getActivity())
        .setView(input)
        .setTitle(title==0 ? null : getActivity().getString(title))
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            try {
              ((TextEntryDialogButtonClickListener)getActivity())
                  .onTextEntryDialogPositiveButtonClick(input.getText().toString(), id);
              if (getParentFragment() != null) {
                ((TextEntryDialogButtonClickListener)getParentFragment())
                    .onTextEntryDialogPositiveButtonClick(input.getText().toString(), id);
              }
            } catch (ClassCastException e) {}
          }
        })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            try {
              ((TextEntryDialogButtonClickListener)getActivity())
                  .onTextEntryDialogNegativeButtonClick();
              if (getParentFragment() != null) {
                ((TextEntryDialogButtonClickListener)getParentFragment())
                    .onTextEntryDialogNegativeButtonClick();
              }
            } catch (ClassCastException e) {}
          }
        })
        .create();
  }
}
