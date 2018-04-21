package com.bijoysingh.quicknote.scarlet

import com.bijoysingh.quicknote.firebase.data.getFirebaseTag
import com.bijoysingh.quicknote.firebase.support.deleteTagFromFirebase
import com.bijoysingh.quicknote.firebase.support.insertTagToFirebase
import com.maubis.scarlet.base.core.database.room.tag.Tag
import com.maubis.scarlet.base.note.actions.MaterialTagActor

class ScarletTagActor(tag: Tag) : MaterialTagActor(tag) {

  override fun onlineSave() {
    super.onlineSave()
    insertTagToFirebase(tag.getFirebaseTag())
  }

  override fun delete() {
    super.delete()
    deleteTagFromFirebase(tag.getFirebaseTag())
  }
}