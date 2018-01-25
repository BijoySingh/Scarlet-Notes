package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.view.View
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.items.OptionsItem
import com.github.bijoysingh.starter.prefs.DataStore

class NoteSettingsOptionsBottomSheet : OptionItemBottomSheetBase() {

  override fun setupViewWithDialog(dialog: Dialog) {
    setOptions(dialog, getOptions())
  }

  private fun getOptions(): List<OptionsItem> {
    val activity = context as MainActivity
    val dataStore = DataStore.get(context)
    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.note_option_default_color,
        subtitle = R.string.note_option_default_color_subtitle,
        icon = R.drawable.ic_action_color,
        listener = View.OnClickListener {
          ColorPickerBottomSheet.openSheet(
              activity,
              object : ColorPickerBottomSheet.ColorPickerDefaultController {
                override fun onColorSelected(color: Int) {
                  dataStore.put(KEY_NOTE_DEFAULT_COLOR, color)
                }

                override fun getSelectedColor(): Int {
                  return genDefaultColor(dataStore)
                }
              }
          )
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_markdown_settings,
        subtitle = R.string.home_option_markdown_settings_subtitle,
        icon = R.drawable.ic_action_markdown,
        listener = View.OnClickListener {
          MarkdownBottomSheet.openSheet(activity)
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_security,
        subtitle = R.string.home_option_security_subtitle,
        icon = R.drawable.ic_option_security,
        listener = View.OnClickListener {
          SecurityOptionsBottomSheet.openSheet(activity)
          dismiss()
        }
    ))
    return options
  }

  override fun getLayout(): Int = R.layout.layout_options_sheet

  companion object {

    const val KEY_NOTE_DEFAULT_COLOR = "KEY_NOTE_DEFAULT_COLOR"

    fun openSheet(activity: MainActivity) {
      val sheet = NoteSettingsOptionsBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }

    fun genDefaultColor(dataStore: DataStore): Int {
      return dataStore.get(KEY_NOTE_DEFAULT_COLOR, -0xff8695)
    }
  }
}