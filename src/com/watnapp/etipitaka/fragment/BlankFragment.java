package com.watnapp.etipitaka.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.watnapp.etipitaka.R;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 30/5/2013
 * Time: 10:27
  */

public class BlankFragment extends RoboSherlockFragment {
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_blank, container, false);
  }
}
