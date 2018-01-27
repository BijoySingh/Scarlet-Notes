package com.bijoysingh.quicknote.recyclerview;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bijoysingh.quicknote.R;
import com.bijoysingh.quicknote.activities.ViewAdvancedNoteActivity;
import com.bijoysingh.quicknote.activities.sheets.TextSizeBottomSheet;
import com.bijoysingh.quicknote.formats.Format;
import com.bijoysingh.quicknote.utils.ThemeColorType;
import com.bijoysingh.quicknote.utils.ThemeManager;
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder;
import com.github.bijoysingh.starter.util.TextUtils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.bijoysingh.quicknote.activities.sheets.SettingsOptionsBottomSheet.KEY_MARKDOWN_ENABLED;
import static com.bijoysingh.quicknote.activities.sheets.TextSizeBottomSheet.KEY_TEXT_SIZE;
import static com.bijoysingh.quicknote.activities.sheets.TextSizeBottomSheet.TEXT_SIZE_DEFAULT;
import static com.bijoysingh.quicknote.formats.FormatType.CHECKLIST_CHECKED;
import static com.bijoysingh.quicknote.formats.FormatType.CHECKLIST_UNCHECKED;
import static com.bijoysingh.quicknote.formats.FormatType.CODE;
import static com.bijoysingh.quicknote.formats.FormatType.QUOTE;
import static com.bijoysingh.quicknote.formats.FormatType.TEXT;
import static com.bijoysingh.quicknote.utils.TextInputUtilsKt.renderMarkdown;

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
    edit.setRawInputType(
        InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            | InputType.TYPE_TEXT_FLAG_MULTI_LINE
            | InputType.TYPE_CLASS_TEXT
            | InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE
    );
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
    boolean isMarkdownEnabled = extra == null
        || extra.getBoolean(KEY_MARKDOWN_ENABLED, true)
        || data.forcedMarkdown;

    int fontSize = extra == null
        ? TextSizeBottomSheet.TEXT_SIZE_DEFAULT
        : extra.getInt(KEY_TEXT_SIZE, TEXT_SIZE_DEFAULT);
    text.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
    edit.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

    ThemeManager theme = ThemeManager.Companion.get(context);

    text.setTextColor(theme.get(context, ThemeColorType.SECONDARY_TEXT));
    edit.setTextColor(theme.get(context, ThemeColorType.SECONDARY_TEXT));
    edit.setHintTextColor(theme.get(context, ThemeColorType.HINT_TEXT));

    int backgroundColorRes = data.formatType == CODE
        ? theme.getThemedColor(context, R.color.material_grey_200, R.color.material_grey_700)
        : R.color.transparent;
    int backgroundColor = ContextCompat.getColor(context, backgroundColorRes);
    text.setBackgroundColor(backgroundColor);
    edit.setBackgroundColor(backgroundColor);
    text.setLinkTextColor(theme.get(context, ThemeColorType.ACCENT_TEXT));

    root.setBackgroundColor(theme.get(context, ThemeColorType.BACKGROUND));

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
      text.setText(renderMarkdown(context, data.text));
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
