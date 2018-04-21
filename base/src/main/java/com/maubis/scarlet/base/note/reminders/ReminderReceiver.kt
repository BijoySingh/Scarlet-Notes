package com.maubis.scarlet.base.note.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.maubis.scarlet.base.notification.NotificationConfig
import com.maubis.scarlet.base.notification.NotificationHandler
import com.maubis.scarlet.base.notification.REMINDER_NOTIFICATION_CHANNEL_ID
import com.maubis.scarlet.base.support.database.notesDB

class ReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (context === null || intent === null) {
      return
    }

    if (!intent.hasExtra(ALARM_ID) || !intent.hasExtra(ALARM_UUID)) {
      return
    }

    val noteID = intent.getIntExtra(ALARM_ID, 0)
    val noteUUID = intent.getStringExtra(ALARM_UUID)
    val note = notesDB.getByUUID(noteUUID)
    if (note === null) {
      val scheduler = ReminderScheduler(context)
      scheduler.removeWithoutNote(noteID, noteUUID)
      return
    }

    val handler = NotificationHandler(context)
    handler.openNotification(NotificationConfig(note, REMINDER_NOTIFICATION_CHANNEL_ID))
  }
}