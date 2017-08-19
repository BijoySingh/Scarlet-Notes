package com.bijoysingh.quicknote.recyclerview;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.formats.Format;
import com.bijoysingh.quicknote.formats.FormatType;

public class FormatListViewHolder extends FormatTextViewHolder {

  private ImageView icon;

  /**
   * Constructor for the recycler view holder
   *
   * @param context the application/activity context
   * @param view    the view of the current item
   */
  public FormatListViewHolder(Context context, View view) {
    super(context, view);
    icon = (ImageView) view.findViewById(R.id.icon);
  }

  @Override
  public void populate(final Format data, Bundle extra) {
    super.populate(data, extra);
    final boolean editable = extra != null
                             && extra.containsKey(KEY_EDITABLE)
                             && extra.getBoolean(KEY_EDITABLE);

    if (data.formatType == FormatType.CHECKLIST_UNCHECKED) {
      icon.setImageResource(R.drawable.ic_check_box_outline_blank_white_24dp);
      text.setPaintFlags(text.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
    } else if (data.formatType == FormatType.CHECKLIST_CHECKED) {
      icon.setImageResource(R.drawable.ic_check_box_white_24dp);
      text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!editable) {
          activity.setFormatChecked(data, data.formatType != FormatType.CHECKLIST_CHECKED);
        }
      }
    });
  }

}
