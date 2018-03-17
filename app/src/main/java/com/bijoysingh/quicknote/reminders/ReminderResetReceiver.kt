package com.bijoysingh.quicknote.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.database.utils.NotesDB
import com.bijoysingh.quicknote.database.utils.getReminder

class ReminderResetReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (context === null || intent === null) {
      return
    }

    val reminderScheduler = ReminderScheduler(context)
    val notes = NotesDB.db.getAll()
    notes.forEach {
      if (it.getReminder() !== null) {
        reminderScheduler.reset(it)
      }
    }
  }
}
