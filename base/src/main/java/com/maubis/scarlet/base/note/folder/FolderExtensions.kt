package com.maubis.scarlet.base.note.folder

import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.CoreConfig.Companion.foldersDb
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.support.utils.sDateFormat
import java.util.*

fun Folder.saveIfUnique() {
  val existing = foldersDb.getByTitle(title)
  if (existing !== null) {
    this.uid = existing.uid
    this.uuid = existing.uuid
    return
  }

  val existingByUUID = foldersDb.getByUUID(uuid)
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
  return sDateFormat.readableTime(format, time)
}

/**************************************************************************************
 ******************************* Database Functions ********************************
 **************************************************************************************/

fun Folder.save() {
  ApplicationBase.instance.folderActions(this).save()
}

fun Folder.saveWithoutSync() {
  ApplicationBase.instance.folderActions(this).offlineSave()
}

fun Folder.saveToSync() {
  ApplicationBase.instance.folderActions(this).onlineSave()
}

fun Folder.delete() {
  ApplicationBase.instance.folderActions(this).delete()
}

fun Folder.deleteWithoutSync() {
  ApplicationBase.instance.folderActions(this).offlineDelete()
}