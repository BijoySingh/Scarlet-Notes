package com.bijoysingh.quicknote.drive

import com.bijoysingh.quicknote.database.GDriveDataType
import com.bijoysingh.quicknote.database.GDriveUploadDataDao
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class GDriveRemoteFolder<T>(
    dataType: GDriveDataType,
    database: GDriveUploadDataDao,
    helper: GDriveServiceHelper,
    val uuidToObject: (String) -> T?): GDriveRemoteFolderBase(dataType, database, helper) {

  var contentLoading = AtomicBoolean(true)
  var contentFolderUid: String = INVALID_FILE_ID
  var contentPendingActions = emptySet<String>().toMutableSet()
  val contentFiles = emptyMap<String, String>().toMutableMap()

  var deletedLoading = AtomicBoolean(true)
  var deletedFolderUid: String = INVALID_FILE_ID
  var deletedPendingActions = emptySet<String>().toMutableSet()
  val deletedFiles = emptyMap<String, String>().toMutableMap()

  fun initContentFolderId(fUid: String, onLoaded: () -> Unit) {
    GlobalScope.launch(Dispatchers.IO) {
      contentLoading.set(true)
      contentFolderUid = fUid
      helper.getFilesInFolder(contentFolderUid).addOnCompleteListener {
        val files = it.result?.files ?: emptyList()
        val localFileIds = emptyMap<String, String>().toMutableMap()
        files.forEach { file ->
          localFileIds[file.name] = file.id
          notifyDriveData(file)
        }
        contentFiles.clear()
        contentFiles.putAll(localFileIds)
        contentLoading.set(false)

        GlobalScope.launch { executeInsertPendingActions() }
        GlobalScope.launch { onLoaded() }
      }
    }
  }

  fun initDeletedFolderId(fUid: String, onLoaded: () -> Unit) {
    GlobalScope.launch(Dispatchers.IO) {
      deletedLoading.set(true)
      deletedFolderUid = fUid
      helper.getFilesInFolder(deletedFolderUid).addOnCompleteListener {
        val files = it.result?.files ?: emptyList()
        val localFileIds = emptyMap<String, String>().toMutableMap()
        files.forEach { file ->
          localFileIds[file.name] = file.id
          notifyDriveData(file, true)
        }
        deletedFiles.clear()
        deletedFiles.putAll(localFileIds)
        deletedLoading.set(false)

        GlobalScope.launch { executeDeletePendingActions() }
        GlobalScope.launch { onLoaded() }
      }
    }
  }

  fun executeInsertPendingActions() {
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
    deletedPendingActions.forEach {
      GlobalScope.launch { delete(it) }
    }
  }

  fun insert(uuid: String, item: T) {
    if (contentLoading.get()) {
      contentPendingActions.add(uuid)
      return
    }

    try {
      val data = Gson().toJson(item)
      val fileId = contentFiles[uuid]
      val timestamp = database.getByUUID(dataType.name, uuid)?.lastUpdateTimestamp ?: getTrueCurrentTime()
      if (fileId !== null) {
        helper.saveFile(fileId, uuid, data, timestamp).addOnCompleteListener {
          val file = it.result
          if (file !== null) {
            notifyDriveData(file.id, uuid, timestamp)
          }
        }
        return
      }
      helper.createFileWithData(contentFolderUid, uuid, data, timestamp).addOnCompleteListener {
        val file = it.result
        if (file !== null) {
          contentFiles[uuid] = file.id
          notifyDriveData(file.id, uuid, timestamp)
        }
      }
    } catch (exception: Exception) {
    }
  }

  fun delete(uuid: String) {
    if (deletedLoading.get() || contentLoading.get()) {
      deletedPendingActions.add(uuid)
      return
    }

    val existingFileUid = contentFiles[uuid]
    if (existingFileUid !== null) {
      helper.removeFileOrFolder(existingFileUid)
      contentFiles.remove(uuid)
    }

    val timestamp = database.getByUUID(dataType.name, uuid)?.lastUpdateTimestamp ?: getTrueCurrentTime()
    helper.createFileWithData(deletedFolderUid, uuid, "", timestamp).addOnCompleteListener {
      val file = it.result
      if (file !== null) {
        deletedFiles[uuid] = file.id
        notifyDriveData(file.id, uuid, timestamp, true)
      }
    }
  }
}