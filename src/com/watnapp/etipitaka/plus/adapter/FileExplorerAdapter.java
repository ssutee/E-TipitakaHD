package com.watnapp.etipitaka.plus.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListAdapter;
import com.watnapp.etipitaka.plus.R;
import com.watnapp.etipitaka.plus.widget.CheckableLinearLayout;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: sutee
 * Date: 10/7/2013
 * Time: 13:27
  */
abstract public class FileExplorerAdapter extends BaseAdapter implements ListAdapter {

  private Context mContext;

  public FileExplorerAdapter(Context context) {
    mContext = context;
  }

  @Override
  public long getItemId(int position) {
    return 0;
  }

  @SuppressWarnings("unchecked")
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder;
    if (convertView == null) {
      viewHolder = new ViewHolder();
      convertView = LayoutInflater.from(mContext).inflate(
          R.layout.file_explorer_row, parent, false);
      viewHolder.row = (CheckableLinearLayout) convertView;
      viewHolder.txtFileName = (CheckedTextView) convertView
          .findViewById(R.id.txtFileName);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }
    File file = (File) getItem(position);
    viewHolder.txtFileName.setText(file.getName());
    viewHolder.row.setChecked(false);
    viewHolder.txtFileName.setChecked(false);
    if (file.isDirectory()) {
      viewHolder.txtFileName.setCheckMarkDrawable(null);
      viewHolder.txtFileName.setCompoundDrawablesWithIntrinsicBounds(
          R.drawable.folder, 0, 0, 0);
      viewHolder.txtFileName.setCompoundDrawablePadding(10);
    } else {
      int[] attrs = { android.R.attr.listChoiceIndicatorMultiple };
      TypedArray ta = mContext.getTheme().obtainStyledAttributes(attrs);
      viewHolder.txtFileName.setCheckMarkDrawable(ta.getDrawable(0));
      viewHolder.txtFileName.setCompoundDrawablesWithIntrinsicBounds(
          R.drawable.file, 0, 0, 0);
      viewHolder.txtFileName.setCompoundDrawablePadding(10);
    }
    return convertView;
  }

  static private final class ViewHolder {
    CheckableLinearLayout row;
    CheckedTextView txtFileName;
  }
}
