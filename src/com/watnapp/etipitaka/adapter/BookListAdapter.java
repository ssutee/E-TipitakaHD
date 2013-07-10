package com.watnapp.etipitaka.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.google.inject.Inject;
import com.watnapp.etipitaka.R;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 23/5/2013
 * Time: 22:17
 */

public class BookListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

  private String[] mTitles;
  private String[] mSections;

  @Inject
  private Context mContext;

  @Inject
  public BookListAdapter(Context context) {
    mContext = context;
    mTitles = context.getResources().getStringArray(R.array.book_titles_with_number);
    mSections = context.getResources().getStringArray(R.array.sections);
  }

  @Override
  public long getHeaderId(int position) {
    if (position >= 0 && position <= 7)
      return 1;
    else if (position >= 8 && position <= 32)
      return 2;
    return 3;
  }

  @Override
  public int getCount() {
    return mTitles.length;
  }

  @Override
  public Object getItem(int position) {
    return mTitles[position];
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder;
    if (convertView == null) {
      viewHolder = new ViewHolder();
      convertView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false);
      viewHolder.text1 = (TextView) convertView.findViewById(android.R.id.text1);
      viewHolder.text1.setSingleLine(true);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }
    viewHolder.text1.setText(mTitles[position]);
    return convertView;
  }

  @Override
  public View getHeaderView(int position, View convertView, ViewGroup parent) {
    HeaderViewHolder viewHolder;
    if (convertView == null) {
      viewHolder = new HeaderViewHolder();
      convertView = LayoutInflater.from(mContext).inflate(R.layout.item_book_title, parent, false);
      viewHolder.text1 = (TextView) convertView.findViewById(android.R.id.text1);
      viewHolder.text1.setTextSize(20);
      viewHolder.text1.setGravity(Gravity.CENTER_VERTICAL);
      viewHolder.text1.setLines(1);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (HeaderViewHolder) convertView.getTag();
    }

    if (getHeaderId(position) == 1) {
      viewHolder.text1.setText(mSections[0]);
    } else if (getHeaderId(position) == 2) {
      viewHolder.text1.setText(mSections[1]);
    } else {
      viewHolder.text1.setText(mSections[2]);
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
