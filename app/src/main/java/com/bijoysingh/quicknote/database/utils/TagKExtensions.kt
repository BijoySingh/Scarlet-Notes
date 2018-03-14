package com.bijoysingh.quicknote.database.utils

import com.bijoysingh.quicknote.database.Tag

fun Tag.saveIfUnique() {
  val existing = Tag.db().getByTitle(title)
  if (existing !== null) {
    this.uid = existing.uid
    this.title = existing.title
    this.uuid = existing.uuid
  }

  val existingByUUID = Tag.db().getByUUID(uuid)
  if (existingByUUID != null) {
    this.uid = existing.uid
    this.title = existing.title
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
  val id = Tag.db().insertTag(this)
  uid = if (isUnsaved()) id.toInt() else uid
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
  Tag.db().delete(this)
  uid = 0
}

fun Tag.deleteToSync() {
  // Notify change to online/offline sync
}
