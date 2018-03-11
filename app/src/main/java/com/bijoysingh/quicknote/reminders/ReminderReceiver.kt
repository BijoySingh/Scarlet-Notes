package com.bijoysingh.quicknote.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.utils.NotificationHandler

class ReminderReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (context === null || intent === null) {
      return
    }

    if (!intent.hasExtra(ALARM_ID)) {
      return
    }

    val noteId = intent.getStringExtra(ALARM_ID)
    val note = Note.db().getByUUID(noteId)
    if (note === null) {
      val scheduler = ReminderScheduler(context)
      scheduler.remove(noteId)
      return
    }

    val handler = NotificationHandler(context, note)
    handler.createNotificationChannel()
    handler.openNotification()
  }
}