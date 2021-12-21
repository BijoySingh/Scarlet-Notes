package com.bijoysingh.quicknote.scarlet

import android.content.Context
import com.bijoysingh.quicknote.Scarlet.Companion.gDrive
import com.bijoysingh.quicknote.Scarlet.Companion.remoteDatabaseStateController
import com.maubis.scarlet.base.core.note.MaterialNoteActor
import com.maubis.scarlet.base.database.room.note.Note

class ScarletNoteActor(note: Note) : MaterialNoteActor(note) {

  private val notifyChange: () -> Unit = {
    gDrive?.notifyChange()
  }

  override fun onlineSave(context: Context) {
    super.onlineSave(context)
    remoteDatabaseStateController?.notifyInsert(note, notifyChange)
  }

  override fun onlineDelete(context: Context) {
    super.onlineDelete(context)
    when {
      gDrive?.isValid() == true -> remoteDatabaseStateController?.notifyRemove(note, notifyChange)
      else -> remoteDatabaseStateController?.stopTrackingItem(note, notifyChange)
    }
  }
}