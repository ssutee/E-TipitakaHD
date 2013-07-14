package com.watnapp.etipitaka.plus.adapter;

import android.R;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.google.inject.Inject;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.model.History;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 12/6/2013
 * Time: 20:26
  */

public class HistoryAdapter extends CursorAdapter {

  @Inject
  public HistoryAdapter(Context context) {
    super(context, null, 0);
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    View view = LayoutInflater.from(context).inflate(R.layout.simple_list_item_2, parent, false);
    ViewHolder viewHolder = new ViewHolder();
    viewHolder.text1 = (TextView) view.findViewById(R.id.text1);
    viewHolder.text2 = (TextView) view.findViewById(R.id.text2);
    view.setTag(viewHolder);
    return view;
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    ViewHolder viewHolder = (ViewHolder) view.getTag();
    History history = History.newInstance(cursor, context);
    viewHolder.text1.setText(history.getKeywords());
    String subtitle = "";
    if (history.getResult1() > 0) {
      subtitle += " " + context.getString(com.watnapp.etipitaka.plus.R.string.abbr_section1,
          Utils.convertToThaiNumber(context, history.getResult1()));
    }
    if (history.getResult2() > 0) {
      subtitle += " " + context.getString(com.watnapp.etipitaka.plus.R.string.abbr_section2,
          Utils.convertToThaiNumber(context, history.getResult2()));
    }
    if (history.getResult3() > 0) {
      subtitle += " " + context.getString(com.watnapp.etipitaka.plus.R.string.abbr_section3,
          Utils.convertToThaiNumber(context, history.getResult3()));
    }
    viewHolder.text2.setText(subtitle.length() > 0
        ? subtitle.trim() : context.getString(com.watnapp.etipitaka.plus.R.string.not_found));
  }

  private static final class ViewHolder {
    TextView text1;
    TextView text2;
  }
}
