package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.github.bijoysingh.uibasics.views.UIActionView


class ReminderBottomSheet : ThemedBottomSheetFragment() {
  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val reminderDate = dialog.findViewById<UIActionView>(R.id.reminder_date)
    val reminderTime = dialog.findViewById<UIActionView>(R.id.reminder_time)
    val reminderRepeat = dialog.findViewById<UIActionView>(R.id.reminder_repeat)
    val removeAlarm = dialog.findViewById<TextView>(R.id.remove_alarm)
    val setAlarm = dialog.findViewById<TextView>(R.id.set_alarm)

    val iconColor = theme().get(themedContext(), ThemeColorType.TOOLBAR_ICON)
    val textColor = theme().get(themedContext(), ThemeColorType.TERTIARY_TEXT)
    val titleColor = theme().get(themedContext(), ThemeColorType.SECTION_HEADER)

    val optionsTitle = dialog.findViewById<TextView>(R.id.options_title)
    optionsTitle.setTextColor(theme().get(themedContext(), ThemeColorType.SECONDARY_TEXT))

    reminderDate.setTitleColor(titleColor)
    reminderDate.setSubtitleColor(textColor)
    reminderDate.setImageTint(iconColor)
    reminderDate.setActionTint(iconColor)

    reminderTime.setTitleColor(titleColor)
    reminderTime.setSubtitleColor(textColor)
    reminderTime.setImageTint(iconColor)
    reminderTime.setActionTint(iconColor)

    reminderRepeat.setTitleColor(titleColor)
    reminderRepeat.setSubtitleColor(textColor)
    reminderRepeat.setImageTint(iconColor)
    reminderRepeat.setActionTint(iconColor)

    removeAlarm.setTextColor(theme().get(themedContext(), ThemeColorType.DISABLED_TEXT))
    setAlarm.setTextColor(theme().get(themedContext(), ThemeColorType.ACCENT_TEXT))
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_reminder

  companion object {
    fun openSheet(activity: ThemedActivity) {
      val sheet = ReminderBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}