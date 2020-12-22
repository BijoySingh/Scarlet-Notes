package com.maubis.scarlet.base.note.tag

import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.CoreConfig.Companion.tagsDb
import com.maubis.scarlet.base.database.room.tag.Tag

fun Tag.saveIfUnique() {
  val existing = tagsDb.getByTitle(title)
  if (existing !== null) {
    this.uid = existing.uid
    this.uuid = existing.uuid
    return
  }

  val existingByUUID = tagsDb.getByUUID(uuid)
  if (existingByUUID != null) {
    this.uid = existingByUUID.uid
    this.title = existingByUUID.title
    return
  }
  save()
}

/**************************************************************************************
 ******************************* Database Functions ********************************
 **************************************************************************************/

fun Tag.save() {
  ApplicationBase.instance.tagActions(this).save()
}

fun Tag.saveWithoutSync() {
  ApplicationBase.instance.tagActions(this).offlineSave()
}

fun Tag.saveToSync() {
  ApplicationBase.instance.tagActions(this).onlineSave()
}

fun Tag.delete() {
  ApplicationBase.instance.tagActions(this).delete()
}

fun Tag.deleteWithoutSync() {
  ApplicationBase.instance.tagActions(this).offlineDelete()
}