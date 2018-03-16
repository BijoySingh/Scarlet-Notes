package com.bijoysingh.quicknote.activities.sheets

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.view.View.GONE
import android.widget.TextView
import com.bijoysingh.quicknote.MaterialNotes.Companion.appTheme
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.database.utils.getReminder
import com.bijoysingh.quicknote.items.SimpleOptionsItem
import com.bijoysingh.quicknote.reminders.Reminder
import com.bijoysingh.quicknote.reminders.ReminderInterval
import com.bijoysingh.quicknote.reminders.ReminderScheduler
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.github.bijoysingh.starter.util.DateFormatter
import com.github.bijoysingh.uibasics.views.UIActionView
import java.util.*


class ReminderBottomSheet : ThemedBottomSheetFragment() {

  var selectedNote: Note? = null
  var reminder: Reminder? = null

  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val note = selectedNote
    if (note === null) {
      return
    }


    reminder = note.getReminder()
    val isNewReminder = reminder === null
    if (isNewReminder) {
      val calendar = Calendar.getInstance()
      calendar.set(Calendar.HOUR_OF_DAY, 8)
      calendar.set(Calendar.MINUTE, 0)
      calendar.set(Calendar.SECOND, 0)
      if (Calendar.getInstance().after(calendar)) {
        calendar.add(Calendar.HOUR_OF_DAY, 24)
      }
      reminder = Reminder(
          calendar.timeInMillis,
          ReminderInterval.ONCE,
          intArrayOf())
    }
    setColors()
    setContent(reminder!!)
    setListeners(note, isNewReminder)
  }

  fun setListeners(note: Note, isNewReminder: Boolean) {
    val reminderDate = dialog.findViewById<UIActionView>(R.id.reminder_date)
    val reminderTime = dialog.findViewById<UIActionView>(R.id.reminder_time)
    val reminderRepeat = dialog.findViewById<UIActionView>(R.id.reminder_repeat)

    reminderDate.setOnClickListener {
      if (reminder!!.interval == ReminderInterval.ONCE) {
        openDatePickerDialog()
      }
    }
    reminderTime.setOnClickListener {
      openTimePickerDialog()
    }
    reminderRepeat.setOnClickListener {
      openFrequencyDialog()
    }

    val scheduler = ReminderScheduler(themedContext())
    val removeAlarm = dialog.findViewById<TextView>(R.id.remove_alarm)
    val setAlarm = dialog.findViewById<TextView>(R.id.set_alarm)
    if (isNewReminder) {
      removeAlarm.visibility = GONE
    }
    removeAlarm.setOnClickListener {
      scheduler.remove(note)
      dismiss()
    }
    setAlarm.setOnClickListener {
      if (!isNewReminder) {
        scheduler.removeWithoutNote(note.uid, note.uuid)
      }
      if (Calendar.getInstance().after(reminder!!.getCalendar())) {
        dismiss()
        return@setOnClickListener
      }
      scheduler.create(note, reminder!!)
      dismiss()
    }
  }

  fun openFrequencyDialog() {
    val isSelected = fun(interval: ReminderInterval): Boolean = interval == reminder!!.interval
    GenericOptionsBottomSheet.openSheet(
        themedActivity() as ThemedActivity,
        getString(R.string.reminder_sheet_repeat),
        arrayListOf(
            SimpleOptionsItem(
                title = ReminderInterval.ONCE.resource,
                listener = {
                  reminder!!.interval = ReminderInterval.ONCE
                  setContent(reminder!!)
                },
                selected = isSelected(ReminderInterval.ONCE)
            ),
            SimpleOptionsItem(
                title = ReminderInterval.DAILY.resource,
                listener = {
                  reminder!!.interval = ReminderInterval.DAILY
                  setContent(reminder!!)
                },
                selected = isSelected(ReminderInterval.DAILY)
            )
        ))
  }

  fun openDatePickerDialog() {
    val calendar = reminder!!.getCalendar()
    val dialog = DatePickerDialog(
        themedContext(),
        DatePickerDialog.OnDateSetListener { _, year, month, day ->
          calendar.set(Calendar.YEAR, year)
          calendar.set(Calendar.MONTH, month)
          calendar.set(Calendar.DAY_OF_MONTH, day)
          reminder!!.alarmTimestamp = calendar.timeInMillis
          setContent(reminder!!)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH))
    dialog.show()
  }

  fun openTimePickerDialog() {
    val calendar = reminder!!.getCalendar()
    val dialog = TimePickerDialog(
        themedContext(),
        TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
          calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
          calendar.set(Calendar.MINUTE, minute)
          calendar.set(Calendar.SECOND, 0)
          reminder!!.alarmTimestamp = calendar.timeInMillis
          setContent(reminder!!)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false)
    dialog.show()
  }

  fun setContent(reminder: Reminder) {
    val reminderDate = dialog.findViewById<UIActionView>(R.id.reminder_date)
    val reminderTime = dialog.findViewById<UIActionView>(R.id.reminder_time)
    val reminderRepeat = dialog.findViewById<UIActionView>(R.id.reminder_repeat)

    val date = Date(reminder.alarmTimestamp)
    reminderRepeat.setSubtitle(reminder.interval.resource)
    reminderTime.setSubtitle(DateFormatter.getDate(DateFormatter.Formats.HH_MM_A.format, date))
    reminderDate.setSubtitle(DateFormatter.getDate(DateFormatter.Formats.DD_MMM_YYYY.format, date))
    reminderDate.alpha = if (reminder.interval == ReminderInterval.ONCE) 1.0f else 0.5f
  }

  fun setColors() {
    val betaLabel = dialog.findViewById<TextView>(R.id.beta_label)
    val reminderDate = dialog.findViewById<UIActionView>(R.id.reminder_date)
    val reminderTime = dialog.findViewById<UIActionView>(R.id.reminder_time)
    val reminderRepeat = dialog.findViewById<UIActionView>(R.id.reminder_repeat)
    val removeAlarm = dialog.findViewById<TextView>(R.id.remove_alarm)
    val setAlarm = dialog.findViewById<TextView>(R.id.set_alarm)

    val iconColor = appTheme().get(ThemeColorType.TOOLBAR_ICON)
    val textColor = appTheme().get(ThemeColorType.TERTIARY_TEXT)
    val titleColor = appTheme().get(ThemeColorType.SECTION_HEADER)

    val optionsTitle = dialog.findViewById<TextView>(R.id.options_title)
    optionsTitle.setTextColor(appTheme().get(ThemeColorType.SECONDARY_TEXT))

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

    betaLabel.setTextColor(appTheme().get(ThemeColorType.HINT_TEXT))
    betaLabel.setBackgroundResource(
        if (appTheme().isNightTheme()) R.drawable.light_circular_border_bg
        else R.drawable.dark_circular_border_bg)

    removeAlarm.setTextColor(appTheme().get(ThemeColorType.DISABLED_TEXT))
    setAlarm.setTextColor(appTheme().get(ThemeColorType.ACCENT_TEXT))
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_reminder

  companion object {
    fun openSheet(activity: ThemedActivity, note: Note) {
      val sheet = ReminderBottomSheet()
      sheet.selectedNote = note
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}