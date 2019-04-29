package com.bijoysingh.quicknote.drive

import com.maubis.scarlet.base.config.ApplicationBase.Companion.noteImagesFolder
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

  val contentLoading = AtomicBoolean(true)
  var contentFolderUid: String = INVALID_FILE_ID
  val contentFiles = emptyMap<ImageUUID, String>().toMutableMap()

  val contentPendingActions = emptySet<ImageUUID>().toMutableSet()
  val deletedPendingActions = emptySet<ImageUUID>().toMutableSet()

  fun initContentFolderId(fUid: String, onLoaded: () -> Unit) {
    contentFolderUid = fUid
    GlobalScope.launch(Dispatchers.IO) {
      helper.getFilesInFolder(contentFolderUid, GOOGLE_DRIVE_IMAGE_MIME_TYPE).addOnCompleteListener {
        val imageFiles = it.result?.files
        if (imageFiles !== null) {
          imageFiles.forEach { imageFile ->
            val components = imageFile.name.split("::")
            if (components.size == 2) {
              val noteUuid = components[0]
              val imageId = components[1]
              contentFiles[ImageUUID(noteUuid, imageId)] = imageFile.id
            }
          }
          contentLoading.set(false)
          GlobalScope.launch { onLoaded() }
        }
      }
    }
  }

  fun insert(id: ImageUUID) {
    if (contentLoading.get()) {
      contentPendingActions.add(id)
      return
    }

    if (contentFiles.containsKey(id)) {
      return
    }

    val imageFile = noteImagesFolder.getFile(id.noteUuid, id.imageUuid)
    val finalFileName = "${id.noteUuid}::${id.imageUuid}"
    helper.createFileWithData(contentFolderUid, finalFileName, imageFile).addOnCompleteListener {
      helper.updateLastModifiedTime(contentFolderUid)
    }
  }

  fun delete(id: ImageUUID) {
    if (contentLoading.get()) {
      deletedPendingActions.add(id)
      return
    }

    if (!contentFiles.containsKey(id)) {
      return
    }

    helper.removeFileOrFolder(contentFiles[id] ?: INVALID_FILE_ID)
    contentFiles.remove(id)
  }
}