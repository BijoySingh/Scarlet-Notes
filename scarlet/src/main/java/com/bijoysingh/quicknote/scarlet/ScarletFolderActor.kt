package com.bijoysingh.quicknote.scarlet

import com.bijoysingh.quicknote.Scarlet.Companion.firebase
import com.bijoysingh.quicknote.Scarlet.Companion.gDrive
import com.bijoysingh.quicknote.firebase.data.getFirebaseFolder
import com.maubis.scarlet.base.core.folder.MaterialFolderActor
import com.maubis.scarlet.base.database.room.folder.Folder

class ScarletFolderActor(folder: Folder) : MaterialFolderActor(folder) {

  override fun onlineSave() {
    super.onlineSave()
    // TODO: Remove this completely, Not doing this anymore.
    // firebase?.insert(folder.getFirebaseFolder())
    gDrive?.notifyInsert(folder.getFirebaseFolder())
  }

  override fun delete() {
    super.delete()
    // TODO: Remove this completely, Not doing this anymore.
    // firebase?.remove(folder.getFirebaseFolder())
    gDrive?.notifyRemove(folder.getFirebaseFolder())
  }
}