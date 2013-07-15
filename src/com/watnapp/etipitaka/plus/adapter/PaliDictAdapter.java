package com.watnapp.etipitaka.plus.adapter;

import android.R;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 15/7/2013
 * Time: 14:27
 */

public class PaliDictAdapter extends CursorAdapter {

  @Inject
  public PaliDictAdapter(Context context) {
    super(context, null, 0);
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
    ViewHolder viewHolder = new ViewHolder();
    View view = LayoutInflater.from(context).inflate(R.layout.simple_list_item_1, viewGroup, false);
    viewHolder.text1 = (TextView) view.findViewById(R.id.text1);
    Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/THSarabun.ttf");
    viewHolder.text1.setTypeface(font);
    viewHolder.text1.setTextSize(26);
    view.setTag(viewHolder);
    return view;
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    ViewHolder viewHolder = (ViewHolder) view.getTag();
    String headword = cursor.getString(cursor.getColumnIndex("headword"));
    viewHolder.text1.setText(headword);
  }

  private static final class ViewHolder {
    TextView text1;
  }
}
