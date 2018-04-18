package com.bijoysingh.quicknote.database.utils

import com.bijoysingh.quicknote.database.external.FirebaseTag
import com.bijoysingh.quicknote.database.external.deleteTagFromFirebase
import com.bijoysingh.quicknote.database.external.insertTagToFirebase
import com.bijoysingh.quicknote.database.tagsDB
import com.maubis.scarlet.base.database.room.tag.Tag

fun Tag.saveIfUnique() {
  val existing = tagsDB.getByTitle(title)
  if (existing !== null) {
    this.uid = existing.uid
    this.uuid = existing.uuid
    return
  }

  val existingByUUID = tagsDB.getByUUID(uuid)
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
  val id = tagsDB.database().insertTag(this)
  uid = if (isUnsaved()) id.toInt() else uid
  tagsDB.notifyInsertTag(this)
}

fun Tag.saveToSync() {
  // Notify change to online/offline sync
  insertTagToFirebase(getFirebaseTag())
}

fun Tag.delete() {
  deleteWithoutSync()
  deleteToSync()
}

fun Tag.deleteWithoutSync() {
  if (isUnsaved()) {
    return
  }
  tagsDB.database().delete(this)
  tagsDB.notifyDelete(this)
  uid = 0
}

fun Tag.deleteToSync() {
  // Notify change to online/offline sync
  deleteTagFromFirebase(getFirebaseTag())
}

fun Tag.getFirebaseTag(): FirebaseTag = FirebaseTag(uuid, title)
