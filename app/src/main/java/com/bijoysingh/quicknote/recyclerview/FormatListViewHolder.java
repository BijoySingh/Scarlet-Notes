package com.bijoysingh.quicknote.recyclerview;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.activities.ThemedActivity;
import com.bijoysingh.quicknote.formats.Format;
import com.bijoysingh.quicknote.formats.FormatType;
import com.bijoysingh.quicknote.utils.ThemeColorType;
import com.bijoysingh.quicknote.utils.ThemeManager;

import static com.bijoysingh.quicknote.utils.ThemeManagerKt.KEY_NIGHT_THEME;

public class FormatListViewHolder extends FormatTextViewHolder implements TextView.OnEditorActionListener {

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
    edit.setOnEditorActionListener(this);
    edit.setImeOptions(EditorInfo.IME_ACTION_DONE);
    edit.setRawInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
  }

  @Override
  public void populate(final Format data, Bundle extra) {
    super.populate(data, extra);
    final boolean editable = extra != null
                             && extra.containsKey(KEY_EDITABLE)
                             && extra.getBoolean(KEY_EDITABLE);
    icon.setColorFilter(ThemeManager.Companion.get(context).get(context, ThemeColorType.TOOLBAR_ICON));

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


  @Override
  public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
    if (format == null || !edit.isFocused()) {
      return false;
    }

    // Ref: https://stackoverflow.com/questions/1489852/android-handle-enter-in-an-edittext
    if (event == null) {
      if (actionId != EditorInfo.IME_ACTION_DONE && actionId != EditorInfo.IME_ACTION_NEXT) {
        return false;
      }
    } else if (actionId == EditorInfo.IME_NULL || actionId == KeyEvent.KEYCODE_ENTER) {
      if (event.getAction() != KeyEvent.ACTION_DOWN) {
        return true;
      }
    } else {
      return false;
    }

    // Enter clicked
    activity.createOrChangeToNextFormat(format);
    return true;
  }
}
