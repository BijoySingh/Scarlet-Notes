package com.bijoysingh.quicknote.service

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.bijoysingh.quicknote.activities.INTENT_KEY_NOTE_ID
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.database.utils.copy
import com.bijoysingh.quicknote.database.utils.deleteOrMoveToTrash
import com.bijoysingh.quicknote.database.utils.share

class NotificationIntentService: IntentService("NotificationIntentService") {

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

    val note = Note.db().getByID(noteId)
    if (note === null) {
      return
    }

    when (action) {
      NoteAction.COPY -> note.copy(context)
      NoteAction.SHARE -> note.share(context)
      NoteAction.DELETE -> {
        note.deleteOrMoveToTrash(context)
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
  
  companion object {
    const val INTENT_KEY_ACTION = "ACTION"
  }

  enum class NoteAction {
    COPY,
    SHARE,
    DELETE,
  }
}