package com.bijoysingh.quicknote.database.utils

import android.content.Context
import com.bijoysingh.quicknote.database.Tag

fun Tag.saveIfUnique(context: Context) {
  val existing = Tag.db(context).getByTitle(title)
  if (existing == null) {
    save(context)
    return
  }

  this.uid = existing.uid
  this.title = existing.title
}

fun Tag.isUnsaved(): Boolean {
  return uid == 0
}

/*Database Functions*/
fun Tag.save(context: Context) {
  saveWithoutSync(context)
  saveToSync()
}

fun Tag.saveWithoutSync(context: Context) {
  val id = Tag.db(context).insertTag(this)
  uid = if (isUnsaved()) id.toInt() else uid
}

fun Tag.saveToSync() {
  // Notify change to online/offline sync
}

fun Tag.delete(context: Context) {
  deleteWithoutSync(context)
  deleteToSync()
}

fun Tag.deleteWithoutSync(context: Context) {
  if (isUnsaved()) {
    return
  }
  Tag.db(context).delete(this)
  uid = 0
}

fun Tag.deleteToSync() {
  // Notify change to online/offline sync
}
