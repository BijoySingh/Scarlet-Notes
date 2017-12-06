package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.items.OptionsItem
import com.github.bijoysingh.uibasics.views.UIContentView

abstract class OptionItemBottomSheetBase : ThemedBottomSheetFragment() {
  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }
    reset(dialog)
    maybeSetTextNightModeColor(dialog, R.id.options_title, R.color.light_tertiary_text)
  }

  abstract fun setupViewWithDialog(dialog: Dialog)

  override fun getBackgroundView(): Int {
    return R.id.options_layout
  }

  fun setOptionTitle(dialog: Dialog, title: Int) {
    val titleView = dialog.findViewById<TextView>(R.id.options_title);
    titleView.setText(title)
  }

  fun reset(dialog: Dialog) {
    val layout = dialog.findViewById<LinearLayout>(R.id.options_container)
    layout.removeAllViews()
    setupViewWithDialog(dialog)
  }

  fun setOptions(dialog: Dialog, options: List<OptionsItem>) {
    val layout = dialog.findViewById<LinearLayout>(R.id.options_container);
    for (option in options) {
      if (!option.visible) {
        continue
      }

      val contentView = View.inflate(context, R.layout.layout_option_sheet_item, null) as UIContentView
      contentView.setTitle(option.title)
      contentView.setSubtitle(option.subtitle)
      contentView.setOnClickListener(option.listener)
      contentView.setImageResource(option.icon)

      contentView.setTitleColor(getTitleColor(option))
      contentView.setSubtitleColor(getSubtitleColor(option))
      contentView.setImageTint(getTitleColor(option))
      
      layout.addView(contentView)
    }
  }

  private fun getTitleColor(option: OptionsItem): Int {
    val colorResource = when {
      isNightMode && option.selected -> R.color.material_blue_400
      isNightMode -> R.color.light_secondary_text
      option.selected -> R.color.material_blue_700
      else -> R.color.dark_secondary_text
    }
    return ContextCompat.getColor(context, colorResource)
  }

  private fun getSubtitleColor(option: OptionsItem): Int {
    val colorResource = when {
      isNightMode && option.selected -> R.color.material_blue_200
      isNightMode -> R.color.light_tertiary_text
      option.selected -> R.color.material_blue_500
      else -> R.color.dark_tertiary_text
    }
    return ContextCompat.getColor(context, colorResource)
  }

  override fun getLayout(): Int = R.layout.layout_options_sheet
}