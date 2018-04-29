package com.maubis.scarlet.base.note.reminders.sheet

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.view.View.GONE
import android.widget.TextView
import com.github.bijoysingh.starter.util.DateFormatter
import com.github.bijoysingh.uibasics.views.UIActionView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.database.room.note.Note
import com.maubis.scarlet.base.core.note.NoteReminder
import com.maubis.scarlet.base.core.note.ReminderInterval
import com.maubis.scarlet.base.core.note.getReminder
import com.maubis.scarlet.base.main.sheets.GenericOptionsBottomSheet
import com.maubis.scarlet.base.note.reminders.ReminderScheduler
import com.maubis.scarlet.base.support.option.SimpleOptionsItem
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.ui.ThemedBottomSheetFragment
import java.util.*


class ReminderBottomSheet : ThemedBottomSheetFragment() {

  var selectedNote: Note? = null
  var reminder: NoteReminder? = null

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
      reminder = NoteReminder(
          calendar.timeInMillis,
          ReminderInterval.ONCE,
          intArrayOf())
    }
    setColors()
    setContent(reminder!!)
    setListeners(note, isNewReminder)
    makeBackgroundTransparent(dialog, R.id.root_layout)
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

  fun getReminderIntervalLabel(interval: ReminderInterval): Int {
    return when (interval) {
      ReminderInterval.ONCE -> R.string.reminder_frequency_once
      ReminderInterval.DAILY -> R.string.reminder_frequency_daily
      ReminderInterval.CUSTOM -> R.string.reminder_frequency_custom
    }
  }

  fun openFrequencyDialog() {
    val isSelected = fun(interval: ReminderInterval): Boolean = interval == reminder!!.interval
    GenericOptionsBottomSheet.openSheet(
        themedActivity() as ThemedActivity,
        getString(R.string.reminder_sheet_repeat),
        arrayListOf(
            SimpleOptionsItem(
                title = getReminderIntervalLabel(ReminderInterval.ONCE),
                listener = {
                  reminder!!.interval = ReminderInterval.ONCE
                  setContent(reminder!!)
                },
                selected = isSelected(ReminderInterval.ONCE)
            ),
            SimpleOptionsItem(
                title = getReminderIntervalLabel(ReminderInterval.DAILY),
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
        R.style.DialogTheme,
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
        R.style.DialogTheme,
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

  fun setContent(reminder: NoteReminder) {
    val reminderDate = dialog.findViewById<UIActionView>(R.id.reminder_date)
    val reminderTime = dialog.findViewById<UIActionView>(R.id.reminder_time)
    val reminderRepeat = dialog.findViewById<UIActionView>(R.id.reminder_repeat)

    val date = Date(reminder.alarmTimestamp)
    reminderRepeat.setSubtitle(getReminderIntervalLabel(reminder.interval))
    reminderTime.setSubtitle(DateFormatter.getDate(DateFormatter.Formats.HH_MM_A.format, date))
    reminderDate.setSubtitle(DateFormatter.getDate(DateFormatter.Formats.DD_MMM_YYYY.format, date))
    reminderDate.alpha = if (reminder.interval == ReminderInterval.ONCE) 1.0f else 0.5f
  }

  fun setColors() {
    val reminderDate = dialog.findViewById<UIActionView>(R.id.reminder_date)
    val reminderTime = dialog.findViewById<UIActionView>(R.id.reminder_time)
    val reminderRepeat = dialog.findViewById<UIActionView>(R.id.reminder_repeat)

    val iconColor = CoreConfig.instance.themeController().get(ThemeColorType.TOOLBAR_ICON)
    val textColor = CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT)
    val titleColor = CoreConfig.instance.themeController().get(ThemeColorType.SECTION_HEADER)

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
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_reminder

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.card_layout)

  companion object {
    fun openSheet(activity: ThemedActivity, note: Note) {
      val sheet = ReminderBottomSheet()
      sheet.selectedNote = note
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}