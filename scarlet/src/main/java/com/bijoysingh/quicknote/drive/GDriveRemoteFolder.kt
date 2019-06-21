package com.bijoysingh.quicknote.drive

import com.bijoysingh.quicknote.database.GDriveDataType
import com.bijoysingh.quicknote.database.GDriveUploadDataDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class GDriveRemoteFolder<T>(
    dataType: GDriveDataType,
    database: GDriveUploadDataDao,
    helper: GDriveServiceHelper,
    onPendingChange: () -> Unit,
    onPendingSyncComplete: (String) -> Unit,
    val serialiser: (T) -> String,
    val uuidToObject: (String) -> T?) : GDriveRemoteFolderBase(dataType, database, helper, onPendingChange, onPendingSyncComplete) {

  var networkOrAbsoluteFailure = AtomicBoolean(false)

  var contentLoading = AtomicBoolean(true)
  var contentFolderUid: String = INVALID_FILE_ID
  var contentPendingActions = emptySet<String>().toMutableSet()
  val contentFiles = emptyMap<String, String>().toMutableMap()

  var deletedLoading = AtomicBoolean(true)
  var deletedFolderUid: String = INVALID_FILE_ID
  var deletedPendingActions = emptySet<String>().toMutableSet()
  val deletedFiles = emptyMap<String, String>().toMutableMap()

  val duplicateFilesToDelete: MutableList<String> = emptyList<String>().toMutableList()

  fun initContentFolderId(fUid: String, onLoaded: () -> Unit) {
    val logInfo = "initContentFolderId($fUid)"
    GlobalScope.launch(Dispatchers.IO) {
      contentLoading.set(true)
      contentFolderUid = fUid
      helper.getFilesInFolder(contentFolderUid).addOnCompleteListener {
        networkOrAbsoluteFailure.set(it.result === null)

        val files = it.result?.files ?: emptyList()
        val localFileIds = emptyMap<String, String>().toMutableMap()
        files.forEach { file ->
          if (localFileIds.containsKey(file.name)) {
            duplicateFilesToDelete.add(file.id)
          } else {
            localFileIds[file.name] = file.id
            notifyDriveData(file)
          }
        }
        contentFiles.clear()
        contentFiles.putAll(localFileIds)
        contentLoading.set(false)

        GlobalScope.launch { executeAllDuplicateDeletion() }
        GlobalScope.launch {
          executeInsertPendingActions()
          executeDeletePendingActions()
        }
        GlobalScope.launch { onLoaded() }
      }.addOnFailureListener {
        onPendingSyncComplete(logInfo)
      }.addOnCanceledListener {
        onPendingSyncComplete(logInfo)
      }
    }
  }

  fun initDeletedFolderId(fUid: String, onLoaded: () -> Unit) {
    val logInfo = "initDeletedFolderId($fUid)"
    if (fUid == INVALID_FILE_ID) {
      deletedLoading.set(false)
      GlobalScope.launch {
        executeInsertPendingActions()
        executeDeletePendingActions()
      }
      GlobalScope.launch { onLoaded() }
      return
    }

    GlobalScope.launch(Dispatchers.IO) {
      deletedLoading.set(true)
      deletedFolderUid = fUid
      helper.getFilesInFolder(deletedFolderUid).addOnCompleteListener {
        networkOrAbsoluteFailure.set(it.result === null)

        val files = it.result?.files ?: emptyList()
        val localFileIds = emptyMap<String, String>().toMutableMap()
        files.forEach { file ->
          when {
            localFileIds.containsKey(file.name) -> duplicateFilesToDelete.add(file.id)
            getTrueCurrentTime() - (file.modifiedTime?.value ?: 0L) > TimeUnit.DAYS.toMillis(7) -> duplicateFilesToDelete.add(file.id)
            else -> {
              localFileIds[file.name] = file.id
              notifyDriveData(file, true)
            }
          }
        }
        deletedFiles.clear()
        deletedFiles.putAll(localFileIds)
        deletedLoading.set(false)

        GlobalScope.launch { executeAllDuplicateDeletion() }
        GlobalScope.launch { executeDeletePendingActions() }
        GlobalScope.launch { onLoaded() }
      }.addOnFailureListener {
        onPendingSyncComplete(logInfo)
      }.addOnCanceledListener {
        onPendingSyncComplete(logInfo)
      }
    }
  }

  fun executeAllDuplicateDeletion() {
    val files = ArrayList<String>()
    files.addAll(duplicateFilesToDelete)
    duplicateFilesToDelete.clear()
    files.forEach { fileId ->
      helper.removeFileOrFolder(fileId)
    }
  }

  fun executeInsertPendingActions() {
    if (deletedLoading.get() || contentLoading.get()) {
      return
    }
    contentPendingActions.forEach { uuid ->
      GlobalScope.launch {
        val item = uuidToObject(uuid)
        if (item !== null) {
          insert(uuid, item)
        }
      }
    }
  }

  fun executeDeletePendingActions() {
    if (deletedLoading.get() || contentLoading.get()) {
      return
    }
    deletedPendingActions.forEach {
      GlobalScope.launch { delete(it) }
    }
  }

  /**
   * Insert the file on the server based on the insertion on the local device
   */
  fun insert(uuid: String, item: T) {
    val logInfo = "insert($uuid)"

    if (deletedLoading.get() || contentLoading.get()) {
      if (!contentPendingActions.add(uuid)) {
        onPendingSyncComplete(logInfo)
      }
      return
    }

    if (networkOrAbsoluteFailure.get()) {
      onPendingSyncComplete(logInfo)
      return
    }

    if (deletedFiles.containsKey(uuid)) {
      GlobalScope.launch {
        val existingFileUid = deletedFiles[uuid] ?: INVALID_FILE_ID
        helper.removeFileOrFolder(existingFileUid)
            .addOnCompleteListener {
              deletedFiles.remove(uuid)
            }
      }
    }

    val data = serialiser(item)
    val fileId = contentFiles[uuid]
    val existing = database.getByUUID(dataType.name, uuid)
    val timestamp = existing?.lastUpdateTimestamp ?: getTrueCurrentTime()

    if (fileId !== null) {
      helper.saveFile(fileId, uuid, data, timestamp)
          .addOnCompleteListener {
            val file = it.result
            if (file !== null) {
              notifyDriveData(file.id, uuid, timestamp)
            }
            onPendingSyncComplete(logInfo)
          }
          .addOnFailureListener { onPendingSyncComplete(logInfo) }
          .addOnCanceledListener { onPendingSyncComplete(logInfo) }
      return
    }
    helper.createFileWithData(contentFolderUid, uuid, data, timestamp)
        .addOnCompleteListener {
          val file = it.result
          if (file !== null) {
            contentFiles[uuid] = file.id
            notifyDriveData(file.id, uuid, timestamp)
          }
          onPendingSyncComplete(logInfo)
        }
        .addOnFailureListener { onPendingSyncComplete(logInfo) }
        .addOnCanceledListener { onPendingSyncComplete(logInfo) }

  }

  /**
   * Delete the file on the server based on removal on the local device
   */
  fun delete(uuid: String) {
    val logInfo = "delete($uuid)"
    if (deletedLoading.get() || contentLoading.get()) {
      if (!deletedPendingActions.add(uuid)) {
        onPendingSyncComplete(logInfo)
      }
      return
    }

    if (networkOrAbsoluteFailure.get()) {
      onPendingSyncComplete(logInfo)
      return
    }

    val existingFileUid = contentFiles[uuid]
    if (existingFileUid === null) {
      GlobalScope.launch {
        val existing = database.getByUUID(dataType.name, uuid)
        if (existing !== null) {
          database.delete(existing)
          onPendingChange()
        }
        onPendingSyncComplete(logInfo)
      }
      return
    }

    helper.removeFileOrFolder(existingFileUid)
        .addOnCompleteListener {
          contentFiles.remove(uuid)
          if (deletedFolderUid == INVALID_FILE_ID) {
            onPendingSyncComplete(logInfo)
            return@addOnCompleteListener
          }

          GlobalScope.launch {
            val timestamp = database.getByUUID(dataType.name, uuid)?.lastUpdateTimestamp
                ?: getTrueCurrentTime()
            helper.createFileWithData(deletedFolderUid, uuid, uuid, timestamp)
                .addOnCompleteListener {
                  val file = it.result
                  if (file !== null) {
                    deletedFiles[uuid] = file.id
                    notifyDriveData(file.id, uuid, timestamp, true)
                  }
                  onPendingSyncComplete(logInfo)
                }
                .addOnFailureListener { onPendingSyncComplete(logInfo) }
                .addOnCanceledListener { onPendingSyncComplete(logInfo) }
          }
        }
        .addOnFailureListener { onPendingSyncComplete(logInfo) }
        .addOnCanceledListener { onPendingSyncComplete(logInfo) }
  }
}