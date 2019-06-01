package com.bijoysingh.quicknote.scarlet

import com.bijoysingh.quicknote.Scarlet.Companion.gDrive
import com.maubis.scarlet.base.core.folder.MaterialFolderActor
import com.maubis.scarlet.base.database.room.folder.Folder

class ScarletFolderActor(folder: Folder) : MaterialFolderActor(folder) {

  override fun onlineSave() {
    super.onlineSave()
    gDrive?.notifyInsert(folder)
  }

  override fun delete() {
    super.delete()
    gDrive?.notifyRemove(folder)
  }
}