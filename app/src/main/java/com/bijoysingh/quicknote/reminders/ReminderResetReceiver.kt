package com.bijoysingh.quicknote.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.bijoysingh.starter.prefs.Store

class ReminderResetReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    if (context === null || intent === null) {
      return
    }

    val store = Store.get(context, REMINDER_STORE_NAME)
    val scheduler = ReminderScheduler(context)
    for (key in store.keys()) {
      scheduler.reset(key)
    }
  }
}
