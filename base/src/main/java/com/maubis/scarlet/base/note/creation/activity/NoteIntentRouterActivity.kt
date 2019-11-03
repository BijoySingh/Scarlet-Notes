package com.maubis.scarlet.base.note.creation.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.maubis.scarlet.base.config.ApplicationBase.Companion.instance
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.note.creation.sheet.sEditorSkipNoteViewer

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

    val intent = when (data.getQueryParameter("is_edit", false)) {
      true -> edit(this, note)
      false -> view(this, note, data.getQueryParameter("is_distraction_free", false))
    }
    startActivity(intent)
    return true
  }

  fun handleCreateNote(data: Uri): Boolean {
    if (data.host != "create_note") {
      return false
    }

    startActivity(create(this))
    return true
  }

  companion object {

    private fun Boolean.toInt(): Int {
      return when (this) {
        true -> 1
        else -> 0
      }
    }

    private fun Uri.getQueryParameter(key: String, defaultValue: Boolean): Boolean {
      val param = getQueryParameter(key)
      if (param === null) {
        return defaultValue
      }
      return param == "1"
    }

    fun view(context: Context, note: Note, isDistractionFree: Boolean = false): Intent {
      if (sEditorSkipNoteViewer) {
        return edit(context, note)
      }

      return Intent(context, ViewAdvancedNoteActivity::class.java)
        .putExtra(INTENT_KEY_NOTE_ID, note.uid)
        .putExtra(INTENT_KEY_DISTRACTION_FREE, isDistractionFree)
    }

    fun edit(context: Context, note: Note): Intent {
      return Intent(context, CreateNoteActivity::class.java)
        .putExtra(INTENT_KEY_NOTE_ID, note.uid)
    }

    fun create(context: Context, baseFolder: String = ""): Intent {
      return Intent(context, CreateNoteActivity::class.java)
        .putExtra(CreateNoteActivity.INTENT_KEY_FOLDER, baseFolder)
    }

    fun view(note: Note, isDistractionFree: Boolean = false): Intent {
      val uri = Uri.Builder()
        .scheme("scarlet")
        .authority("open_note")
        .appendQueryParameter("uuid", note.uuid)
        .appendQueryParameter("is_distraction_free", isDistractionFree.toInt().toString())
        .build()
      return Intent(Intent.ACTION_VIEW, uri)
    }

    fun edit(note: Note): Intent {
      val uri = Uri.Builder()
        .scheme("scarlet")
        .authority("open_note")
        .appendQueryParameter("uuid", note.uuid)
        .appendQueryParameter("is_edit", "1")
        .build()
      return Intent(Intent.ACTION_VIEW, uri)
    }

    fun create(): Intent {
      val uri = Uri.Builder()
        .scheme("scarlet")
        .authority("create_note")
        .build()
      return Intent(Intent.ACTION_VIEW, uri)
    }
  }
}
