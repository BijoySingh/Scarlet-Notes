package com.maubis.scarlet.base.note.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.maubis.scarlet.base.core.note.getReminder
import com.maubis.scarlet.base.support.database.notesDB

class ReminderResetReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (context === null || intent === null) {
      return
    }

    val reminderScheduler = ReminderScheduler(context)
    val notes = notesDB.getAll()
    notes.forEach {
      if (it.getReminder() !== null) {
        reminderScheduler.reset(it)
      }
    }
  }
}
