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
    gDrive?.notifyInsert(note)
  }

  override fun onlineDelete(context: Context) {
    super.onlineDelete(context)
    gDrive?.notifyRemove(note)
  }
}