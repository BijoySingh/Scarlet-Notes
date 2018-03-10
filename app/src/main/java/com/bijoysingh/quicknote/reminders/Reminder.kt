package com.bijoysingh.quicknote.reminders

import android.content.Context
import com.bijoysingh.quicknote.R
import com.github.bijoysingh.starter.prefs.Store
import com.google.gson.Gson
import java.util.*

const val REMINDER_STORE_NAME = "REMINDERS";

enum class ReminderInterval(val resource: Int) {
  ONCE(R.string.reminder_frequency_once),
  DAILY(R.string.reminder_frequency_daily),
  CUSTOM(R.string.reminder_frequency_custom),
}

class Reminder() {
  var uniqueID: Int = 0
  var noteUUID: String = ""
  var alarmTimestamp: Long = 0
  var interval: ReminderInterval = ReminderInterval.ONCE
  var daysOfWeek: IntArray = intArrayOf()


  constructor(
      noteUUID: String,
      alarmTimestamp: Long,
      interval: ReminderInterval,
      daysOfWeek: IntArray) : this() {
    this.uniqueID = Random().nextInt(100000)
    this.alarmTimestamp = alarmTimestamp
    this.noteUUID = noteUUID
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

  fun store(context: Context) {
    val store = Store.get(context, REMINDER_STORE_NAME)
    store.put(noteUUID, Gson().toJson(this))
  }

  fun delete(context: Context) {
    val store = Store.get(context, REMINDER_STORE_NAME)
    store.remove(noteUUID)
  }

  companion object {
    fun load(context: Context, noteUUID: String): Reminder? {
      val store = Store.get(context, REMINDER_STORE_NAME)
      val json = store.get(noteUUID, "")
      if (json.isBlank()) {
        return null
      }
      return Gson().fromJson(json, Reminder::class.java)
    }
  }
}
