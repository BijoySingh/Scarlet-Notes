package com.bijoysingh.quicknote.scarlet

import android.content.Context
import com.bijoysingh.quicknote.firebase.data.getFirebaseNote
import com.bijoysingh.quicknote.firebase.support.deleteFromFirebase
import com.bijoysingh.quicknote.firebase.support.insertNoteToFirebase
import com.maubis.scarlet.base.core.database.room.note.Note
import com.maubis.scarlet.base.note.actions.MaterialNoteActor

class ScarletNoteActor(note: Note) : MaterialNoteActor(note) {

  override fun onlineSave(context: Context) {
    super.onlineSave(context)
    insertNoteToFirebase(note.getFirebaseNote())
  }

  override fun onlineDelete(context: Context) {
    super.onlineDelete(context)
    deleteFromFirebase(note.getFirebaseNote())
  }
}