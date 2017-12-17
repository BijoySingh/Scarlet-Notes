package com.bijoysingh.quicknote.recyclerview;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.activities.ThemedActivity;
import com.bijoysingh.quicknote.activities.ViewAdvancedNoteActivity;
import com.bijoysingh.quicknote.formats.Format;
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder;
import com.github.bijoysingh.starter.util.TextUtils;

import ru.noties.markwon.Markwon;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.bijoysingh.quicknote.activities.sheets.SettingsOptionsBottomSheet.KEY_MARKDOWN_ENABLED;
import static com.bijoysingh.quicknote.formats.FormatType.CHECKLIST_CHECKED;
import static com.bijoysingh.quicknote.formats.FormatType.CHECKLIST_UNCHECKED;
import static com.bijoysingh.quicknote.formats.FormatType.CODE;
import static com.bijoysingh.quicknote.formats.FormatType.QUOTE;
import static com.bijoysingh.quicknote.formats.FormatType.TEXT;
import static com.bijoysingh.quicknote.utils.TextInputUtilsKt.trim;

public class FormatTextViewHolder extends RecyclerViewHolder<Format> implements TextWatcher {

  public static final String KEY_EDITABLE = "KEY_EDITABLE";

  protected ViewAdvancedNoteActivity activity;
  protected TextView text;
  protected EditText edit;
  protected Format format;
  private View actionMove;
  private View actionDelete;
  private View actionCopy;
  private View actionPanel;

  /**
   * Constructor for the recycler view holder
   *
   * @param context the application/activity context
   * @param view    the view of the current item
   */
  public FormatTextViewHolder(Context context, View view) {
    super(context, view);
    text = (TextView) view.findViewById(R.id.text);
    edit = (EditText) view.findViewById(R.id.edit);
    activity = (ViewAdvancedNoteActivity) context;
    edit.addTextChangedListener(this);
    edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        activity.focusedFormat = format;
      }
    });
    actionPanel = view.findViewById(R.id.action_panel);

    actionDelete = view.findViewById(R.id.action_delete);
    actionCopy = view.findViewById(R.id.action_copy);
    actionMove = view.findViewById(R.id.action_move);
  }

  @Override
  public void populate(final Format data, Bundle extra) {
    format = null;
    boolean editable = !(extra != null
        && extra.containsKey(KEY_EDITABLE)
        && !extra.getBoolean(KEY_EDITABLE));

    boolean nightMode = extra != null
        && extra.containsKey(ThemedActivity.Companion.getKey())
        && extra.getBoolean(ThemedActivity.Companion.getKey());
    boolean isMarkdownEnabled = extra == null
        || extra.getBoolean(KEY_MARKDOWN_ENABLED, true)
        || data.forcedMarkdown;

    text.setTextColor(ContextCompat.getColor(
        context, nightMode ? R.color.white : R.color.dark_secondary_text));
    edit.setTextColor(ContextCompat.getColor(
        context, nightMode ? R.color.white : R.color.dark_secondary_text));
    edit.setHintTextColor(ContextCompat.getColor(
        context, nightMode ? R.color.light_tertiary_text : R.color.dark_hint_text));

    int backgroundColorRes = data.formatType == CODE
        ? (nightMode ? R.color.material_grey_700 : R.color.material_grey_200)
        : R.color.transparent;
    int backgroundColor = ContextCompat.getColor(context, backgroundColorRes);
    text.setBackgroundColor(backgroundColor);
    text.setLinkTextColor(ContextCompat.getColor(context,
        nightMode ? R.color.colorAccentDark : R.color.colorAccent));
    edit.setBackgroundColor(backgroundColor);

    root.setBackgroundResource(nightMode ? R.color.material_grey_800 : R.color.white);

    actionPanel.setVisibility(editable ? VISIBLE : GONE);

    text.setTextIsSelectable(true);
    text.setVisibility(editable ? GONE : VISIBLE);
    edit.setVisibility(!editable ? GONE : VISIBLE);
    edit.setEnabled(editable);
    if (editable) {
      edit.setText(data.text);
    } else if (isMarkdownEnabled && (data.formatType == TEXT
        || data.formatType == CHECKLIST_CHECKED
        || data.formatType == CHECKLIST_UNCHECKED
        || data.formatType == QUOTE
        || data.forcedMarkdown)) {
      CharSequence source = Markwon.markdown(context, data.text);
      text.setText(trim(source));
    } else {
      text.setText(data.text);
    }

    format = data;

    actionMove.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        boolean areActionsVisible = actionCopy.getVisibility() == VISIBLE;
        actionCopy.setVisibility(areActionsVisible ? GONE : VISIBLE);
        actionDelete.setVisibility(areActionsVisible ? GONE : VISIBLE);
      }
    });
    actionDelete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        activity.deleteFormat(format);
      }
    });
    actionCopy.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        TextUtils.copyToClipboard(context, edit.getText().toString());
      }
    });
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {

  }

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {
    if (format == null || !edit.isFocused()) {
      return;
    }
    format.text = s.toString();
    activity.setFormat(format);
  }

  @Override
  public void afterTextChanged(Editable s) {

  }

  public void requestEditTextFocus() {
    edit.requestFocus();
  }
}
