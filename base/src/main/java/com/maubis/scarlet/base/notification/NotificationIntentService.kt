package com.maubis.scarlet.base.notification

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.support.INTENT_KEY_ACTION
import com.maubis.scarlet.base.support.INTENT_KEY_NOTE_ID

class NotificationIntentService : IntentService("NotificationIntentService") {

  override fun onHandleIntent(intent: Intent?) {
    if (intent === null) {
      return
    }

    val context = applicationContext
    if (context === null) {
      return
    }

    val action = getAction(intent.getStringExtra(INTENT_KEY_ACTION))
    if (action === null) {
      return
    }

    val noteId = intent.getIntExtra(INTENT_KEY_NOTE_ID, 0)
    if (noteId == 0) {
      return
    }

    val note = CoreConfig.instance.notesDatabase().getByID(noteId)
    if (note === null) {
      return
    }

    when (action) {
      NoteAction.COPY -> CoreConfig.instance.noteActions(note).copy(context)
      NoteAction.SHARE -> CoreConfig.instance.noteActions(note).share(context)
      NoteAction.DELETE -> {
        CoreConfig.instance.noteActions(note).softDelete(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(note.uid)
      }
    }
  }

  private fun getAction(action: String?): NoteAction? {
    if (action === null) {
      return null
    }

    try {
      return NoteAction.valueOf(action)
    } catch (_: Exception) {
      return null
    }
  }

  enum class NoteAction {
    COPY,
    SHARE,
    DELETE,
  }
}