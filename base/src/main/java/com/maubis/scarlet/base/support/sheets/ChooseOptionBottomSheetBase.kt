package com.maubis.scarlet.base.support.sheets

import android.app.Dialog
import android.view.View
import android.widget.LinearLayout
import com.github.bijoysingh.uibasics.views.UITextView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.support.option.OptionsItem

abstract class ChooseOptionBottomSheetBase : OptionItemBottomSheetBase() {

  override fun setOptions(dialog: Dialog, options: List<OptionsItem>) {
    val layout = dialog.findViewById<LinearLayout>(R.id.options_container);
    for (option in options) {
      if (!option.visible) {
        continue
      }

      val contentView = View.inflate(context, R.layout.layout_choose_sheet_item, null) as UITextView
      contentView.setText(option.title)
      contentView.setOnClickListener(option.listener)

      if (option.icon != 0) {
        contentView.setImageResource(option.icon)
      } else {
        contentView.setImageResource(R.drawable.ic_check_box_outline_blank_white_24dp)
        contentView.icon.visibility = View.INVISIBLE
      }

      contentView.setTextColor(getOptionsTitleColor(option))
      contentView.setImageTint(getOptionsTitleColor(option))

      layout.addView(contentView)
    }
  }
}