package com.bijoysingh.quicknote.recyclerview;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.activities.AdvancedNoteActivity;
import com.bijoysingh.quicknote.formats.Format;
import com.bijoysingh.quicknote.formats.FormatType;
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class FormatTextViewHolder extends RecyclerViewHolder<Format> implements TextWatcher {

  public static final String KEY_EDITABLE = "KEY_EDITABLE";

  protected AdvancedNoteActivity activity;
  protected TextView text;
  private Format format;
  private ImageView actionUp;
  private ImageView actionDown;
  private ImageView actionDelete;
  private View actionPanel;
  private View actionSeparator;

  /**
   * Constructor for the recycler view holder
   *
   * @param context the application/activity context
   * @param view    the view of the current item
   */
  public FormatTextViewHolder(Context context, View view) {
    super(context, view);
    text = (TextView) view.findViewById(R.id.text);
    text.addTextChangedListener(this);
    activity = (AdvancedNoteActivity) context;
    text.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        activity.focusedFormat = format;
      }
    });
    actionUp = (ImageView) view.findViewById(R.id.action_up);
    actionDown = (ImageView) view.findViewById(R.id.action_down);
    actionDelete = (ImageView) view.findViewById(R.id.action_delete);
    actionPanel = view.findViewById(R.id.action_panel);
    actionSeparator = view.findViewById(R.id.action_separator);
  }

  @Override
  public void populate(final Format data, Bundle extra) {
    boolean uneditable = extra != null
                         && extra.containsKey(KEY_EDITABLE)
                         && !extra.getBoolean(KEY_EDITABLE);
    actionPanel.setVisibility(uneditable ? GONE : VISIBLE);
    text.setEnabled(!uneditable);
    actionSeparator.setVisibility(uneditable ? GONE : VISIBLE);

    text.setText(data.text);
    format = data;
    actionUp.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        activity.moveUpFormat(data);
      }
    });
    actionDown.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        activity.moveDownFormat(data);
      }
    });
    actionDelete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        activity.deleteFormat(data);
      }
    });

    boolean isTop = data.formatType == FormatType.HEADING;
    actionUp.setVisibility(isTop ? GONE : VISIBLE);
    actionDown.setVisibility(isTop ? GONE : VISIBLE);
    actionDelete.setVisibility(isTop ? GONE : VISIBLE);
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {

  }

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {
    if (format == null || !text.isFocused() || (text.getY() + text.getHeight()) < 0) {
      return;
    }
    format.text = s.toString();
    activity.setFormat(format);
  }

  @Override
  public void afterTextChanged(Editable s) {

  }
}
