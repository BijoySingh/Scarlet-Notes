package com.bijoysingh.quicknote.database

import android.content.Context
import com.bijoysingh.quicknote.drive.ImageUUID
import com.bijoysingh.quicknote.drive.getTrueCurrentTime
import com.bijoysingh.quicknote.drive.toImageUUID
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

class RemoteDatabaseStateController(context: Context) : IRemoteDatabaseState {

  init {
    remoteDatabase = genRemoteDatabase(context)
  }

  /**
   * Notify local changes to the notes
   */
  override fun notifyInsert(data: Any, onExecution: () -> Unit) {
    when {
      data is Tag -> localDatabaseUpdate(RemoteDataType.TAG, data.uuid, onExecution)
      data is Folder -> localDatabaseUpdate(RemoteDataType.FOLDER, data.uuid, onExecution)
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
        if (database.getByUUID(RemoteDataType.TAG.name, data.uuid) === null) {
          notifyInsert(data) {}
        }
      }
      data is Folder -> {
        if (database.getByUUID(RemoteDataType.FOLDER.name, data.uuid) === null) {
          notifyInsert(data) {}
        }
      }
      data is Note -> {
        if (database.getByUUID(RemoteDataType.NOTE.name, data.uuid) === null
            || database.getByUUID(RemoteDataType.NOTE_META.name, data.uuid) === null) {
          notifyInsert(data) {}
        }
      }
      else -> maybeThrow("notifyInsert called with unhandled data type")
    }
  }

  private fun notifyNoteInsertImpl(note: Note, onExecution: () -> Unit) {
    val noteUuid = note.uuid
    localDatabaseUpdate(RemoteDataType.NOTE, noteUuid, onExecution)
    localDatabaseUpdate(RemoteDataType.NOTE_META, noteUuid, onExecution)

    val database = remoteDatabase
    if (database === null) {
      return
    }

    GlobalScope.launch {
      val imageUUIDs = HashSet<ImageUUID>()
      notifyImageIds(note) { imageUUIDs.add(it) }

      database.getByType(RemoteDataType.IMAGE.name)
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
        val existing = database.getByUUID(RemoteDataType.IMAGE.name, it.name())
        if (existing === null) {
          localDatabaseUpdate(RemoteDataType.IMAGE, it.name(), {}, false)
        }
      }
    }
  }

  override fun notifyRemove(data: Any, onExecution: () -> Unit) {
    when {
      data is Tag -> localDatabaseUpdate(RemoteDataType.TAG, data.uuid, onExecution, true)
      data is Folder -> localDatabaseUpdate(RemoteDataType.FOLDER, data.uuid, onExecution, true)
      data is Note -> {
        localDatabaseUpdate(RemoteDataType.NOTE, data.uuid, onExecution, true)
        localDatabaseUpdate(RemoteDataType.NOTE_META, data.uuid, onExecution, true)
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
      data is Tag -> localDatabaseRemove(RemoteDataType.TAG, data.uuid, onExecution)
      data is Folder -> localDatabaseRemove(RemoteDataType.FOLDER, data.uuid, onExecution)
      data is Note -> {
        localDatabaseRemove(RemoteDataType.NOTE, data.uuid, onExecution)
        localDatabaseRemove(RemoteDataType.NOTE_META, data.uuid, onExecution)
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
  fun notifyAttempt(itemType: RemoteDataType, itemUUID: String): Boolean {
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

  fun remoteDatabaseUpdate(itemType: RemoteDataType, itemUUID: String, onExecution: () -> Unit) {
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
      itemType: RemoteDataType,
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
      itemType: RemoteDataType,
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