package com.bijoysingh.quicknote.scarlet

import com.bijoysingh.quicknote.Scarlet.Companion.gDrive
import com.bijoysingh.quicknote.Scarlet.Companion.gDriveDbState
import com.maubis.scarlet.base.core.folder.MaterialFolderActor
import com.maubis.scarlet.base.database.room.folder.Folder

class ScarletFolderActor(folder: Folder) : MaterialFolderActor(folder) {

  private val notifyChange: () -> Unit = {
    gDrive?.notifyChange()
  }

  override fun onlineSave() {
    super.onlineSave()
    gDriveDbState?.notifyInsert(folder, notifyChange)
  }

  override fun delete() {
    super.delete()
    gDriveDbState?.notifyRemove(folder, notifyChange)
  }
}