package com.bijoysingh.quicknote.scarlet

import com.bijoysingh.quicknote.Scarlet.Companion.gDrive
import com.bijoysingh.quicknote.Scarlet.Companion.remoteDatabaseStateController
import com.maubis.scarlet.base.core.tag.MaterialTagActor
import com.maubis.scarlet.base.database.room.tag.Tag

class ScarletTagActor(tag: Tag) : MaterialTagActor(tag) {

  private val notifyChange: () -> Unit = {
    gDrive?.notifyChange()
  }

  override fun onlineSave() {
    super.onlineSave()
    remoteDatabaseStateController?.notifyInsert(tag, notifyChange)
  }

  override fun delete() {
    super.delete()
    when {
      gDrive?.isValid() == true -> remoteDatabaseStateController?.notifyRemove(tag, notifyChange)
      else -> remoteDatabaseStateController?.stopTrackingItem(tag, notifyChange)
    }
  }
}