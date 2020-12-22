package com.bijoysingh.quicknote.drive

import com.bijoysingh.quicknote.database.RemoteDataType
import com.bijoysingh.quicknote.database.RemoteUploadData
import com.bijoysingh.quicknote.database.RemoteUploadDataDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class GDriveRemoteFolder<T>(
  dataType: RemoteDataType,
  database: RemoteUploadDataDao,
  service: GDriveServiceHelper,
  onPendingChange: () -> Unit,
  val serialiser: (T) -> String,
  val uuidToObject: (String) -> T?) : GDriveRemoteFolderBase<T>(dataType, database, service, onPendingChange) {

  private var networkOrAbsoluteFailure = AtomicBoolean(false)

  private var contentLoading = AtomicBoolean(true)
  private var contentFolderUid: String = INVALID_FILE_ID
  private var contentPendingActions = emptySet<String>().toMutableSet()
  private val contentFiles = emptyMap<String, String>().toMutableMap()

  private var deletedLoading = AtomicBoolean(true)
  private var deletedFolderUid: String = INVALID_FILE_ID
  private var deletedPendingActions = emptySet<String>().toMutableSet()
  private val deletedFiles = emptyMap<String, String>().toMutableMap()

  private val duplicateFilesToDelete: MutableList<String> = emptyList<String>().toMutableList()

  override fun initContentFolder(resourceId: String?, onSuccess: () -> Unit) {
    if (resourceId === null) {
      return
    }
    initContentFolderId(resourceId, onSuccess)
  }

  override fun initDeletedFolder(resourceId: String?, onSuccess: () -> Unit) {
    initDeletedFolderId(resourceId ?: INVALID_FILE_ID, onSuccess)
  }

  override fun insert(remoteData: RemoteUploadData, resource: T) {
    insert(remoteData.uuid, resource)
  }

  override fun delete(remoteData: RemoteUploadData) {
    delete(remoteData.uuid)
  }

  override fun invalidate() {
    networkOrAbsoluteFailure.set(false)
    contentLoading.set(true)
    contentFolderUid = INVALID_FILE_ID
    contentPendingActions.clear()
    contentFiles.clear()

    deletedLoading.set(true)
    deletedFolderUid = INVALID_FILE_ID
    deletedPendingActions.clear()
    deletedFiles.clear()
    duplicateFilesToDelete.clear()
  }

  private fun initContentFolderId(fUid: String, onLoaded: () -> Unit) {
    GlobalScope.launch(Dispatchers.IO) {
      contentLoading.set(true)
      contentFolderUid = fUid
      service.getFilesInFolder(contentFolderUid, GOOGLE_DRIVE_FILE_MIME_TYPE) { filesList ->
        networkOrAbsoluteFailure.set(filesList === null)

        val localFileIds = emptyMap<String, String>().toMutableMap()
        val files = filesList?.files ?: emptyList()
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
      }
    }
  }

  private fun initDeletedFolderId(fUid: String, onLoaded: () -> Unit) {
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

      service.getFilesInFolder(deletedFolderUid, GOOGLE_DRIVE_FILE_MIME_TYPE) { filesList ->
        networkOrAbsoluteFailure.set(filesList === null)

        val files = filesList?.files ?: emptyList()
        val localFileIds = emptyMap<String, String>().toMutableMap()
        files.forEach { file ->
          when {
            localFileIds.containsKey(file.name) -> duplicateFilesToDelete.add(file.id)
            getTrueCurrentTime() - (file.modifiedTime?.value
              ?: 0L) > TimeUnit.DAYS.toMillis(7) -> duplicateFilesToDelete.add(file.id)
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
      }
    }
  }

  private fun executeAllDuplicateDeletion() {
    val files = ArrayList<String>()
    files.addAll(duplicateFilesToDelete)
    duplicateFilesToDelete.clear()
    files.forEach { fileId ->
      service.removeFileOrFolder(fileId) {}
    }
  }

  private fun executeInsertPendingActions() {
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

  private fun executeDeletePendingActions() {
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
  private fun insert(uuid: String, resource: T) {
    if (deletedLoading.get() || contentLoading.get()) {
      return
    }

    if (networkOrAbsoluteFailure.get()) {
      return
    }

    if (deletedFiles.containsKey(uuid)) {
      GlobalScope.launch {
        val existingFileUid = deletedFiles[uuid] ?: INVALID_FILE_ID
        service.removeFileOrFolder(existingFileUid) { success ->
          if (success) {
            deletedFiles.remove(uuid)
          }
        }
      }
    }

    val data = serialiser(resource)
    val fileId = contentFiles[uuid]
    val existing = database.getByUUID(dataType.name, uuid)
    val timestamp = existing?.lastUpdateTimestamp ?: getTrueCurrentTime()

    if (fileId !== null) {
      service.updateFileWithData(fileId, uuid, data, timestamp) { file ->
        if (file !== null) {
          notifyDriveData(file.id, uuid, timestamp)
        }
      }
      return
    }
    service.createFileWithData(contentFolderUid, uuid, data, timestamp) { file ->
      if (file !== null) {
        contentFiles[uuid] = file.id
        notifyDriveData(file.id, uuid, timestamp)
      }
    }
  }

  /**
   * Delete the file on the server based on removal on the local device
   */
  private fun delete(uuid: String) {
    if (deletedLoading.get() || contentLoading.get()) {
      return
    }

    if (networkOrAbsoluteFailure.get()) {
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
      }
      return
    }

    service.removeFileOrFolder(existingFileUid) { success ->
      if (!success) {
        return@removeFileOrFolder
      }

      contentFiles.remove(uuid)
      if (deletedFolderUid == INVALID_FILE_ID) {
        return@removeFileOrFolder
      }
      GlobalScope.launch {
        val timestamp = database.getByUUID(dataType.name, uuid)?.lastUpdateTimestamp
          ?: getTrueCurrentTime()
        service.createFileWithData(deletedFolderUid, uuid, uuid, timestamp) { file ->
          if (file !== null) {
            deletedFiles[uuid] = file.id
            notifyDriveData(file.id, uuid, timestamp, true)
          }
        }
      }
    }
  }
}