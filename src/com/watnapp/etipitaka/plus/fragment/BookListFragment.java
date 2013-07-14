package com.watnapp.etipitaka.plus.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockListFragment;
import com.watnapp.etipitaka.plus.E_TipitakaApplication;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.activity.MainActivity;
import com.watnapp.etipitaka.plus.adapter.BookListAdapter;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 22/5/2013
 * Time: 22:57

 */
public class BookListFragment extends RoboSherlockListFragment {

  private E_TipitakaApplication application;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    application = (E_TipitakaApplication) getActivity().getApplication();
  }

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

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    MainActivity activity = (MainActivity) getActivity();
    activity.openBook(application.getLanguage(), position+1);
    application.setHistory(null);
  }
}
