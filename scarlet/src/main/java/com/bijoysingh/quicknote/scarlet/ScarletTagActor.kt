package com.bijoysingh.quicknote.scarlet

import com.bijoysingh.quicknote.Scarlet.Companion.gDrive
import com.maubis.scarlet.base.core.tag.MaterialTagActor
import com.maubis.scarlet.base.database.room.tag.Tag

class ScarletTagActor(tag: Tag) : MaterialTagActor(tag) {

  override fun onlineSave() {
    super.onlineSave()
    gDrive?.notifyInsert(tag)
  }

  override fun delete() {
    super.delete()
    gDrive?.notifyRemove(tag)
  }
}