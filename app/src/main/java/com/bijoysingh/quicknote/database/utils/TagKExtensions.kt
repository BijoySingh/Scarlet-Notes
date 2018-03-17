package com.bijoysingh.quicknote.database.utils

import com.bijoysingh.quicknote.database.Tag

fun Tag.saveIfUnique() {
  val existing = TagsDB.db.getByTitle(title)
  if (existing !== null) {
    this.uid = existing.uid
    this.uuid = existing.uuid
    return
  }

  val existingByUUID = TagsDB.db.getByUUID(uuid)
  if (existingByUUID != null) {
    this.uid = existingByUUID.uid
    this.title = existingByUUID.title
    return
  }

  save()
}

fun Tag.isUnsaved(): Boolean {
  return uid == 0
}

/*Database Functions*/
fun Tag.save() {
  saveWithoutSync()
  saveToSync()
}

fun Tag.saveWithoutSync() {
  val id = TagsDB.db().insertTag(this)
  uid = if (isUnsaved()) id.toInt() else uid
  TagsDB.db.notifyInsertTag(this)
}

fun Tag.saveToSync() {
  // Notify change to online/offline sync
}

fun Tag.delete() {
  deleteWithoutSync()
  deleteToSync()
}

fun Tag.deleteWithoutSync() {
  if (isUnsaved()) {
    return
  }
  TagsDB.db.delete(this)
  uid = 0
}

fun Tag.deleteToSync() {
  // Notify change to online/offline sync
}
