package com.bijoysingh.quicknote.scarlet

import com.bijoysingh.quicknote.firebase.data.getFirebaseFolder
import com.bijoysingh.quicknote.firebase.support.deleteFolderFromFirebase
import com.bijoysingh.quicknote.firebase.support.insertFolderToFirebase
import com.maubis.scarlet.base.core.database.room.folder.Folder
import com.maubis.scarlet.base.note.actions.MaterialFolderActor

class ScarletFolderActor(folder: Folder) : MaterialFolderActor(folder) {

  override fun onlineSave() {
    super.onlineSave()
    insertFolderToFirebase(folder.getFirebaseFolder())
  }

  override fun delete() {
    super.delete()
    deleteFolderFromFirebase(folder.getFirebaseFolder())
  }
}