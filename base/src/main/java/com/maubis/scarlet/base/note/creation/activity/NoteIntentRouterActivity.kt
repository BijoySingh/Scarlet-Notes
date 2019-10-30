package com.maubis.scarlet.base.note.creation.activity

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.maubis.scarlet.base.config.ApplicationBase.Companion.instance

class NoteIntentRouterActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val data: Uri? = intent?.data
    if (data === null) {
      finish()
      return
    }

    handleOpenNote(data)
    handleCreateNote(data)
  }

  fun handleOpenNote(data: Uri): Boolean {
    if (data.host != "open_note") {
      return false
    }

    val noteUUID = data.getQueryParameter("uuid")
    if (noteUUID === null) {
      return false
    }

    val note = instance.notesDatabase().getByUUID(noteUUID)
    if (note === null) {
      return false
    }

    startActivity(ViewAdvancedNoteActivity.getIntent(this, note))
    return true
  }

  fun handleCreateNote(data: Uri): Boolean {
    if (data.host != "create_note") {
      return false
    }

    startActivity(CreateNoteActivity.getNewNoteIntent(this))
    return true
  }
}
