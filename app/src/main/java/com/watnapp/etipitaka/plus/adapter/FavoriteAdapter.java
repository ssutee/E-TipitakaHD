package com.watnapp.etipitaka.plus.adapter;

import android.R;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import com.watnapp.etipitaka.plus.Utils;
import com.watnapp.etipitaka.plus.model.Favorite;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 9/7/2013
 * Time: 16:58
 * To change this template use File | Settings | File Templates.
 */
public class FavoriteAdapter extends CursorAdapter {

  public FavoriteAdapter(Context context) {
    super(context, null, 0);
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    View view = LayoutInflater.from(context).inflate(R.layout.simple_list_item_2, parent, false);
    ViewHolder viewHolder = new ViewHolder();
    viewHolder.text1 = (TextView) view.findViewById(R.id.text1);
    viewHolder.text1.setTextSize(18);
    viewHolder.text2 = (TextView) view.findViewById(R.id.text2);
    viewHolder.text2.setSingleLine(false);
    viewHolder.text2.setLines(2);
    viewHolder.text2.setEllipsize(TextUtils.TruncateAt.MARQUEE);
    viewHolder.text2.setTextSize(15);
    view.setTag(viewHolder);
    return view;
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    ViewHolder viewHolder = (ViewHolder) view.getTag();
    Favorite fav = Favorite.newInstance(cursor, context);
    if (fav.getItem() > 0) {
      viewHolder.text1.setText(context.getString(com.watnapp.etipitaka.plus.R.string.favorite_template,
          Utils.convertToThaiNumber(context, fav.getVolume()),
          Utils.convertToThaiNumber(context, fav.getPage()),
          Utils.convertToThaiNumber(context, fav.getItem())));
    } else {
      viewHolder.text1.setText(context.getString(com.watnapp.etipitaka.plus.R.string.favorite_no_item_template,
          Utils.convertToThaiNumber(context, fav.getVolume()),
          Utils.convertToThaiNumber(context, fav.getPage())));
    }
    viewHolder.text2.setText(fav.getNote());
    if (fav.getScore() > 0) {
      Drawable star = context.getResources().getDrawable(R.drawable.btn_star_big_on);
      star.setBounds(0, 0, 60, 60);
      viewHolder.text1.setCompoundDrawables(null, null, star, null);
    } else {
      viewHolder.text1.setCompoundDrawables(null, null, null, null);
    }
  }

  private static final class ViewHolder {
    TextView text1;
    TextView text2;
  }
}
