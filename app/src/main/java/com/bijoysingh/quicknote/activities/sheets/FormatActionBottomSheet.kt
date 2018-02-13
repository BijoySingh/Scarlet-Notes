package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.view.View
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.activities.ViewAdvancedNoteActivity
import com.bijoysingh.quicknote.formats.Format
import com.bijoysingh.quicknote.items.OptionsItem
import com.github.bijoysingh.starter.util.IntentUtils
import com.github.bijoysingh.starter.util.TextUtils

class FormatActionBottomSheet : GridBottomSheetBase() {

  var format: Format? = null

  override fun setupViewWithDialog(dialog: Dialog) {
    if (format === null) {
      return
    }

    setOptions(dialog, getOptions(format!!))
    setOptionTitle(dialog, R.string.format_action_title)
  }

  private fun getOptions(format: Format): List<OptionsItem> {
    val activity = themedActivity() as ViewAdvancedNoteActivity
    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.import_export_layout_exporting_share,
        subtitle = R.string.import_export_layout_exporting_share,
        icon = R.drawable.ic_share_white_48dp,
        listener = View.OnClickListener {
          IntentUtils.ShareBuilder(activity)
              .setChooserText(activity.getString(R.string.share_using))
              .setText(format.text)
              .share()
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.format_action_copy,
        subtitle = R.string.format_action_copy,
        icon = R.drawable.ic_content_copy_white_48dp,
        listener = View.OnClickListener {
          TextUtils.copyToClipboard(context, format.text)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.delete_sheet_delete_trash_yes,
        subtitle = R.string.delete_sheet_delete_trash_yes,
        icon = R.drawable.ic_delete_white_48dp,
        listener = View.OnClickListener {
          activity.deleteFormat(format)
          dismiss()
        }
    ))
    return options
  }

  companion object {

    fun openSheet(activity: ThemedActivity, format: Format) {
      val sheet = FormatActionBottomSheet()
      sheet.format = format
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}