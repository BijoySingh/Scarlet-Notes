package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.sheets.openSheet

const val STORE_KEY_NOTE_DEFAULT_COLOR = "KEY_NOTE_DEFAULT_COLOR"

var sNoteDefaultColor: Int
  get() = ApplicationBase.instance.store().get(STORE_KEY_NOTE_DEFAULT_COLOR, (0xFFD32F2F).toInt())
  set(value) = ApplicationBase.instance.store().put(STORE_KEY_NOTE_DEFAULT_COLOR, value)

class NoteSettingsOptionsBottomSheet : LithoOptionBottomSheet() {
  override fun title(): Int = R.string.home_option_note_settings

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<LithoOptionsItem>()
    options.add(LithoOptionsItem(
        title = R.string.note_option_default_color,
        subtitle = R.string.note_option_default_color_subtitle,
        icon = R.drawable.ic_action_color,
        listener = {
          val config = ColorPickerDefaultController(
              title = R.string.note_option_default_color,
              colors = listOf(activity.resources.getIntArray(R.array.bright_colors), activity.resources.getIntArray(R.array.bright_colors_accent)),
              selectedColor = sNoteDefaultColor,
              onColorSelected = { sNoteDefaultColor = it }
          )
          openSheet(activity, ColorPickerBottomSheet().apply { this.config = config })
          dismiss()
        }
    ))
    options.add(LithoOptionsItem(
        title = R.string.home_option_security,
        subtitle = R.string.home_option_security_subtitle,
        icon = R.drawable.ic_option_security,
        listener = {
          SecurityOptionsBottomSheet.openSheet(activity)
          dismiss()
        }
    ))
    return options
  }
}