package com.watnapp.etipitaka.plus.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.helper.BookDatabaseHelper;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 23/5/2013
 * Time: 22:17
 */

public class BookListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

  public interface BookListAdapterDataSource {
    int getTitlesArrayId(BookDatabaseHelper.Language language);
    int getSectionsArrayId(BookDatabaseHelper.Language language);
    int getSectionBoundary(int index);
  }

  private BookListAdapterDataSource mDataSource;

  private Context mContext;
  private BookDatabaseHelper.Language mLanguage;

  public BookListAdapter(Context context, BookDatabaseHelper.Language language, BookListAdapterDataSource dataSource) {
    mContext = context;
    mDataSource = dataSource;
    mLanguage = language;
  }

  public void setLanguage(BookDatabaseHelper.Language language) {
    this.mLanguage = language;
  }

  private String[] getTitles() {
    return mContext.getResources().getStringArray(mDataSource.getTitlesArrayId(mLanguage));
  }

  private String[] getSections() {
    return mDataSource.getSectionsArrayId(mLanguage) > 0
        ? mContext.getResources().getStringArray(mDataSource.getSectionsArrayId(mLanguage)) : null;
  }

  @Override
  public long getHeaderId(int position) {
    if (position >= 0 && position < mDataSource.getSectionBoundary(0))
      return 1;
    else if (position >= mDataSource.getSectionBoundary(0) && position < mDataSource.getSectionBoundary(1))
      return 2;
    return 3;
  }

  @Override
  public int getCount() {
    return getTitles().length;
  }

  @Override
  public Object getItem(int position) {
    return getTitles()[position];
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder;
    if (convertView == null) {
      Resources res = mContext.getResources();
      float scale = res.getDisplayMetrics().density;
      viewHolder = new ViewHolder();
      convertView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false);
      viewHolder.text1 = (TextView) convertView.findViewById(android.R.id.text1);
      viewHolder.text1.setSingleLine(true);
      viewHolder.text1.setTextSize(res.getDimension(R.dimen.subtitle_text_size)/scale);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }
    viewHolder.text1.setText(getTitles()[position]);
    return convertView;
  }

  @Override
  public View getHeaderView(int position, View convertView, ViewGroup parent) {
    HeaderViewHolder viewHolder;
    if (convertView == null) {
      viewHolder = new HeaderViewHolder();
      convertView = LayoutInflater.from(mContext).inflate(R.layout.item_book_title, parent, false);
      viewHolder.text1 = (TextView) convertView.findViewById(android.R.id.text1);
      viewHolder.text1.setGravity(Gravity.CENTER_VERTICAL);
      viewHolder.text1.setLines(1);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (HeaderViewHolder) convertView.getTag();
    }

    if (getSections() != null && getHeaderId(position) == 1) {
      viewHolder.text1.setText(getSections()[0]);
    } else if (getSections() != null && getHeaderId(position) == 2) {
      viewHolder.text1.setText(getSections()[1]);
    } else if (getSections() != null) {
      viewHolder.text1.setText(getSections()[2]);
    } else {
      viewHolder.text1.setText("");
    }

    return convertView;
  }

  static private class HeaderViewHolder {
    TextView text1;
  }

  static private class ViewHolder {
    TextView text1;
  }
}
