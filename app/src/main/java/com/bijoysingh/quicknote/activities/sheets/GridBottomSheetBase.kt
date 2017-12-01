package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.items.OptionsItem
import com.github.bijoysingh.uibasics.views.UILabelView

abstract class GridBottomSheetBase : ThemedBottomSheetFragment() {

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }
    setupViewWithDialog(dialog)
  }

  abstract fun setupViewWithDialog(dialog: Dialog)

  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  fun setOptionTitle(dialog: Dialog, title: Int) {
    val titleView = dialog.findViewById<TextView>(R.id.options_title);
    titleView.setText(title)
  }

  fun setOptions(dialog: Dialog, options: List<OptionsItem>) {
    maybeSetTextNightModeColor(dialog, R.id.options_title, R.color.light_tertiary_text)

    val layoutGrid = dialog.findViewById<GridLayout>(R.id.grid_layout);
    for (option in options) {
      if (!option.visible) {
        continue
      }

      val contentView = View.inflate(context, R.layout.layout_grid_item, null) as UILabelView
      contentView.setText(option.title)
      contentView.setOnClickListener(option.listener)
      contentView.setImageResource(option.icon)
      if (isNightMode) {
        contentView.setTextColor(ContextCompat.getColor(context, R.color.light_secondary_text))
        contentView.setImageTint(ContextCompat.getColor(context, R.color.light_tertiary_text))
      }
      layoutGrid.addView(contentView)
    }
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_grid_layout
}