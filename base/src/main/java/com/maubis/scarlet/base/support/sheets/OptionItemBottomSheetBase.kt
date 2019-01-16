package com.maubis.scarlet.base.support.sheets

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.github.bijoysingh.uibasics.views.UIActionView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.support.option.OptionsItem
import com.maubis.scarlet.base.support.ui.ThemedBottomSheetFragment

fun getViewForOption(context: Context, option: OptionsItem, titleColor: Int, subtitleColor: Int): UIActionView {
  val contentView = View.inflate(context, R.layout.layout_option_sheet_item, null) as UIActionView
  contentView.setTitle(option.title)
  when (option.subtitle) {
    0 -> contentView.setSubtitle(option.content)
    else -> contentView.setSubtitle(option.subtitle)
  }
  contentView.setOnClickListener(option.listener)
  contentView.setImageResource(option.icon)

  contentView.setTitleColor(titleColor)
  contentView.setSubtitleColor(subtitleColor)
  contentView.setImageTint(titleColor)

  if (option.enabled) {
    contentView.setActionResource(R.drawable.ic_check_box_white_24dp)
  } else if (option.actionIcon != 0) {
    contentView.setActionResource(option.actionIcon)
  }
  return contentView
}

abstract class OptionItemBottomSheetBase : ThemedBottomSheetFragment() {
  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }
    reset(dialog)
  }

  abstract fun setupViewWithDialog(dialog: Dialog)

  override fun getBackgroundView(): Int {
    return R.id.options_layout
  }

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.card_layout)

  fun reset(dialog: Dialog) {
    val layout = dialog.findViewById<LinearLayout>(R.id.options_container)
    layout.removeAllViews()
    setupViewWithDialog(dialog)
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  open fun setOptions(dialog: Dialog, options: List<OptionsItem>) {
    val layout = dialog.findViewById<LinearLayout>(R.id.options_container);
    for (option in options) {
      if (!option.visible) {
        continue
      }

      val contentView = getViewForOption(
          themedContext(), option, getOptionsTitleColor(option), getOptionsSubtitleColor(option))
      layout.addView(contentView)
    }
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_options

  fun getOptionsTitleColor(option: OptionsItem): Int {
    return getOptionsTitleColor(option.selected)
  }

  fun getOptionsSubtitleColor(option: OptionsItem): Int {
    return getOptionsSubtitleColor(option.selected)
  }
}