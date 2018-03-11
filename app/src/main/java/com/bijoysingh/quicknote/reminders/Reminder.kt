package com.bijoysingh.quicknote.reminders

import com.bijoysingh.quicknote.R
import java.util.*

enum class ReminderInterval(val resource: Int) {
  ONCE(R.string.reminder_frequency_once),
  DAILY(R.string.reminder_frequency_daily),
  CUSTOM(R.string.reminder_frequency_custom),
}

class Reminder() {
  var alarmTimestamp: Long = 0
  var interval: ReminderInterval = ReminderInterval.ONCE
  var daysOfWeek: IntArray = intArrayOf()


  constructor(
      alarmTimestamp: Long,
      interval: ReminderInterval,
      daysOfWeek: IntArray) : this() {
    this.alarmTimestamp = alarmTimestamp
    this.interval = interval
    this.daysOfWeek = daysOfWeek
  }

  fun getCalendar(): Calendar {
    val minutes = alarmTimestamp / (1000 * 60)
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, ((minutes / 60) % 24).toInt())
    calendar.set(Calendar.MINUTE, (minutes % 60).toInt())
    calendar.set(Calendar.SECOND, 0)

    val nowCalendar = Calendar.getInstance()
    if (nowCalendar.after(calendar)) {
      calendar.add(Calendar.HOUR_OF_DAY, 24)
    }
    return calendar
  }

  fun shouldUpdate(): Boolean {
    if (interval === ReminderInterval.ONCE) {
      return System.currentTimeMillis() < alarmTimestamp;
    }
    return true;
  }
}
