package com.bijoysingh.quicknote.scarlet

import com.bijoysingh.quicknote.Scarlet.Companion.firebase
import com.bijoysingh.quicknote.Scarlet.Companion.gDrive
import com.bijoysingh.quicknote.firebase.data.getFirebaseTag
import com.maubis.scarlet.base.core.tag.MaterialTagActor
import com.maubis.scarlet.base.database.room.tag.Tag

class ScarletTagActor(tag: Tag) : MaterialTagActor(tag) {

  override fun onlineSave() {
    super.onlineSave()
    firebase?.insert(tag.getFirebaseTag())
    gDrive?.notifyInsert(tag.getFirebaseTag())
  }

  override fun delete() {
    super.delete()
    firebase?.remove(tag.getFirebaseTag())
    gDrive?.notifyRemove(tag.getFirebaseTag())
  }
}