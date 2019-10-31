package com.bijoysingh.quicknote.drive

import com.bijoysingh.quicknote.database.RemoteDataType
import com.bijoysingh.quicknote.database.RemoteDatabaseHelper
import com.bijoysingh.quicknote.database.RemoteFolder
import com.bijoysingh.quicknote.database.RemoteUploadDataDao
import com.google.api.services.drive.model.File
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class GDriveRemoteFolderBase<T>(
  val dataType: RemoteDataType,
  val database: RemoteUploadDataDao,
  val service: GDriveServiceHelper,
  val onPendingChange: () -> Unit) : RemoteFolder<String, T> {

  protected fun notifyDriveData(file: File, deleted: Boolean = false) {
    val modifiedTime = file.modifiedTime?.value ?: file.modifiedByMeTime?.value ?: 0L
    notifyDriveData(file.id, file.name, modifiedTime, deleted)
  }

  protected fun notifyDriveData(uid: String, name: String, modifiedTime: Long, deleted: Boolean = false) {
    GlobalScope.launch {
      val uploadData = RemoteDatabaseHelper.getByUUID(dataType, name)
      if (uploadData.unsaved()) {
        uploadData.apply {
          fileId = uid
          remoteUpdateTimestamp = modifiedTime
          remoteStateDeleted = deleted
          save(database)
        }
        onPendingChange()
        return@launch
      }

      if (uploadData.remoteStateDeleted != deleted) {
        uploadData.apply {
          remoteUpdateTimestamp = modifiedTime
          fileId = uid
          remoteStateDeleted = deleted
          save(database)
        }
        onPendingChange()
        return@launch
      }

      if (uploadData.fileId != uid && uploadData.remoteUpdateTimestamp < modifiedTime) {
        // This is a bit of shit situation to be in, as multiple files are pointing to the
        // same name... Something Google Drive allows for whatever reason
        // To help disambiguate, choose the one with the higher update timestamp
        uploadData.apply {
          remoteUpdateTimestamp = modifiedTime
          fileId = uid
          remoteStateDeleted = deleted
          save(database)
        }
        onPendingChange()
      }

      if (uploadData.fileId == uid && uploadData.remoteUpdateTimestamp != modifiedTime) {
        uploadData.apply {
          remoteUpdateTimestamp = modifiedTime
          fileId = uid
          remoteStateDeleted = deleted
          save(database)
        }
        onPendingChange()
      }
    }
  }
}