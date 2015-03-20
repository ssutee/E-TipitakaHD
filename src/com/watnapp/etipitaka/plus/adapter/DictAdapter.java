package com.watnapp.etipitaka.plus.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by sutee on 20/3/58.
 */
abstract public class DictAdapter extends CursorAdapter {

  public DictAdapter(Context context) {
    super(context, null, 0);
  }

  abstract public String getHeadWordColumn();
  abstract public void setTextViewFontStyle(Context context, TextView textView);

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
    ViewHolder viewHolder = new ViewHolder();
    View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, viewGroup, false);
    viewHolder.text1 = (TextView) view.findViewById(android.R.id.text1);

    setTextViewFontStyle(context, viewHolder.text1);

    view.setTag(viewHolder);
    return view;
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    ViewHolder viewHolder = (ViewHolder) view.getTag();
    String headword = cursor.getString(cursor.getColumnIndex(getHeadWordColumn()));
    viewHolder.text1.setText(headword);
  }

  private static final class ViewHolder {
    TextView text1;
  }
}
