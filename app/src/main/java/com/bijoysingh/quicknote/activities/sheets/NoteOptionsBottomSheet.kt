package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.view.View
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.items.OptionsItem
import com.github.bijoysingh.starter.util.IntentUtils

class NoteOptionsBottomSheet : OptionItemBottomSheetBase() {
  override fun setupViewWithDialog(dialog: Dialog) {
    setOptions(dialog, getOptions())
    setOptionTitle(dialog, R.string.choose_action)
  }

  override fun getOptions(): List<OptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.edit_note,
        icon = R.drawable.ic_edit_white_48dp,
        listener = View.OnClickListener {

        }
    ))
    options.add(OptionsItem(
        title = R.string.send_note,
        icon = R.drawable.ic_share_white_48dp,
        listener = View.OnClickListener {

        }
    ))
    options.add(OptionsItem(
        title = R.string.copy_note,
        icon = R.drawable.ic_content_copy_white_48dp,
        listener = View.OnClickListener {

        }
    ))
    options.add(OptionsItem(
        title = R.string.delete_note,
        icon = R.drawable.ic_delete_white_48dp,
        listener = View.OnClickListener {

        }
    ))
    options.add(OptionsItem(
        title = R.string.open_in_popup,
        icon = R.drawable.ic_bubble_chart_white_48dp,
        listener = View.OnClickListener {

        }
    ))
    return options
  }

  companion object {
    fun openSheet(activity: MainActivity) {
      val sheet = NoteOptionsBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}