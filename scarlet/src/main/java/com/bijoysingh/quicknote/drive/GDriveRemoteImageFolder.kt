package com.bijoysingh.quicknote.drive

import com.bijoysingh.quicknote.database.RemoteDataType
import com.bijoysingh.quicknote.database.RemoteUploadData
import com.bijoysingh.quicknote.database.RemoteUploadDataDao
import com.maubis.scarlet.base.config.ApplicationBase.Companion.noteImagesFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

data class ImageUUID(
    val noteUuid: String,
    val imageUuid: String) {

  fun name(): String = "$noteUuid::$imageUuid"

  override fun equals(other: Any?): Boolean {
    if (other !is ImageUUID) {
      return false
    }
    return other.imageUuid == imageUuid && other.noteUuid == noteUuid
  }

  override fun hashCode(): Int {
    return Objects.hash(noteUuid, imageUuid)
  }
}

fun toImageUUID(imageUuid: String): ImageUUID? {
  val components = imageUuid.split("::")
  if (components.size == 2) {
    val noteUuid = components[0]
    val imageId = components[1]
    return ImageUUID(noteUuid, imageId)
  }
  return null
}

class GDriveRemoteImageFolder(
    dataType: RemoteDataType,
    database: RemoteUploadDataDao,
    service: GDriveServiceHelper,
    onPendingChange: () -> Unit) : GDriveRemoteFolderBase<File>(dataType, database, service, onPendingChange) {

  private val networkOrAbsoluteFailure = AtomicBoolean(false)

  private val contentLoading = AtomicBoolean(true)
  private var contentFolderUid: String = INVALID_FILE_ID
  private val contentFiles = emptyMap<ImageUUID, String>().toMutableMap()

  private val contentPendingActions = emptySet<ImageUUID>().toMutableSet()
  private val deletedPendingActions = emptySet<ImageUUID>().toMutableSet()

  override fun initContentFolder(resourceId: String?, onSuccess: () -> Unit) {
    if (resourceId === null) {
      return
    }
    initContentFolderId(resourceId, onSuccess)
  }

  override fun initDeletedFolder(resourceId: String?, onSuccess: () -> Unit) {
    // Ignore
  }

  override fun insert(remoteData: RemoteUploadData, resource: File) {
    val image = toImageUUID(remoteData.uuid)
    if (image !== null) {
      insert(image)
    }
  }

  override fun delete(remoteData: RemoteUploadData) {
    val image = toImageUUID(remoteData.uuid)
    if (image !== null) {
      delete(image)
    }
  }

  override fun invalidate() {
    networkOrAbsoluteFailure.set(false)

    contentLoading.set(true)
    contentFolderUid = INVALID_FILE_ID
    contentFiles.clear()

    contentPendingActions.clear()
    deletedPendingActions.clear()
  }

  private fun initContentFolderId(fUid: String, onLoaded: () -> Unit) {
    contentFolderUid = fUid
    GlobalScope.launch(Dispatchers.IO) {

      service.getFilesInFolder(contentFolderUid, GOOGLE_DRIVE_IMAGE_MIME_TYPE) { filesList ->
        networkOrAbsoluteFailure.set(filesList === null)

        val imageFiles = filesList?.files
        if (imageFiles !== null) {
          imageFiles.forEach { imageFile ->
            val components = toImageUUID(imageFile.name)
            if (components !== null) {
              contentFiles[components] = imageFile.id
              notifyDriveData(imageFile)
            }
          }
          contentLoading.set(false)
        }
        GlobalScope.launch { onLoaded() }
      }
    }
  }

  private fun insert(id: ImageUUID) {
    if (contentLoading.get()) {
      contentPendingActions.add(id)
      return
    }

    if (networkOrAbsoluteFailure.get()) {
      return
    }

    if (contentFiles.containsKey(id)) {
      GlobalScope.launch {
        database.getByUUID(dataType.name, id.name())?.apply {
          remoteUpdateTimestamp = lastUpdateTimestamp
          remoteStateDeleted = localStateDeleted
          save(database)
        }
        onPendingChange()
      }
      return
    }

    val gDriveUUID = id.name()
    val timestamp = database.getByUUID(dataType.name, gDriveUUID)?.lastUpdateTimestamp
        ?: getTrueCurrentTime()
    val imageFile = noteImagesFolder.getFile(id.noteUuid, id.imageUuid)
    service.createFileFromFile(contentFolderUid, gDriveUUID, imageFile, timestamp) { file ->
      if (file !== null) {
        contentFiles[id] = file.id
        notifyDriveData(file.id, gDriveUUID, timestamp)
      }
    }
  }

  private fun delete(id: ImageUUID) {
    if (contentLoading.get()) {
      deletedPendingActions.add(id)
      return
    }

    if (networkOrAbsoluteFailure.get()) {
      return
    }

    if (!contentFiles.containsKey(id)) {
      GlobalScope.launch {
        val existing = database.getByUUID(dataType.name, id.name())
        if (existing !== null) {
          database.delete(existing)
        }
      }
      return
    }

    GlobalScope.launch {
      val fuid = contentFiles[id] ?: INVALID_FILE_ID
      val existing = database.getByUUID(dataType.name, id.name())
      val timestamp = existing?.lastUpdateTimestamp ?: getTrueCurrentTime()

      service.removeFileOrFolder(fuid) { success ->
        if (success) {
          notifyDriveData(fuid, id.name(), timestamp, true)
          contentFiles.remove(id)
        }
      }
    }
  }
}