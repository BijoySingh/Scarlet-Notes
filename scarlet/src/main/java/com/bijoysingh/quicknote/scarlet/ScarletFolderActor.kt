package com.bijoysingh.quicknote.scarlet

import com.bijoysingh.quicknote.Scarlet.Companion.gDrive
import com.bijoysingh.quicknote.Scarlet.Companion.remoteDatabaseStateController
import com.maubis.scarlet.base.core.folder.MaterialFolderActor
import com.maubis.scarlet.base.database.room.folder.Folder

class ScarletFolderActor(folder: Folder) : MaterialFolderActor(folder) {

  private val notifyChange: () -> Unit = {
    gDrive?.notifyChange()
  }

  override fun onlineSave() {
    super.onlineSave()
    remoteDatabaseStateController?.notifyInsert(folder, notifyChange)
  }

  override fun delete() {
    super.delete()
    when {
      gDrive?.isValid() == true -> remoteDatabaseStateController?.notifyRemove(folder, notifyChange)
      else -> remoteDatabaseStateController?.stopTrackingItem(folder, notifyChange)
    }
  }
}