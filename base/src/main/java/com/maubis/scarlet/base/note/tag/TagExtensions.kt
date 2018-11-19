package com.maubis.scarlet.base.note.tag

import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.database.room.tag.Tag
import com.maubis.scarlet.base.config.CoreConfig.Companion.tagsDb

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
  CoreConfig.instance.tagActions(this).save()
}

fun Tag.saveWithoutSync() {
  CoreConfig.instance.tagActions(this).offlineSave()
}

fun Tag.saveToSync() {
  CoreConfig.instance.tagActions(this).onlineSave()
}

fun Tag.delete() {
  CoreConfig.instance.tagActions(this).delete()
}

fun Tag.deleteWithoutSync() {
  CoreConfig.instance.tagActions(this).offlineDelete()
}