package com.bijoysingh.quicknote.drive

import com.bijoysingh.quicknote.firebase.data.FirebaseFolder
import com.bijoysingh.quicknote.firebase.data.FirebaseNote
import com.google.gson.Gson
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.export.remote.LAST_MODIFIED_ERROR_MARGIN
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

const val KEY_G_DRIVE_SYNC_LAST_SCAN = "drive_g_folder_sync_last_sync"

class GDriveRemoteFolder<T>(
    val klass: Class<T>,
    val helper: GDriveServiceHelper,
    val uuidToObject: (String) -> T?,
    val onRemoteInsert: (T) -> Unit,
    val onRemoteDelete: (String) -> Unit) {

  // Mapping from uuid => fileIds
  var deletedFileId: String? = null
  val loaded = AtomicBoolean(false)
  val fileIds = emptyMap<String, String>().toMutableMap()
  val pendingActions = emptyMap<String, String>().toMutableMap()
  val deletedUuids = HashSet<String>()
  var folderUid: String = INVALID_FILE_ID

  fun init(fUid: String, onLoaded: () -> Unit) {
    folderUid = fUid
    val lastScanKey = "${KEY_G_DRIVE_SYNC_LAST_SCAN}_$folderUid"
    val lastScan = CoreConfig.instance.store().get(lastScanKey, 0L)

    GlobalScope.launch(Dispatchers.IO) {
      helper.getFilesInFolder(folderUid).addOnCompleteListener {
        val files = it.result?.files
        if (files !== null) {
          files.forEach { file ->
            if (file.mimeType == GOOGLE_DRIVE_FILE_MIME_TYPE
                && (file.modifiedTime?.value ?: System.currentTimeMillis() > lastScan - LAST_MODIFIED_ERROR_MARGIN)) {
              fileIds[file.name] = file.id
              helper.readFile(file.id).addOnCompleteListener {
                val data = it.result
                if (data !== null) {
                  try {
                    val item = Gson().fromJson(data, klass)
                    if (item !== null) {
                      onRemoteInsert(item)
                    }
                  } catch (exception: Exception) {
                  }
                }
              }
            }
          }
          loaded.set(true)
          removePendingActions()
          scanDeletedFiles()
          GlobalScope.launch { onLoaded() }
        }
      }
    }
  }

  fun scanDeletedFiles() {
    helper.getOrCreateDirectory(folderUid, "delete") {
      deletedFileId = it
      if (deletedFileId !== null) {
        helper.getFilesInFolder(deletedFileId!!).addOnCompleteListener {
          val files = it.result?.files
          if (files !== null) {
            files.forEach { file ->
              if (file.mimeType == GOOGLE_DRIVE_FILE_MIME_TYPE) {
                deletedUuids.add(file.name)
                onRemoteDelete(file.name)
              }
            }
          }
        }
        removePendingActions()
      }
    }
  }

  fun removePendingActions() {
    val tPendingActions = emptyMap<String, String>().toMutableMap()
    tPendingActions.putAll(pendingActions)
    pendingActions.clear()

    GlobalScope.launch {
      tPendingActions.forEach {
        when (it.value) {
          "insert" -> {
            val item = uuidToObject(it.key)
            if (item !== null) {
              insert(it.key, item)
            }
          }
          "delete" -> delete(it.key)
        }
      }
    }
  }

  fun insert(uuid: String, item: T) {
    if (!loaded.get()) {
      pendingActions[uuid] = "insert"
      return
    }

    try {
      val data = Gson().toJson(item)
      val fileId = fileIds.get(uuid)
      if (fileId !== null) {
        helper.saveFile(fileId, uuid, data)
        return
      }

      val modificationTime = when {
        item is FirebaseNote -> item.updateTimestamp
        item is FirebaseFolder -> item.updateTimestamp
        else -> null
      }
      helper.createFile(folderUid, modificationTime).addOnCompleteListener {
        val createdFileId = it.result
        if (createdFileId !== null) {
          fileIds[uuid] = createdFileId
          helper.saveFile(createdFileId, uuid, data)
        }
      }
    } catch (exception: Exception) {
    }
  }

  fun delete(uuid: String) {
    if (!loaded.get() || deletedFileId === null) {
      pendingActions[uuid] = "delete"
      return
    }

    fileIds.remove(uuid)
    helper.createFile(deletedFileId!!).addOnCompleteListener {
      val removeFileId = it.result
      if (removeFileId !== null) {
        helper.saveFile(removeFileId, uuid, System.currentTimeMillis().toString())
      }
    }
  }
}