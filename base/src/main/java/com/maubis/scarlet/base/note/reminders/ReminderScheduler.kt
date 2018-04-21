package com.maubis.scarlet.base.note.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_NO_CREATE
import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.maubis.scarlet.base.core.database.room.note.Note
import com.maubis.scarlet.base.core.note.NoteReminder
import com.maubis.scarlet.base.core.note.ReminderInterval
import com.maubis.scarlet.base.core.note.getMeta
import com.maubis.scarlet.base.core.note.getReminder
import com.maubis.scarlet.base.note.saveWithoutSync

const val ALARM_ID = "ALARM_ID"
const val ALARM_UUID = "ALARM_UUID"

class ReminderScheduler(val context: Context) {

  val manager: AlarmManager

  init {
    manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
  }

  fun setNoteReminder(note: Note, reminder: NoteReminder?) {
    val noteMeta = note.getMeta()
    noteMeta.reminder = reminder
    note.meta = Gson().toJson(noteMeta)
    note.saveWithoutSync(context)
  }

  fun create(note: Note, reminder: NoteReminder) {
    setNoteReminder(note, reminder)
    val pendingIntent = getPendingIntent(note, PendingIntent.FLAG_UPDATE_CURRENT)
    setReminder(reminder, pendingIntent!!)
  }

  fun remove(note: Note) {
    val reminder = note.getReminder()
    if (reminder === null) {
      return
    }

    val pendingIntent = getPendingIntent(note, FLAG_NO_CREATE)
    if (pendingIntent != null) {
      manager.cancel(pendingIntent)
    }
    setNoteReminder(note, null)
  }

  fun removeWithoutNote(noteID: Int, noteUUID: String) {
    val pendingIntent = getPendingIntent(noteID, noteUUID, FLAG_NO_CREATE)
    if (pendingIntent != null) {
      manager.cancel(pendingIntent)
    }
  }

  fun reset(note: Note) {
    val reminder = note.getReminder()
    if (reminder === null) {
      return
    }

    if (!reminder.shouldUpdate()) {
      setNoteReminder(note, null)
      return
    }

    val pendingIntent = getPendingIntent(note, FLAG_NO_CREATE)
    if (pendingIntent != null) {
      manager.cancel(pendingIntent)
      setReminder(reminder, pendingIntent)
    }
  }

  private fun getPendingIntent(note: Note, flag: Int): PendingIntent? {
    return PendingIntent.getBroadcast(context, note.uid, getIntent(note), flag)
  }

  private fun getPendingIntent(noteID: Int, noteUUID: String, flag: Int): PendingIntent? {
    return PendingIntent.getBroadcast(context, noteID, getIntent(noteID, noteUUID), flag)
  }

  private fun getIntent(note: Note): Intent = getIntent(note.uid, note.uuid)

  private fun getIntent(noteID: Int, noteUUID: String): Intent {
    val intent = Intent(context, ReminderReceiver::class.java)
    intent.putExtra(ALARM_ID, noteID)
    intent.putExtra(ALARM_UUID, noteUUID)
    return intent
  }

  private fun setReminder(reminder: NoteReminder, pendingIntent: PendingIntent) {
    if (reminder.interval === ReminderInterval.ONCE) {
      manager.set(AlarmManager.RTC_WAKEUP, reminder.getCalendar().timeInMillis, pendingIntent)
    } else {
      manager.setInexactRepeating(
          AlarmManager.RTC_WAKEUP,
          reminder.getCalendar().timeInMillis,
          AlarmManager.INTERVAL_DAY,
          pendingIntent)
    }
  }
}
