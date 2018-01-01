package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.view.View
import android.widget.LinearLayout
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.items.OptionsItem
import com.github.bijoysingh.uibasics.views.UITextView

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
      contentView.setImageResource(option.icon)

      contentView.setTextColor(getOptionsTitleColor(option))
      contentView.setImageTint(getOptionsTitleColor(option))

      layout.addView(contentView)
    }
  }
}