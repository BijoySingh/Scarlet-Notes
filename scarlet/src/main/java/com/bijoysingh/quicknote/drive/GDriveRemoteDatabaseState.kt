package com.bijoysingh.quicknote.drive

import android.content.Context
import com.bijoysingh.quicknote.database.GDriveDataType
import com.bijoysingh.quicknote.database.RemoteDatabaseHelper
import com.bijoysingh.quicknote.database.remoteDatabase
import com.bijoysingh.quicknote.database.genRemoteDatabase
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.note.getFormats
import com.maubis.scarlet.base.database.remote.IRemoteDatabaseState
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.database.room.tag.Tag
import com.maubis.scarlet.base.support.utils.log
import com.maubis.scarlet.base.support.utils.maybeThrow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GDriveRemoteDatabaseState(context: Context) : IRemoteDatabaseState {

  init {
    remoteDatabase = genRemoteDatabase(context)
  }

  /**
   * Notify local changes to the notes
   */
  override fun notifyInsert(data: Any, onExecution: () -> Unit) {
    when {
      data is Tag -> localDatabaseUpdate(GDriveDataType.TAG, data.uuid, onExecution)
      data is Folder -> localDatabaseUpdate(GDriveDataType.FOLDER, data.uuid, onExecution)
      data is Note -> notifyNoteInsertImpl(data, onExecution)
      else -> maybeThrow("notifyInsert called with unhandled data type")
    }
  }

  fun notifyInsertIfNotPresent(data: Any) {
    val database = remoteDatabase
    if (database === null) {
      return
    }

    when {
      data is Tag -> {
        if (database.getByUUID(GDriveDataType.TAG.name, data.uuid) === null) {
          notifyInsert(data) {}
        }
      }
      data is Folder -> {
        if (database.getByUUID(GDriveDataType.FOLDER.name, data.uuid) === null) {
          notifyInsert(data) {}
        }
      }
      data is Note -> {
        if (database.getByUUID(GDriveDataType.NOTE.name, data.uuid) === null
            || database.getByUUID(GDriveDataType.NOTE_META.name, data.uuid) === null) {
          notifyInsert(data) {}
        }
      }
      else -> maybeThrow("notifyInsert called with unhandled data type")
    }
  }

  private fun notifyNoteInsertImpl(note: Note, onExecution: () -> Unit) {
    val noteUuid = note.uuid
    localDatabaseUpdate(GDriveDataType.NOTE, noteUuid, onExecution)
    localDatabaseUpdate(GDriveDataType.NOTE_META, noteUuid, onExecution)

    val database = remoteDatabase
    if (database === null) {
      return
    }

    GlobalScope.launch {
      val imageUUIDs = HashSet<ImageUUID>()
      notifyImageIds(note) { imageUUIDs.add(it) }

      database.getByType(GDriveDataType.IMAGE.name)
          .filter {
            val uuid = toImageUUID(it.uuid)
            uuid?.noteUuid == note.uuid && !imageUUIDs.contains(uuid)
          }.forEach {
            it.apply {
              lastUpdateTimestamp = getTrueCurrentTime()
              localStateDeleted = true
              save(database)
            }
          }

      imageUUIDs.forEach {
        val existing = database.getByUUID(GDriveDataType.IMAGE.name, it.name())
        if (existing === null) {
          localDatabaseUpdate(GDriveDataType.IMAGE, it.name(), {}, false)
        }
      }
    }
  }

  override fun notifyRemove(data: Any, onExecution: () -> Unit) {
    when {
      data is Tag -> localDatabaseUpdate(GDriveDataType.TAG, data.uuid, onExecution, true)
      data is Folder -> localDatabaseUpdate(GDriveDataType.FOLDER, data.uuid, onExecution, true)
      data is Note -> {
        localDatabaseUpdate(GDriveDataType.NOTE, data.uuid, onExecution, true)
        localDatabaseUpdate(GDriveDataType.NOTE_META, data.uuid, onExecution, true)
      }
      else -> maybeThrow("notifyRemove called with unhandled data type")
    }
  }

  /**
   * Deletes the item from the database, to allow for deletion
   * in case the user logs out and the data is deleted
   */
  fun stopTrackingItem(data: Any, onExecution: () -> Unit) {
    when {
      data is Tag -> localDatabaseRemove(GDriveDataType.TAG, data.uuid, onExecution)
      data is Folder -> localDatabaseRemove(GDriveDataType.FOLDER, data.uuid, onExecution)
      data is Note -> {
        localDatabaseRemove(GDriveDataType.NOTE, data.uuid, onExecution)
        localDatabaseRemove(GDriveDataType.NOTE_META, data.uuid, onExecution)
      }
      else -> maybeThrow("removeFromDb called with unhandled data type")
    }
  }

  /**
   * Notifies that an attempt to update this item was made.
   * If this number is over 10, we will delete the item to prevent issues
   *
   * @return if false, the item will be deleted from the database
   */
  fun notifyAttempt(itemType: GDriveDataType, itemUUID: String): Boolean {
    val database = remoteDatabase
    if (database === null) {
      return false
    }

    val existing = RemoteDatabaseHelper.getByUUID(itemType, itemUUID)
    val lastAttemptedTime = existing.lastAttemptTime

    // If it fails 8 times, only re-attempt after hour. This handles situations like no-network conditions
    val reAttempt = (existing.attempts >= 8 && (getTrueCurrentTime() - lastAttemptedTime > 1000 * 60 * 60))
    existing.apply {
      attempts = when {
        (attempts < 8) -> attempts + 1
        reAttempt -> 0
        else -> attempts
      }
      lastAttemptTime = getTrueCurrentTime()
      save(database)
    }
    return existing.attempts < 8
  }

  fun remoteDatabaseUpdate(itemType: GDriveDataType, itemUUID: String, onExecution: () -> Unit) {
    GlobalScope.launch {
      val database = remoteDatabase
      if (database === null) {
        return@launch
      }

      log("GDrive", "remoteDatabaseUpdate(${itemType.name}, $itemUUID)")
      val existing = RemoteDatabaseHelper.getByUUID(itemType, itemUUID)
      existing.apply {
        attempts = 0
        lastAttemptTime = 0
        lastUpdateTimestamp = remoteUpdateTimestamp
        localStateDeleted = remoteStateDeleted
        save(database)
      }
      onExecution()
    }
  }

  fun localDatabaseUpdate(
      itemType: GDriveDataType,
      itemUUID: String,
      onExecution: () -> Unit,
      removed: Boolean = false) {
    GlobalScope.launch {
      val database = remoteDatabase
      if (database === null) {
        return@launch
      }

      log("GDrive", "localDatabaseUpdate(${itemType.name}, $itemUUID)")
      val existing = RemoteDatabaseHelper.getByUUID(itemType, itemUUID)
      existing.apply {
        attempts = 0
        lastAttemptTime = 0
        lastUpdateTimestamp = Math.max(Math.max(remoteUpdateTimestamp + 1, lastUpdateTimestamp + 1), getTrueCurrentTime())
        localStateDeleted = removed
        save(database)
      }
      onExecution()
    }
  }

  fun localDatabaseRemove(
      itemType: GDriveDataType,
      itemUUID: String,
      onExecution: () -> Unit) {
    GlobalScope.launch {
      val database = remoteDatabase
      if (database !== null) {
        val existing = database.getByUUID(itemType.name, itemUUID)
        if (existing !== null) {
          database.delete(existing)
        }
      }
      onExecution()
    }
  }

  private fun notifyImageIds(note: Note, onImageUUID: (ImageUUID) -> Unit) {
    val imageIds = note.getFormats()
        .filter { it.formatType == FormatType.IMAGE }
        .map { it.text }
        .toSet()
    imageIds.forEach {
      onImageUUID(ImageUUID(note.uuid, it))
    }
  }
}