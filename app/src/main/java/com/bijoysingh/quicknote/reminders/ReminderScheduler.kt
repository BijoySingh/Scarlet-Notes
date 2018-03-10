package com.bijoysingh.quicknote.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_NO_CREATE
import android.content.Context
import android.content.Intent
import com.bijoysingh.quicknote.reminders.Reminder.Companion.load

const val ALARM_ID = "ALARM_ID"

class ReminderScheduler(val context: Context) {

  val manager: AlarmManager

  init {
    manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
  }

  fun get(noteUUID: String) = load(context, noteUUID)

  fun create(reminder: Reminder) {
    reminder.store(context)
    val pendingIntent = getPendingIntent(reminder, PendingIntent.FLAG_UPDATE_CURRENT)
    setReminder(reminder, pendingIntent!!)
  }

  fun remove(noteUUID: String) {
    val reminder = get(noteUUID)
    if (reminder === null) {
      return
    }

    val pendingIntent = getPendingIntent(reminder, FLAG_NO_CREATE)
    if (pendingIntent != null) {
      manager.cancel(pendingIntent)
    }
    reminder.delete(context)
  }

  fun reset(noteUUID: String) {
    val reminder = get(noteUUID)
    if (reminder === null) {
      return
    }

    if (!reminder.shouldUpdate()) {
      reminder.delete(context)
      return
    }

    val pendingIntent = getPendingIntent(reminder, FLAG_NO_CREATE)
    if (pendingIntent != null) {
      manager.cancel(pendingIntent)
      setReminder(reminder, pendingIntent)
    }
  }

  private fun getPendingIntent(reminder: Reminder, flag: Int): PendingIntent? {
    return PendingIntent.getBroadcast(context, reminder.uniqueID, getIntent(reminder.noteUUID), flag)
  }

  private fun getIntent(noteUUID: String): Intent {
    val intent = Intent(context, ReminderReceiver::class.java)
    intent.putExtra(ALARM_ID, noteUUID)
    return intent
  }

  private fun setReminder(reminder: Reminder, pendingIntent: PendingIntent) {
    if (reminder.interval === ReminderInterval.ONCE) {
      manager.set(AlarmManager.RTC_WAKEUP, reminder.getCalendar().getTimeInMillis(), pendingIntent)
    } else {
      manager.setInexactRepeating(
          AlarmManager.RTC_WAKEUP, reminder.getCalendar().getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent)
    }
  }
}
