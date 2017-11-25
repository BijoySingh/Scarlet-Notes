package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.items.OptionsItem
import com.github.bijoysingh.starter.fragments.SimpleBottomSheetFragment
import com.github.bijoysingh.uibasics.views.UIContentView

abstract class OptionItemBottomSheetBase : SimpleBottomSheetFragment() {
  override fun setupView(dialog: Dialog?) {
    if (dialog == null) {
      return
    }
    setupViewWithDialog(dialog)
  }

  abstract fun setupViewWithDialog(dialog: Dialog)

  fun setOptionTitle(dialog: Dialog, title: Int) {
    val titleView = dialog.findViewById<TextView>(R.id.options_title);
    titleView.setText(title)
  }

  fun setOptions(dialog: Dialog, options: List<OptionsItem>) {
    val layout = dialog.findViewById<LinearLayout>(R.id.options_layout);
    for (option in options) {
      val contentView = View.inflate(context, R.layout.layout_option_sheet_item, null) as UIContentView
      contentView.setTitle(option.title)
      contentView.setSubtitle(option.subtitle)
      contentView.setOnClickListener(option.listener)
      contentView.setImageResource(option.icon)
      layout.addView(contentView)
    }
  }

  override fun getLayout(): Int = R.layout.layout_options_sheet
}