package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.view.View
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.CreateOrEditAdvancedNoteActivity
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.items.OptionsItem
import com.maubis.scarlet.base.format.MarkdownType

class NoteMarkdownOptionsBottomSheet : GridBottomSheetBase() {
  override fun setupViewWithDialog(dialog: Dialog) {
    setOptions(dialog, getOptions())
    setOptionTitle(dialog, R.string.markdown_sheet_option_title)
  }

  private fun getOptions(): List<OptionsItem> {
    val activity = themedActivity() as CreateOrEditAdvancedNoteActivity
    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.format_label_heading,
        subtitle = R.string.format_label_heading,
        icon = R.drawable.ic_title_white_48dp,
        listener = View.OnClickListener {
          activity.triggerMarkdown(MarkdownType.HEADER)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.markdown_label_bold,
        subtitle = R.string.markdown_label_bold,
        icon = R.drawable.ic_markdown_bold,
        listener = View.OnClickListener {
          activity.triggerMarkdown(MarkdownType.BOLD)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.markdown_label_italics,
        subtitle = R.string.markdown_label_italics,
        icon = R.drawable.ic_markdown_italics,
        listener = View.OnClickListener {
          activity.triggerMarkdown(MarkdownType.ITALICS)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.markdown_label_underline,
        subtitle = R.string.markdown_label_underline,
        icon = R.drawable.ic_markdown_underline,
        listener = View.OnClickListener {
          activity.triggerMarkdown(MarkdownType.UNDERLINE)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.markdown_label_strikethrough,
        subtitle = R.string.markdown_label_strikethrough,
        icon = R.drawable.ic_markdown_strikethrough,
        listener = View.OnClickListener {
          activity.triggerMarkdown(MarkdownType.STRIKE_THROUGH)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.format_label_code,
        subtitle = R.string.format_label_code,
        icon = R.drawable.ic_code_white_48dp,
        listener = View.OnClickListener {
          activity.triggerMarkdown(MarkdownType.CODE)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.markdown_label_list,
        subtitle = R.string.markdown_label_list,
        icon = R.drawable.ic_list_white_48dp,
        listener = View.OnClickListener {
          activity.triggerMarkdown(MarkdownType.UNORDERED)
          dismiss()
        }
    ))
    return options
  }

  companion object {

    fun openSheet(activity: ThemedActivity) {
      val sheet = NoteMarkdownOptionsBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}