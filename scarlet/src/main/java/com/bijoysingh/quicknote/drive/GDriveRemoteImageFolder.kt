package com.bijoysingh.quicknote.drive

import com.maubis.scarlet.base.config.ApplicationBase.Companion.noteImagesFolder
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.database.remote.IRemoteDatabaseUtils.onRemoteInsert
import com.maubis.scarlet.base.export.remote.LAST_MODIFIED_ERROR_MARGIN
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

data class ImageUUID(
    val noteUuid: String,
    val imageUuid: String) {
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

class GDriveRemoteImageFolder(val helper: GDriveServiceHelper) {

  val loaded = AtomicBoolean(false)
  val imageFileIds = emptyMap<ImageUUID, String>().toMutableMap()
  val pendingActions = emptyMap<ImageUUID, String>().toMutableMap()
  var folderUid: String = INVALID_FILE_ID

  fun init(fUid: String, onLoaded: () -> Unit) {
    folderUid = fUid
    val lastScanKey = "${KEY_G_DRIVE_SYNC_LAST_SCAN}_$folderUid"
    val lastScan = CoreConfig.instance.store().get(lastScanKey, 0L)

    GlobalScope.launch(Dispatchers.IO) {
      helper.getFilesInFolder(folderUid, GOOGLE_DRIVE_IMAGE_MIME_TYPE).addOnCompleteListener {
        val imageFiles = it.result?.files
        if (imageFiles !== null) {
          imageFiles.forEach { imageFile ->
            val components = imageFile.name.split("::")
            if (components.size == 2) {
              val noteUuid = components[0]
              val imageId = components[1]
              imageFileIds[ImageUUID(noteUuid, imageId)] = imageFile.id
            }
          }
          loaded.set(true)
          GlobalScope.launch { onLoaded() }
        }
      }
    }
  }

  fun notifyingExistingIds(localImages: Set<ImageUUID>) {
    if (!loaded.get()) {
      return
    }

    val remoteImages = imageFileIds.keys.toHashSet()
    localImages.filter { !remoteImages.contains(it) }.forEach {
      GlobalScope.launch {
        onInsert(it)
      }
    }
  }

  fun onInsert(id: ImageUUID) {
    if (!loaded.get()) {
      pendingActions.put(id, "insert")
      return
    }

    if (imageFileIds.containsKey(id)) {
      return
    }

    val imageFile = noteImagesFolder.getFile(id.noteUuid, id.imageUuid)
    helper.createFile(folderId = folderUid, mimeType = GOOGLE_DRIVE_IMAGE_MIME_TYPE).addOnCompleteListener {
      val createdFileId = it.result
      if (createdFileId !== null) {
        helper.saveFile(createdFileId, imageFile)
      }
    }
  }

  fun onRemove(id: ImageUUID) {
    if (!loaded.get()) {
      pendingActions.put(id, "remove")
      return
    }


  }

  fun onRemoteInsert(id: ImageUUID) {

  }
}