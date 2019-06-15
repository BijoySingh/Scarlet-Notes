package com.bijoysingh.quicknote.drive

import com.bijoysingh.quicknote.database.GDriveDataType
import com.bijoysingh.quicknote.database.GDriveDatabaseHelper
import com.bijoysingh.quicknote.database.GDriveUploadData
import com.bijoysingh.quicknote.database.GDriveUploadDataDao
import com.google.api.services.drive.model.File
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class GDriveRemoteFolderBase(
    val dataType: GDriveDataType,
    val database: GDriveUploadDataDao,
    val helper: GDriveServiceHelper,
    val onPendingChange: () -> Unit,
    val onPendingSyncComplete: () -> Unit) {

  protected fun notifyDriveData(file: File, deleted: Boolean = false) {
    val modifiedTime = file.modifiedTime?.value ?: file.modifiedByMeTime?.value ?: 0L
    notifyDriveData(file.id, file.name, modifiedTime, deleted)
  }

  protected fun notifyDriveData(uid: String, name: String, modifiedTime: Long, deleted: Boolean = false) {
    GlobalScope.launch {
      val uploadData = GDriveDatabaseHelper.getByUUID(dataType, name)
      if (uploadData.unsaved()) {
        uploadData.apply {
          fileId = uid
          gDriveUpdateTimestamp = modifiedTime
          gDriveStateDeleted = deleted
          save(database)
        }
        onPendingChange()
        return@launch
      }

      if (uploadData.gDriveStateDeleted != deleted) {
        uploadData.apply {
          gDriveUpdateTimestamp = modifiedTime
          fileId = uid
          gDriveStateDeleted = deleted
          save(database)
        }
        onPendingChange()
        return@launch
      }

      if (uploadData.fileId != uid && uploadData.gDriveUpdateTimestamp < modifiedTime) {
        // This is a bit of shit situation to be in, as multiple files are pointing to the
        // same name... Something Google Drive allows for whatever reason
        // To help disambiguate, choose the one with the higher update timestamp
        uploadData.apply {
          gDriveUpdateTimestamp = modifiedTime
          fileId = uid
          gDriveStateDeleted = deleted
          save(database)
        }
        onPendingChange()
      }

      if (uploadData.fileId == uid && uploadData.gDriveUpdateTimestamp != modifiedTime) {
        uploadData.apply {
          gDriveUpdateTimestamp = modifiedTime
          fileId = uid
          gDriveStateDeleted = deleted
          save(database)
        }
        onPendingChange()
      }
    }
  }
}