package com.maubis.scarlet.base.note.folder

import com.github.bijoysingh.starter.util.DateFormatter
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.support.database.foldersDB
import java.util.*

fun Folder.saveIfUnique() {
  val existing = foldersDB.getByTitle(title)
  if (existing !== null) {
    this.uid = existing.uid
    this.uuid = existing.uuid
    return
  }

  val existingByUUID = foldersDB.getByUUID(uuid)
  if (existingByUUID != null) {
    this.uid = existingByUUID.uid
    this.title = existingByUUID.title
    return
  }
  save()
}

fun Folder.getDisplayTime(): String {
  val time = when {
    (this.updateTimestamp != 0L) -> this.updateTimestamp
    (this.timestamp != null) -> this.timestamp
    else -> 0
  }

  val format = when {
    Calendar.getInstance().timeInMillis - time < 1000 * 60 * 60 * 2 -> "hh:mm aa"
    else -> "dd MMMM"
  }
  return DateFormatter.getDate(format, time)
}

/**************************************************************************************
 ******************************* Database Functions ********************************
 **************************************************************************************/

fun Folder.save() {
  CoreConfig.instance.folderActions(this).save()
}

fun Folder.saveWithoutSync() {
  CoreConfig.instance.folderActions(this).offlineSave()
}

fun Folder.saveToSync() {
  CoreConfig.instance.folderActions(this).onlineSave()
}

fun Folder.delete() {
  CoreConfig.instance.folderActions(this).delete()
}

fun Folder.deleteWithoutSync() {
  CoreConfig.instance.folderActions(this).offlineDelete()
}