package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.bijoysingh.quicknote.MaterialNotes.Companion.appTheme
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.items.OptionsItem
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.github.bijoysingh.uibasics.views.UIActionView

abstract class OptionItemBottomSheetBase : ThemedBottomSheetFragment() {
  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }
    reset(dialog)
    resetOptionTitle(dialog)
  }

  abstract fun setupViewWithDialog(dialog: Dialog)

  override fun getBackgroundView(): Int {
    return R.id.options_layout
  }

  fun setOptionTitle(dialog: Dialog, title: Int) {
    val titleView = dialog.findViewById<TextView>(R.id.options_title);
    titleView.setText(title)
  }

  fun resetOptionTitle(dialog: Dialog) {
    val optionsTitle = dialog.findViewById<TextView>(R.id.options_title)
    optionsTitle.setTextColor(appTheme().get(ThemeColorType.SECONDARY_TEXT))
  }

  fun reset(dialog: Dialog) {
    val layout = dialog.findViewById<LinearLayout>(R.id.options_container)
    layout.removeAllViews()
    setupViewWithDialog(dialog)
  }

  open fun setOptions(dialog: Dialog, options: List<OptionsItem>) {
    val layout = dialog.findViewById<LinearLayout>(R.id.options_container);
    for (option in options) {
      if (!option.visible) {
        continue
      }

      val contentView = View.inflate(context, R.layout.layout_option_sheet_item, null) as UIActionView
      contentView.setTitle(option.title)
      when (option.subtitle) {
        0 -> contentView.setSubtitle(option.content)
        else -> contentView.setSubtitle(option.subtitle)
      }
      contentView.setOnClickListener(option.listener)
      contentView.setImageResource(option.icon)

      contentView.setTitleColor(getOptionsTitleColor(option))
      contentView.setSubtitleColor(getOptionsSubtitleColor(option))
      contentView.setImageTint(getOptionsTitleColor(option))

      if (option.enabled) {
        contentView.setActionResource(R.drawable.ic_check_box_white_24dp)
      } else if (option.actionIcon != 0) {
        contentView.setActionResource(option.actionIcon)
      }

      layout.addView(contentView)
    }
  }

  override fun getLayout(): Int = R.layout.layout_options_sheet

  fun getOptionsTitleColor(option: OptionsItem): Int {
    return getOptionsTitleColor(option.selected)
  }

  fun getOptionsSubtitleColor(option: OptionsItem): Int {
    return getOptionsSubtitleColor(option.selected)
  }
}