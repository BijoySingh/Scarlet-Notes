package com.bijoysingh.quicknote.scarlet

import android.content.Context
import com.bijoysingh.quicknote.Scarlet.Companion.firebase
import com.bijoysingh.quicknote.Scarlet.Companion.gDrive
import com.bijoysingh.quicknote.firebase.data.getFirebaseNote
import com.maubis.scarlet.base.core.note.MaterialNoteActor
import com.maubis.scarlet.base.database.room.note.Note

class ScarletNoteActor(note: Note) : MaterialNoteActor(note) {

  override fun onlineSave(context: Context) {
    super.onlineSave(context)
    // TODO: Remove this completely, Not doing this anymore.
    // firebase?.insert(note.getFirebaseNote())
    gDrive?.notifyInsert(note.getFirebaseNote())
  }

  override fun onlineDelete(context: Context) {
    super.onlineDelete(context)
    // TODO: Remove this completely, Not doing this anymore.
    // firebase?.remove(note.getFirebaseNote())
    gDrive?.notifyRemove(note.getFirebaseNote())
  }
}