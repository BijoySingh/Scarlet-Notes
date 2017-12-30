package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.ThemedActivity
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
    val optionsTitle = dialog.findViewById<TextView>(R.id.options_title)

    val titleColor = getColor(R.color.material_blue_grey_600, R.color.material_blue_grey_300)
    val iconColor = getColor(R.color.dark_tertiary_text, R.color.light_primary_text)
    val textColor = getColor(R.color.dark_tertiary_text, R.color.light_tertiary_text)

    optionsTitle.setTextColor(getColor(R.color.dark_tertiary_text, R.color.light_secondary_text))

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

    removeAlarm.setTextColor(getColor(R.color.material_grey_600, R.color.material_grey_200))
    setAlarm.setTextColor(getColor(R.color.material_blue_600, R.color.material_blue_200))
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_reminder

  companion object {
    fun openSheet(activity: ThemedActivity) {
      val sheet = ReminderBottomSheet()
      sheet.isNightMode = activity.isNightMode
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}