package com.maubis.scarlet.base.note.creation.activity

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.maubis.scarlet.base.config.ApplicationBase.Companion.instance

class NoteIntentRouterActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val data: Uri? = intent?.data
    if (data === null) {
      finish()
      return
    }

    val noteUUID = data.getQueryParameter("uuid")
    if (noteUUID === null) {
      finish()
      return
    }

    val note = instance.notesDatabase().getByUUID(noteUUID)
    if (note === null) {
      finish()
      return
    }

    startActivity(ViewAdvancedNoteActivity.getIntent(this, note))
    finish()
  }
}
