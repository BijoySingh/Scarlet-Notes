package com.bijoysingh.quicknote.scarlet

import android.content.Context
import com.bijoysingh.quicknote.Scarlet.Companion.firebase
import com.bijoysingh.quicknote.Scarlet.Companion.gDrive
import com.bijoysingh.quicknote.firebase.data.getFirebaseNote
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.core.note.MaterialNoteActor

class ScarletNoteActor(note: Note) : MaterialNoteActor(note) {

  override fun onlineSave(context: Context) {
    super.onlineSave(context)
    firebase?.insert(note.getFirebaseNote())
    gDrive?.insert(note.getFirebaseNote())
  }

  override fun onlineDelete(context: Context) {
    super.onlineDelete(context)
    firebase?.remove(note.getFirebaseNote())
    gDrive?.remove(note.getFirebaseNote())
  }
}