package com.bijoysingh.quicknote.drive

import android.content.Context
import com.bijoysingh.quicknote.Scarlet.Companion.remoteConfig
import com.bijoysingh.quicknote.database.RemoteController
import com.bijoysingh.quicknote.database.RemoteDataType
import com.bijoysingh.quicknote.database.RemoteUploadData
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.google.gson.Gson
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.export.data.getExportableFolder
import com.maubis.scarlet.base.export.data.getExportableNoteMeta
import com.maubis.scarlet.base.export.data.getExportableTag
import com.maubis.scarlet.base.export.data.toExportedMarkdown
import com.maubis.scarlet.base.support.utils.log
import java.lang.ref.WeakReference

class GDriveRemoteDatabase(weakContext: WeakReference<Context>) : RemoteController<String, File, FileList>(weakContext) {
  override fun initSyncs() {
    notesSync = GDriveRemoteFolder(
        dataType = RemoteDataType.NOTE,
        database = remoteDatabase,
        service = remoteService as GDriveServiceHelper,
        onPendingChange = { verifyAndNotifyPendingStateChange() },
        serialiser = { it },
        uuidToObject = {
          ApplicationBase.instance.notesDatabase().getByUUID(it)?.toExportedMarkdown()
        })
    notesMetaSync = GDriveRemoteFolder(
        dataType = RemoteDataType.NOTE_META,
        database = remoteDatabase,
        service = remoteService as GDriveServiceHelper,
        onPendingChange = { verifyAndNotifyPendingStateChange() },
        serialiser = { Gson().toJson(it) },
        uuidToObject = {
          ApplicationBase.instance.notesDatabase().getByUUID(it)?.getExportableNoteMeta()
        })
    tagsSync = GDriveRemoteFolder(
        dataType = RemoteDataType.TAG,
        database = remoteDatabase,
        service = remoteService as GDriveServiceHelper,
        onPendingChange = { verifyAndNotifyPendingStateChange() },
        serialiser = { Gson().toJson(it) },
        uuidToObject = {
          ApplicationBase.instance.tagsDatabase().getByUUID(it)?.getExportableTag()
        })
    foldersSync = GDriveRemoteFolder(
        dataType = RemoteDataType.FOLDER,
        database = remoteDatabase,
        service = remoteService as GDriveServiceHelper,
        onPendingChange = { verifyAndNotifyPendingStateChange() },
        serialiser = { Gson().toJson(it) },
        uuidToObject = {
          ApplicationBase.instance.foldersDatabase().getByUUID(it)?.getExportableFolder()
        })
    imageSync = GDriveRemoteImageFolder(
        dataType = RemoteDataType.IMAGE,
        database = remoteDatabase,
        service = remoteService as GDriveServiceHelper,
        onPendingChange = { verifyAndNotifyPendingStateChange() })
  }

  override fun getResourceIdForFolderName(folderName: String): String? {
    val folderId = folderIdForFolderName(folderName)
    return when {
      folderId.isBlank() -> null
      else -> folderId
    }
  }

  override fun storeResourceIdForFolderName(folderName: String, resource: String) {
    folderIdForFolderName(folderName, resource)
  }

  override fun getResourceId(data: RemoteUploadData): String {
    return data.fileId
  }

  private fun folderIdForFolderName(folderName: String, folderId: String = ""): String {
    val key = "g_drive_folder_if_for_$folderName"
    if (folderId.isEmpty()) {
      // Get Condition
      var storedValue = remoteConfig.get(key, "") ?: ""
      if (storedValue == INVALID_FILE_ID) {
        remoteConfig.put(key, "")
        storedValue = ""
      }
      log("GDrive", "folderIdForFolderName($folderName, $storedValue")
      return storedValue
    }

    if (folderId != INVALID_FILE_ID) {
      log("GDrive", "folderIdForFolderName($folderName, $folderId")
      remoteConfig.put(key, folderId)
    }
    return folderId
  }
}