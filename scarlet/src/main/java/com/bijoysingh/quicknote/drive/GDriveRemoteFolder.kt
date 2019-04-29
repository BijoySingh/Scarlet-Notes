package com.bijoysingh.quicknote.drive

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

const val KEY_G_DRIVE_SYNC_LAST_SCAN = "drive_g_folder_sync_last_sync"
const val G_DRIVE_LAST_MODIFIED_ERROR_MARGIN = 7 * 1000 * 60 * 60 * 24L
const val G_DRIVE_LAST_MODIFIED_UPDATE_ERROR_MARGIN = 1000 * 60 * 60 * 1L

class GDriveRemoteFolder<T>(
    val helper: GDriveServiceHelper,
    val uuidToObject: (String) -> T?) {

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
      if (fileId !== null) {
        helper.saveFile(fileId, uuid, data).addOnCompleteListener {
          helper.updateLastModifiedTime(contentFolderUid)
        }
        return
      }

      helper.createFileWithData(contentFolderUid, uuid, data).addOnCompleteListener {
        contentFiles[uuid] = it.result ?: INVALID_FILE_ID
        helper.updateLastModifiedTime(contentFolderUid)
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

    helper.createFileWithData(deletedFolderUid, uuid).addOnCompleteListener {
      deletedFiles[uuid] = it.result ?: INVALID_FILE_ID
      helper.updateLastModifiedTime(deletedFolderUid)
    }
  }
}