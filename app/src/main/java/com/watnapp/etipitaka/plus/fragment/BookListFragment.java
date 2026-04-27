package com.watnapp.etipitaka.plus.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.watnapp.etipitaka.plus.ETipitakaApplication;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.activity.MainActivity;
import com.watnapp.etipitaka.plus.adapter.BookListAdapter;
import com.watnapp.etipitaka.plus.databinding.FragmentBookListBinding;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.plus.model.ETDataModel;
import com.watnapp.etipitaka.plus.model.ETDataModelCreator;
import com.watnapp.etipitaka.plus.vm.SharedViewModel;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 22/5/2013
 * Time: 22:57

 */
public class BookListFragment extends Fragment implements BookListAdapter.BookListAdapterDataSource {

  private ETipitakaApplication application;
  private BookListAdapter mAdapter;
  private ETDataModel dataModel;
  private FragmentBookListBinding binding;
  private SharedViewModel viewModel;

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);
    Activity activity = (Activity)context;
    application = (ETipitakaApplication) activity.getApplication();
    dataModel = ETDataModelCreator.create(application.getLanguage(), getActivity());
  }

  @Override
  public void onDetach() {
    super.onDetach();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mAdapter = new BookListAdapter(getActivity(), application.getLanguage(), this);
    binding.list.setAdapter(mAdapter);
    binding.list.setOnItemClickListener((parent, view1, position, id) -> {
      MainActivity activity = (MainActivity) getActivity();
      activity.openBook(application.getLanguage(), position+1);
      application.setHistory(null);
    });
    viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    viewModel.getSelected().observe(getActivity(), language -> {
      dataModel = ETDataModelCreator.create(language, getActivity());
      mAdapter.setLanguage(language);
      mAdapter.notifyDataSetChanged();
    });
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentBookListBinding.inflate(inflater, container, false);
    View view = binding.getRoot();
    return view;
  }

  @Override
  public int getTitlesArrayId(BookDatabaseHelper.Language language) {
    if (language == BookDatabaseHelper.Language.PALI
            || language == BookDatabaseHelper.Language.PALINEW) {
      return R.array.pali_book_titles_with_numbers;
    } else if (language == BookDatabaseHelper.Language.THAIMM) {
      return R.array.thaimm_book_titles_with_numbers;
    } else if (language == BookDatabaseHelper.Language.THAIBT) {
      return R.array.thaibt_book_titles_with_numbers;
    } else if (language == BookDatabaseHelper.Language.THAIWN) {
      return R.array.thaiwn_book_titles_with_number;
    } else if (language == BookDatabaseHelper.Language.THAIPB) {
      return R.array.thaipb_book_titles_with_number;
    } else if (language == BookDatabaseHelper.Language.ROMANCT) {
      return R.array.romanct_book_titles_with_number;
    } else if (language == BookDatabaseHelper.Language.THAIVN) {
      return R.array.thaivn_book_titles_with_number;
    }
    return R.array.book_titles_with_number;
  }

  @Override
  public int getSectionsArrayId(BookDatabaseHelper.Language language) {
    if (language == BookDatabaseHelper.Language.PALI) {
      return R.array.pali_sections;
    } else if (!Utils.isTipitaka(language)) {
      return 0;
    }
    return R.array.sections;
  }

  @Override
  public int getSectionBoundary(int index) {
    return dataModel.getSectionBoundary(index);
  }
}
