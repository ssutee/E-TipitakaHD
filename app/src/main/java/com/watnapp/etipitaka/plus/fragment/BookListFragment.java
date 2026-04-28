package com.watnapp.etipitaka.plus.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.compose.ui.platform.ComposeView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.watnapp.etipitaka.plus.ETipitakaApplication;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.activity.MainActivity;
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
public class BookListFragment extends Fragment {

  private ETipitakaApplication application;
  private ETDataModel dataModel;
  private ComposeView composeView;
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
    renderBookList();
    viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    viewModel.getSelected().observe(getActivity(), language -> {
      dataModel = ETDataModelCreator.create(language, getActivity());
      renderBookList();
    });
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    composeView = new ComposeView(requireContext());
    return composeView;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    composeView = null;
  }

  private int getTitlesArrayId(BookDatabaseHelper.Language language) {
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

  private int getSectionsArrayId(BookDatabaseHelper.Language language) {
    if (language == BookDatabaseHelper.Language.PALI) {
      return R.array.pali_sections;
    } else if (!Utils.isTipitaka(language)) {
      return 0;
    }
    return R.array.sections;
  }

  private void renderBookList() {
    if (composeView == null) {
      return;
    }

    BookDatabaseHelper.Language language = application.getLanguage();
    int sectionsArrayId = getSectionsArrayId(language);
    String[] sections = sectionsArrayId > 0 ? getResources().getStringArray(sectionsArrayId) : null;
    BookListScreenBridge.render(
        composeView,
        getResources().getStringArray(getTitlesArrayId(language)),
        sections,
        new int[] {dataModel.getSectionBoundary(0), dataModel.getSectionBoundary(1)},
        position -> {
          MainActivity activity = (MainActivity) getActivity();
          activity.openBook(application.getLanguage(), position + 1);
          application.setHistory(null);
        });
  }
}
