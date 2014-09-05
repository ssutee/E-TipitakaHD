package com.watnapp.etipitaka.plus.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;
import com.watnapp.etipitaka.plus.model.ETDataModel;
import com.watnapp.etipitaka.plus.model.HistoryItem;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 3/6/2013
 * Time: 13:52
 */
abstract public class SearchResultAdapter extends CursorAdapter implements StickyListHeadersAdapter {

  private static final String TAG = "SearchResultAdapter";
  private final String[] mSections;
  private Context mContext;

  private static final int TYPE_HEAD = 0;
  private static final int TYPE_CONTENT = 1;

  private static final int ID_HEAD = 0;
  private static final int ID_SECTION_1 = 1;
  private static final int ID_SECTION_2 = 2;
  private static final int ID_SECTION_3 = 3;

  public SearchResultAdapter(Context context, Cursor c) {
    super(context, c, 0);
    mContext = context;
    mSections = mContext.getResources().getStringArray(R.array.sections);
  }

  abstract public String getKeywords();
  abstract public int[] getResultsCount();
  abstract public BookDatabaseHelper.Language getLanguage();
  abstract public HistoryItem.Status getStatus(int volume, int page);
  abstract public ETDataModel getDataModel();

  @Override
  public int getItemViewType(int position) {
    if (!Utils.isTipitaka(getLanguage())) {
      return TYPE_CONTENT;
    }
    return (position <= 2) ? TYPE_HEAD : TYPE_CONTENT;
  }

  @Override
  public int getViewTypeCount() {
    return 2;
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
    ViewHolder viewHolder = new ViewHolder();

    View view;
    if (getItemViewType(cursor.getPosition()) == TYPE_HEAD) {
      view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, viewGroup, false);
      viewHolder.text2 = (TextView) view.findViewById(android.R.id.text2);
    } else {
      view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, viewGroup, false);
    }
    viewHolder.text1 = (TextView) view.findViewById(android.R.id.text1);
    view.setTag(viewHolder);
    return view;
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    ViewHolder viewHolder = (ViewHolder) view.getTag();
    if (getItemViewType(cursor.getPosition()) == TYPE_HEAD) {
      switch (cursor.getPosition()) {
        case 0:
          viewHolder.text1.setText(mSections[0]);
          viewHolder.text2.setText(context.getString(R.string.found_n_pages,
              Utils.convertToThaiNumber(context, getResultsCount()[0])));
          break;
        case 1:
          viewHolder.text1.setText(mSections[1]);
          viewHolder.text2.setText(context.getString(R.string.found_n_pages,
              Utils.convertToThaiNumber(context, getResultsCount()[1])));
          break;
        case 2:
          viewHolder.text1.setText(mSections[2]);
          viewHolder.text2.setText(context.getString(R.string.found_n_pages,
              Utils.convertToThaiNumber(context, getResultsCount()[2])));
          break;
      }
    } else if (getItemViewType(cursor.getPosition()) == TYPE_CONTENT) {
      int volume = getDataModel().getVolume(cursor);
      int page = getDataModel().getPageNumber(cursor);
      int stringId = R.string.n_volume_n_page;
      if (getLanguage()  == BookDatabaseHelper.Language.THAIBT) {
        stringId = R.string.n_volume_n_page_minimal;
      } else if (getLanguage() == BookDatabaseHelper.Language.THAIWN) {
        stringId = R.string.buddhawaj_n_volume_n_page;
      }
      viewHolder.text1.setText(context.getString(stringId,
          Utils.convertToThaiNumber(context, volume), Utils.convertToThaiNumber(context, page)));
      HistoryItem.Status status = getStatus(volume, page);
      if (status == HistoryItem.Status.READ) {
        view.setBackgroundResource(R.drawable.read_color);
      } else if (status == HistoryItem.Status.SKIMMED) {
        view.setBackgroundResource(R.drawable.skimmed_color);
      } else {
        view.setBackgroundResource(R.drawable.transparent);
      }
    } else {
      view.setBackgroundResource(R.drawable.transparent);
    }
  }

  @Override
  public View getHeaderView(int position, View convertView, ViewGroup parent) {
    HeaderViewHolder viewHolder;
    if (convertView == null) {
      convertView = LayoutInflater.from(mContext).inflate(R.layout.item_book_title, parent, false);
      viewHolder = new HeaderViewHolder();
      viewHolder.text1 = (TextView) convertView.findViewById(android.R.id.text1);
      viewHolder.text1.setTextSize(15);
      viewHolder.text1.setSingleLine(true);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (HeaderViewHolder) convertView.getTag();
    }

    switch ((int)getHeaderId(position)) {
      case ID_HEAD:
        if (getResultsCount()[0]+getResultsCount()[1]+getResultsCount()[2] > 0) {
          viewHolder.text1.setText(mContext.getString(
              R.string.search_result_summary, getKeywords(),
              Utils.convertToThaiNumber(mContext, getResultsCount()[0]+getResultsCount()[1]+getResultsCount()[2])));
          viewHolder.text1.setSingleLine(false);
        } else {
          viewHolder.text1.setText(mContext.getString(R.string.search_result_not_found,
              getKeywords(), getDataModel().getLanguage().getFullName(mContext) ));
        }
        viewHolder.text1.setSingleLine(false);
        break;
      case ID_SECTION_1:
        viewHolder.text1.setText(mSections[0]);
        break;
      case ID_SECTION_2:
        viewHolder.text1.setText(mSections[1]);
        break;
      case ID_SECTION_3:
        viewHolder.text1.setText(mSections[2]);
        break;
    }
    return convertView;
  }

  @Override
  public long getHeaderId(int position) {
    if (!Utils.isTipitaka(getLanguage()) || (position <= 2 && Utils.isTipitaka(getLanguage()))) {
      return ID_HEAD;
    }

    Cursor cursor = getCursor();
    cursor.moveToPosition(position);

    int volume = getDataModel().getVolume(cursor);

    if (volume >= 1 && volume <= getDataModel().getSectionBoundary(0))
      return ID_SECTION_1;

    if (volume >= getDataModel().getSectionBoundary(0)+1 && volume <= getDataModel().getSectionBoundary(1))
      return ID_SECTION_2;

    return ID_SECTION_3;
  }

  private static final class ViewHolder {
    TextView text1;
    TextView text2;
  }

  private static final class HeaderViewHolder {
    TextView text1;
  }

}
