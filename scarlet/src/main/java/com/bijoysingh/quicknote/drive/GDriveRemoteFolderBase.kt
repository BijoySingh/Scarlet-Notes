package com.bijoysingh.quicknote.drive

import com.bijoysingh.quicknote.database.GDriveDataType
import com.bijoysingh.quicknote.database.GDriveUploadData
import com.bijoysingh.quicknote.database.GDriveUploadDataDao
import com.google.api.services.drive.model.File
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class GDriveRemoteFolderBase(
    val dataType: GDriveDataType,
    val database: GDriveUploadDataDao,
    val helper: GDriveServiceHelper) {

  protected fun notifyDriveData(file: File, deleted: Boolean = false) {
    val modifiedTime = file.modifiedTime?.value ?: 0L
    notifyDriveData(file.id, file.name, modifiedTime, deleted)
  }

  protected fun notifyDriveData(uid: String, name: String, modifiedTime: Long, deleted: Boolean = false) {
    GlobalScope.launch {
      val uploadData = database.getByUUID(dataType.name, name)
      if (uploadData == null) {
        GDriveUploadData().apply {
          uuid = name
          type = dataType.name
          fileId = uid
          gDriveUpdateTimestamp = modifiedTime
          gDriveStateDeleted = deleted
          save(database)
        }
        return@launch
      }


      if (uploadData.gDriveUpdateTimestamp != modifiedTime
          || uploadData.fileId != uid
          || uploadData.gDriveStateDeleted != deleted) {
        uploadData.apply {
          gDriveUpdateTimestamp = modifiedTime
          fileId = uid
          gDriveStateDeleted = deleted
          save(database)
        }
      }
    }
  }
}