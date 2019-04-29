package com.bijoysingh.quicknote.drive

import android.content.Context
import com.bijoysingh.quicknote.firebase.data.*
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.folder.IFolderContainer
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.note.INoteContainer
import com.maubis.scarlet.base.core.tag.ITagContainer
import com.maubis.scarlet.base.database.remote.IRemoteDatabase
import com.maubis.scarlet.base.database.remote.IRemoteDatabaseUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

const val FOLDER_NAME_IMAGES = "images"
const val FOLDER_NAME_NOTES = "notes"
const val FOLDER_NAME_TAGS = "tags"
const val FOLDER_NAME_FOLDERS = "folders"
const val FOLDER_NAME_DELETED_NOTES = "deleted:notes"
const val FOLDER_NAME_DELETED_TAGS = "deleted:tags"
const val FOLDER_NAME_DELETED_FOLDERS = "deleted:folders"

class GDriveRemoteDatabase(val weakContext: WeakReference<Context>) : IRemoteDatabase {

  private var isValidController: Boolean = true
  private var driveHelper: GDriveServiceHelper? = null

  private var notesSync: GDriveRemoteFolder<FirebaseNote>? = null
  private var foldersSync: GDriveRemoteFolder<FirebaseFolder>? = null
  private var tagsSync: GDriveRemoteFolder<FirebaseTag>? = null
  private var imageSync: GDriveRemoteImageFolder? = null

  override fun init(userId: String) {}

  fun init(helper: GDriveServiceHelper) {
    isValidController = true
    driveHelper = helper

    notesSync = GDriveRemoteFolder(helper) { CoreConfig.instance.notesDatabase().getByUUID(it)?.getFirebaseNote() }
    tagsSync = GDriveRemoteFolder(helper) { CoreConfig.instance.tagsDatabase().getByUUID(it)?.getFirebaseTag() }
    foldersSync = GDriveRemoteFolder(helper) { CoreConfig.instance.foldersDatabase().getByUUID(it)?.getFirebaseFolder() }
    imageSync = GDriveRemoteImageFolder(helper)

    GlobalScope.launch {
      driveHelper?.getOrCreateDirectory("", GOOGLE_DRIVE_ROOT_FOLDER) {
        when {
          (it === null) -> reset()
          else -> onRootFolderLoaded(it)
        }
      }
    }
  }

  fun initSubRootFolder(folderName: String, folderId: String) {
    when (folderName) {
      FOLDER_NAME_NOTES -> notesSync?.initContentFolderId(folderId) {}
      FOLDER_NAME_TAGS -> tagsSync?.initContentFolderId(folderId) {}
      FOLDER_NAME_FOLDERS -> foldersSync?.initContentFolderId(folderId) {}
      FOLDER_NAME_DELETED_NOTES -> notesSync?.initDeletedFolderId(folderId) {}
      FOLDER_NAME_DELETED_TAGS -> tagsSync?.initDeletedFolderId(folderId) {}
      FOLDER_NAME_DELETED_FOLDERS -> foldersSync?.initDeletedFolderId(folderId) {}
      FOLDER_NAME_IMAGES -> imageSync?.initContentFolderId(folderId) {}
    }
  }

  fun onRootFolderLoaded(rootFolderId: String) {
    val expectedFolders = listOf(
        FOLDER_NAME_IMAGES,
        FOLDER_NAME_NOTES, FOLDER_NAME_TAGS, FOLDER_NAME_FOLDERS,
        FOLDER_NAME_DELETED_NOTES, FOLDER_NAME_DELETED_TAGS, FOLDER_NAME_DELETED_FOLDERS)
    driveHelper?.getSubRootFolders(rootFolderId, expectedFolders)?.addOnCompleteListener {
      val fileIds = it.result?.files ?: emptyList()
      val existingFiles = fileIds.map { it.name }
      fileIds.forEach {
        GlobalScope.launch { initSubRootFolder(it.name, it.id) }
      }
      expectedFolders.forEach { expectedFolder ->
        if (!existingFiles.contains(expectedFolder)) {
          driveHelper?.createFolder(rootFolderId, expectedFolder)?.addOnCompleteListener { fileIdTask ->
            val fileId = fileIdTask.result
            if (fileId !== null) {
              GlobalScope.launch { initSubRootFolder(expectedFolder, fileId) }
            }
          }
        }
      }
    }
  }

  override fun reset() {
    isValidController = false
    driveHelper = null
    notesSync = null
    foldersSync = null
    tagsSync = null
    imageSync = null
  }

  override fun logout() {
    reset()
  }

  override fun deleteEverything() {
    if (!isValidController) {
      return
    }
  }

  override fun insert(note: INoteContainer) {
    if (!isValidController || note !is FirebaseNote) {
      return
    }
    notesSync?.insert(note.uuid, note)
    notifyImageIds(note) {
      imageSync?.insert(it)
    }
  }

  override fun insert(tag: ITagContainer) {
    if (!isValidController || tag !is FirebaseTag) {
      return
    }
    tagsSync?.insert(tag.uuid, tag)
  }

  override fun insert(folder: IFolderContainer) {
    if (!isValidController || folder !is FirebaseFolder) {
      return
    }
    foldersSync?.insert(folder.uuid, folder)
  }

  override fun remove(note: INoteContainer) {
    if (!isValidController || note !is FirebaseNote) {
      return
    }
    notesSync?.delete(note.uuid)
    notifyImageIds(note) {
      imageSync?.delete(it)
    }
  }

  override fun remove(tag: ITagContainer) {
    if (!isValidController || tag !is FirebaseTag) {
      return
    }
    tagsSync?.delete(tag.uuid)
  }

  override fun remove(folder: IFolderContainer) {
    if (!isValidController || folder !is FirebaseFolder) {
      return
    }
    foldersSync?.delete(folder.uuid)
  }

  override fun onRemoteInsert(note: INoteContainer) {
    if (!isValidController || note !is FirebaseNote) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }
    IRemoteDatabaseUtils.onRemoteInsert(context, note)
    notifyImageIds(note) {
      imageSync?.insert(it)
    }
  }

  override fun onRemoteRemove(note: INoteContainer) {
    if (!isValidController || note !is FirebaseNote) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }
    IRemoteDatabaseUtils.onRemoteRemove(context, note)
  }

  override fun onRemoteInsert(tag: ITagContainer) {
    if (!isValidController || tag !is FirebaseTag) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }
    IRemoteDatabaseUtils.onRemoteInsert(context, tag)
  }

  override fun onRemoteRemove(tag: ITagContainer) {
    if (!isValidController || tag !is FirebaseTag) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }
    IRemoteDatabaseUtils.onRemoteRemove(context, tag)
  }

  override fun onRemoteInsert(folder: IFolderContainer) {
    if (!isValidController || folder !is FirebaseFolder) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }
    IRemoteDatabaseUtils.onRemoteInsert(context, folder)
  }

  override fun onRemoteRemove(folder: IFolderContainer) {
    if (!isValidController || folder !is FirebaseFolder) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }
    IRemoteDatabaseUtils.onRemoteRemove(context, folder)
  }

  fun notifyImageIds(note: INoteContainer, onImageUUID: (ImageUUID) -> Unit) {
    val imageIds = FormatBuilder()
        .getFormats(note.description())
        .filter { it.formatType == FormatType.IMAGE }
        .map { it.text }
        .toSet()
    imageIds.forEach {
      onImageUUID(ImageUUID(note.uuid(), it))
    }
  }
}