package com.watnapp.etipitaka.plus.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.watnapp.etipitaka.plus.ETipitakaApplication;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.databinding.FragmentMenuBinding;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.plus.model.History;
import com.watnapp.etipitaka.plus.vm.SharedViewModel;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

import kotlin.Unit;

import static com.watnapp.etipitaka.plus.helper.DownloadDatabaseKt.download;
import static com.watnapp.etipitaka.plus.helper.DownloadDatabaseKt.update;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 22/5/2013
 * Time: 22:03
 */

public class MenuFragment extends Fragment implements HistoryFragment.OnHistorySelectedListener {
  private static final String TAG = "MenuFragment";
  private static final String STATE_SELECTED_TAB = "tab";
  private static final String[] TAB_TAGS = {"volume", "search", "history", "favorite"};
  private static final int[] TAB_BUTTON_IDS = {
      R.id.tab_volume,
      R.id.tab_search,
      R.id.tab_history,
      R.id.tab_favorite
  };
  private static final Class<?>[] TAB_FRAGMENT_CLASSES = {
      BookListFragment.class,
      SearchFragment.class,
      HistoryFragment.class,
      FavoriteFragment.class
  };

  private final HashMap<String, Fragment> mFragments = new HashMap<String, Fragment>();
  private ETipitakaApplication application;
  private FragmentMenuBinding binding;
  private SharedViewModel viewModel;
  private int mCurrentTabIndex = 0;

  public void setCurrentTab(int index) {
    if (binding != null && index >= 0 && index < TAB_BUTTON_IDS.length) {
      binding.tabGroup.check(TAB_BUTTON_IDS[index]);
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    application = (ETipitakaApplication) getActivity().getApplication();
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Resources res = getResources();
    float scale = res.getDisplayMetrics().density;

    for (int tabButtonId : TAB_BUTTON_IDS) {
      TextView tv = binding.tabGroup.findViewById(tabButtonId);
      tv.setTextSize(getResources().getDimension(R.dimen.tabwidget_text_size) / scale);
    }

    setupTabs(savedInstanceState);

    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
        R.array.full_languages, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    binding.spnLanguages.setAdapter(adapter);

    int selection = 0;
    for (int code : getResources().getIntArray(R.array.full_languages_code)) {
      if (code == application.getLanguage().getCode()) {
        break;
      }
      selection += 1;
    }
    binding.spnLanguages.setSelection(selection);
    binding.spnLanguages.setTag(R.id.pos, -1);
    binding.spnLanguages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int code = getResources().getIntArray(R.array.full_languages_code)[position];
        BookDatabaseHelper.Language language = BookDatabaseHelper.Language.values()[code];
        String dbPath = Utils.getDatabasePath(getContext(), language);
        if (new File(dbPath).exists()) {
          update(Objects.requireNonNull(getActivity()), language, needUpdate -> {
            if (needUpdate) {
              confirmUpdateDatabase(language, position);
              binding.spnLanguages.setSelection((int)binding.spnLanguages.getTag(R.id.pos));
            } else {
              changeDatabase(position, language);
            }
            return Unit.INSTANCE;
          });

        } else {
          if (Utils.isNetworkConnected(getContext())) {
            confirmDownloadDatabase(language, position);
            binding.spnLanguages.setSelection((int)binding.spnLanguages.getTag(R.id.pos));
          } else {
            Toast.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
          }
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
  }

  private void changeDatabase(int position, BookDatabaseHelper.Language language) {
    application.setLanguage(language);
    if (binding.spnLanguages.getTag() != null && (Integer) binding.spnLanguages.getTag(R.id.pos) != position) {
      viewModel.setResetPage(true);
    }
    viewModel.select(language);
    binding.spnLanguages.setTag(R.id.pos, position);
  }

  private void confirmUpdateDatabase(BookDatabaseHelper.Language language, int position) {
    AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));

    builder.setTitle(R.string.database_needs_update);
    builder.setMessage(R.string.confirm_update_database);

    builder.setPositiveButton(getString(R.string.update), (dialog, which) -> {
      downloadDatabase(language, position);
      dialog.dismiss();
    });
    builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

    AlertDialog alert = builder.create();
    alert.show();
  }

  private void confirmDownloadDatabase(BookDatabaseHelper.Language language, int position) {
    AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));

    builder.setTitle(R.string.database_not_found);
    builder.setMessage(R.string.confirm_download_database);

    builder.setPositiveButton(getString(R.string.download), (dialog, which) -> {
      downloadDatabase(language, position);
      dialog.dismiss();
    });
    builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

    AlertDialog alert = builder.create();
    alert.show();
  }

  private void downloadDatabase(BookDatabaseHelper.Language language, int position) {
    download(Objects.requireNonNull(getActivity()), language, binding.progressbar, success -> {
      if (success) {
        binding.spnLanguages.setSelection(position);
        changeDatabase(position, language);
      }
      return Unit.INSTANCE;
    });
  }

  public void setRadioButton(BookDatabaseHelper.Language language) {
    binding.spnLanguages.setTag(R.id.pos, language.getCode());
    int[] codes = getResources().getIntArray(R.array.full_languages_code);
    int position;
    for (position=0; position<codes.length; ++position) {
        if (codes[position] == language.getCode()) {
          break;
        }
    }
    binding.spnLanguages.setSelection(position, false);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentMenuBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(STATE_SELECTED_TAB, TAB_TAGS[mCurrentTabIndex]);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }

  @Override
  public void onHistorySelected(History history) {
    Log.d(TAG, history.getKeywords());
    SearchFragment fragment = (SearchFragment) mFragments.get("search");
    if (fragment != null) {
      fragment.loadHistory(history);
      setCurrentTab(1);
    }
  }

  private void setupTabs(Bundle savedInstanceState) {
    FragmentManager fragmentManager = getChildFragmentManager();
    FragmentTransaction transaction = fragmentManager.beginTransaction();

    int selectedIndex = getSavedTabIndex(savedInstanceState);
    for (int i = 0; i < TAB_TAGS.length; i++) {
      String tag = TAB_TAGS[i];
      Fragment fragment = fragmentManager.findFragmentByTag(tag);
      if (fragment == null) {
        fragment = Fragment.instantiate(requireContext(), TAB_FRAGMENT_CLASSES[i].getName(), null);
        transaction.add(R.id.menu_content, fragment, tag);
      }
      mFragments.put(tag, fragment);

      if (i == selectedIndex) {
        transaction.show(fragment);
      } else {
        transaction.hide(fragment);
      }
    }
    transaction.commitNow();

    binding.tabGroup.setOnCheckedChangeListener((group, checkedId) -> showTab(indexForButtonId(checkedId)));
    binding.tabGroup.check(TAB_BUTTON_IDS[selectedIndex]);
  }

  private int getSavedTabIndex(Bundle savedInstanceState) {
    if (savedInstanceState == null) {
      return 0;
    }

    String selectedTag = savedInstanceState.getString(STATE_SELECTED_TAB, TAB_TAGS[0]);
    for (int i = 0; i < TAB_TAGS.length; i++) {
      if (TAB_TAGS[i].equals(selectedTag)) {
        return i;
      }
    }
    return 0;
  }

  private int indexForButtonId(int checkedId) {
    for (int i = 0; i < TAB_BUTTON_IDS.length; i++) {
      if (TAB_BUTTON_IDS[i] == checkedId) {
        return i;
      }
    }
    return 0;
  }

  private void showTab(int index) {
    if (index < 0 || index >= TAB_TAGS.length || index == mCurrentTabIndex) {
      return;
    }

    FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
    for (int i = 0; i < TAB_TAGS.length; i++) {
      Fragment fragment = mFragments.get(TAB_TAGS[i]);
      if (fragment == null) {
        continue;
      }
      if (i == index) {
        transaction.show(fragment);
      } else {
        transaction.hide(fragment);
      }
    }
    transaction.commit();
    mCurrentTabIndex = index;
  }
}
