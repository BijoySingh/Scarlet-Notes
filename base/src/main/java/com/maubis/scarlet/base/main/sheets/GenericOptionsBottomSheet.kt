package com.maubis.scarlet.base.main.sheets

import android.app.Dialog
import android.view.View
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.support.option.OptionsItem
import com.maubis.scarlet.base.support.option.SimpleOptionsItem
import com.maubis.scarlet.base.support.sheets.ChooseOptionBottomSheetBase
import com.maubis.scarlet.base.support.ui.ThemedActivity

class GenericOptionsBottomSheet : ChooseOptionBottomSheetBase() {

  var title: String = ""
  var options: List<SimpleOptionsItem> = emptyList()

  override fun setupViewWithDialog(dialog: Dialog) {
    setOptions(dialog, getOptionItems())
    setOptionTitle(dialog, R.string.sort_sheet_title)
  }

  private fun getOptionItems(): List<OptionsItem> {
    val getIcon = fun(isSelected: Boolean): Int = if (isSelected) R.drawable.ic_done_white_48dp else 0
    return options.map { option ->
      OptionsItem(
          title = option.title,
          subtitle = option.title,
          icon = getIcon(option.selected),
          listener = View.OnClickListener {
            option.listener()
            dismiss()
          },
          selected = option.selected)
    }
  }

  companion object {
    fun openSheet(activity: ThemedActivity, title: String, options: List<SimpleOptionsItem>) {
      val sheet = GenericOptionsBottomSheet()
      sheet.title = title
      sheet.options = options
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}