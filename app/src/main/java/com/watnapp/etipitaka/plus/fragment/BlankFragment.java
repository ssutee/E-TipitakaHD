package com.watnapp.etipitaka.plus.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.watnapp.etipitaka.plus.R;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 30/5/2013
 * Time: 10:27
  */

public class BlankFragment extends Fragment {
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_blank, container, false);
  }
}
