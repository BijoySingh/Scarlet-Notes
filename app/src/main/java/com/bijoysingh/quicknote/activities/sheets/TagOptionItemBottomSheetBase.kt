package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.items.TagOptionsItem
import com.github.bijoysingh.uibasics.views.UIActionView
import com.github.bijoysingh.uibasics.views.UITextView

abstract class TagOptionItemBottomSheetBase : ThemedBottomSheetFragment() {
  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }
    reset(dialog)
    maybeSetTextNightModeColor(dialog, R.id.options_title, R.color.light_tertiary_text)
    setAddTagOption(dialog)
  }

  abstract fun setupViewWithDialog(dialog: Dialog)

  override fun getBackgroundView(): Int {
    return R.id.options_layout
  }

  fun setOptionTitle(dialog: Dialog, title: Int) {
    val titleView = dialog.findViewById<TextView>(R.id.options_title);
    titleView.setText(title)
  }

  fun setAddTagOption(dialog: Dialog) {
    val newTagButton = dialog.findViewById<UITextView>(R.id.new_tag_button);
    newTagButton.setTextColor(getColor(R.color.dark_hint_text, R.color.light_hint_text))
    newTagButton.setImageTint(getColor(R.color.dark_hint_text, R.color.light_hint_text))
  }

  fun reset(dialog: Dialog) {
    val layout = dialog.findViewById<LinearLayout>(R.id.options_container)
    layout.removeAllViews()
    setupViewWithDialog(dialog)
  }

  fun setOptions(dialog: Dialog, options: List<TagOptionsItem>) {
    val layout = dialog.findViewById<LinearLayout>(R.id.options_container);
    for (option in options) {
      val contentView = View.inflate(context, R.layout.layout_option_sheet_item, null) as UIActionView
      contentView.setTitle(option.tag.title)
      contentView.setOnClickListener(option.listener)
      contentView.setImageResource(option.getIcon())

      if (option.editable) {
        contentView.setActionResource(option.getEditIcon());
        contentView.setActionTint(getColor(R.color.dark_hint_text, R.color.light_hint_text));
        contentView.setActionClickListener(option.editListener)
      }

      contentView.setTitleColor(getOptionsTitleColor(option.selected))
      contentView.setSubtitleColor(getOptionsSubtitleColor(option.selected))
      contentView.setImageTint(getOptionsTitleColor(option.selected))
      
      layout.addView(contentView)
    }
  }

  override fun getLayout(): Int = R.layout.layout_tag_options_sheet
}