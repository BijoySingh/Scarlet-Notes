package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.view.View
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.CreateOrEditAdvancedNoteActivity
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.formats.FormatType
import com.bijoysingh.quicknote.items.OptionsItem

class NoteFormatOptionsBottomSheet : GridBottomSheetBase() {
  override fun setupViewWithDialog(dialog: Dialog) {
    setOptions(dialog, getOptions())
    setOptionTitle(dialog, R.string.format_sheet_option_title)
  }

  private fun getOptions(): List<OptionsItem> {
    val activity = themedActivity() as CreateOrEditAdvancedNoteActivity
    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.format_label_heading,
        subtitle = R.string.format_label_heading,
        icon = R.drawable.ic_title_white_48dp,
        listener = View.OnClickListener {
          activity.addEmptyItemAtFocused(FormatType.HEADING)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.format_label_sub_heading,
        subtitle = R.string.format_label_sub_heading,
        icon = R.drawable.ic_title_white_48dp,
        listener = View.OnClickListener {
          activity.addEmptyItemAtFocused(FormatType.SUB_HEADING)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.format_label_text,
        subtitle = R.string.format_label_text,
        icon = R.drawable.ic_subject_white_48dp,
        listener = View.OnClickListener {
          activity.addEmptyItemAtFocused(FormatType.TEXT)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.format_label_code,
        subtitle = R.string.format_label_code,
        icon = R.drawable.ic_code_white_48dp,
        listener = View.OnClickListener {
          activity.addEmptyItemAtFocused(FormatType.CODE)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.format_label_quote,
        subtitle = R.string.format_label_quote,
        icon = R.drawable.ic_format_quote_white_48dp,
        listener = View.OnClickListener {
          activity.addEmptyItemAtFocused(FormatType.QUOTE)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.format_label_list,
        subtitle = R.string.format_label_list,
        icon = R.drawable.ic_check_box_white_24dp,
        listener = View.OnClickListener {
          activity.addEmptyItemAtFocused(FormatType.CHECKLIST_UNCHECKED)
          dismiss()
        }
    ))
    return options
  }

  companion object {

    fun openSheet(activity: ThemedActivity) {
      val sheet = NoteFormatOptionsBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}