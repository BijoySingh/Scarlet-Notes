package com.maubis.scarlet.base.notification

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.CoreConfig.Companion.notesDb
import com.maubis.scarlet.base.support.INTENT_KEY_ACTION
import com.maubis.scarlet.base.support.INTENT_KEY_NOTE_ID
import com.maubis.scarlet.base.support.utils.throwOrReturn

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

    val note = notesDb.getByID(noteId)
    if (note === null) {
      return
    }

    when (action) {
      NoteAction.COPY -> ApplicationBase.instance.noteActions(note).copy(context)
      NoteAction.SHARE -> ApplicationBase.instance.noteActions(note).share(context)
      NoteAction.DELETE -> {
        ApplicationBase.instance.noteActions(note).softDelete(context)
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
    } catch (exception: Exception) {
      return throwOrReturn(exception, null)
    }
  }

  enum class NoteAction {
    COPY,
    SHARE,
    DELETE,
  }
}