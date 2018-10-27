package com.maubis.scarlet.base.core.note

import java.util.*

enum class ReminderInterval {
  ONCE,
  DAILY,
}

class NoteReminder() {
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
    if (interval === ReminderInterval.ONCE) {
      val calendar = Calendar.getInstance()
      calendar.timeInMillis = alarmTimestamp
      return calendar
    }

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

class Reminder(var uid: Int = 0,
               var timestamp: Long = 0,
               var interval: ReminderInterval = ReminderInterval.ONCE) {

  fun toCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    return calendar
  }
}
