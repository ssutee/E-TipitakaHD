package com.watnapp.etipitaka.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockListFragment;
import com.google.inject.Inject;
import com.watnapp.etipitaka.R;
import com.watnapp.etipitaka.adapter.BookListAdapter;
import roboguice.inject.InjectView;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 22/5/2013
 * Time: 22:57

 */
public class BookListFragment extends RoboSherlockListFragment {

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    BookListAdapter adapter = new BookListAdapter(getActivity());
    setListAdapter(adapter);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_book_list, container, false);
  }
}
