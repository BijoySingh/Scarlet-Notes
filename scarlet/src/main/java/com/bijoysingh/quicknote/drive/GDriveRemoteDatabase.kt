package com.bijoysingh.quicknote.drive

import android.content.Context
import com.bijoysingh.quicknote.Scarlet.Companion.gDrive
import com.bijoysingh.quicknote.database.GDriveDataType
import com.bijoysingh.quicknote.database.GDriveUploadData
import com.bijoysingh.quicknote.database.GDriveUploadDataDao
import com.bijoysingh.quicknote.database.genGDriveUploadDatabase
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
const val FOLDER_NAME_DELETED_NOTES = "deleted_notes"
const val FOLDER_NAME_DELETED_TAGS = "deleted_tags"
const val FOLDER_NAME_DELETED_FOLDERS = "deleted_folders"

class GDriveRemoteDatabase(val weakContext: WeakReference<Context>) : IRemoteDatabase {

  var gDriveDatabase: GDriveUploadDataDao? = null

  private var isValidController: Boolean = true
  private var driveHelper: GDriveServiceHelper? = null

  private var notesSync: GDriveRemoteFolder<FirebaseNote>? = null
  private var foldersSync: GDriveRemoteFolder<FirebaseFolder>? = null
  private var tagsSync: GDriveRemoteFolder<FirebaseTag>? = null
  private var imageSync: GDriveRemoteImageFolder? = null

  override fun init(userId: String) {}

  fun init(helper: GDriveServiceHelper) {
    val context = weakContext.get()
    if (context === null) {
      return
    }

    isValidController = true
    driveHelper = helper
    gDriveDatabase = genGDriveUploadDatabase(context)

    notesSync = GDriveRemoteFolder(GDriveDataType.NOTE, gDriveDatabase!!, helper) {
      CoreConfig.instance.notesDatabase().getByUUID(it)?.getFirebaseNote()
    }
    tagsSync = GDriveRemoteFolder(GDriveDataType.TAG, gDriveDatabase!!, helper) {
      CoreConfig.instance.tagsDatabase().getByUUID(it)?.getFirebaseTag()
    }
    foldersSync = GDriveRemoteFolder(GDriveDataType.FOLDER, gDriveDatabase!!, helper) {
      CoreConfig.instance.foldersDatabase().getByUUID(it)?.getFirebaseFolder()
    }
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
      FOLDER_NAME_NOTES -> notesSync?.initContentFolderId(folderId) {
        if (!sGDriveFirstSyncNote) {
          CoreConfig.instance.notesDatabase().getAll().forEach {
            gDrive?.notifyInsert(it.getFirebaseNote())
          }
          sGDriveFirstSyncNote = true
        }
      }
      FOLDER_NAME_TAGS -> tagsSync?.initContentFolderId(folderId) {
        if (!sGDriveFirstSyncTag) {
          CoreConfig.instance.tagsDatabase().getAll().forEach {
            gDrive?.notifyInsert(it.getFirebaseTag())
          }
          sGDriveFirstSyncTag = true
        }
      }
      FOLDER_NAME_FOLDERS -> foldersSync?.initContentFolderId(folderId) {
        if (!sGDriveFirstSyncFolder) {
          CoreConfig.instance.foldersDatabase().getAll().forEach {
            gDrive?.notifyInsert(it.getFirebaseFolder())
          }
          sGDriveFirstSyncFolder = true
        }
      }
      FOLDER_NAME_DELETED_NOTES -> notesSync?.initDeletedFolderId(folderId) {}
      FOLDER_NAME_DELETED_TAGS -> tagsSync?.initDeletedFolderId(folderId) {}
      FOLDER_NAME_DELETED_FOLDERS -> foldersSync?.initDeletedFolderId(folderId) {}
      FOLDER_NAME_IMAGES -> imageSync?.initContentFolderId(folderId) {}
    }
  }

  fun onRootFolderLoaded(rootFolderId: String) {
    createFolders(rootFolderId, listOf(FOLDER_NAME_IMAGES, FOLDER_NAME_NOTES, FOLDER_NAME_TAGS, FOLDER_NAME_FOLDERS))
    createFolders(rootFolderId, listOf(FOLDER_NAME_DELETED_NOTES, FOLDER_NAME_DELETED_TAGS, FOLDER_NAME_DELETED_FOLDERS))
  }

  fun createFolders(rootFolderId: String, expectedFolders: List<String>) {
    driveHelper?.getSubRootFolders(rootFolderId, expectedFolders)?.addOnCompleteListener {
      val fileIds = it.result?.files ?: emptyList()
      val existingFiles = fileIds.map { it.name }
      fileIds.forEach {
        GlobalScope.launch { initSubRootFolder(it.name, it.id) }
      }
      expectedFolders.forEach { expectedFolder ->
        if (!existingFiles.contains(expectedFolder)) {
          driveHelper?.createFolder(rootFolderId, expectedFolder)?.addOnCompleteListener { fileIdTask ->
            val file = fileIdTask.result
            if (file !== null) {
              GlobalScope.launch { initSubRootFolder(expectedFolder, file.id) }
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

  fun notifyInsert(note: INoteContainer) {
    if (!isValidController || note !is FirebaseNote) {
      return
    }
    localDatabaseUpdate(GDriveDataType.NOTE, note.uuid)
  }

  fun notifyInsert(tag: ITagContainer) {
    if (!isValidController || tag !is FirebaseTag) {
      return
    }
    localDatabaseUpdate(GDriveDataType.TAG, tag.uuid)
  }

  fun notifyInsert(folder: IFolderContainer) {
    if (!isValidController || folder !is FirebaseFolder) {
      return
    }
    localDatabaseUpdate(GDriveDataType.FOLDER, folder.uuid)
  }

  fun notifyRemove(note: INoteContainer) {
    if (!isValidController || note !is FirebaseNote) {
      return
    }
    localDatabaseUpdate(GDriveDataType.NOTE, note.uuid, true)
  }

  fun notifyRemove(tag: ITagContainer) {
    if (!isValidController || tag !is FirebaseTag) {
      return
    }
    localDatabaseUpdate(GDriveDataType.TAG, tag.uuid, true)
  }

  fun notifyRemove(folder: IFolderContainer) {
    if (!isValidController || folder !is FirebaseFolder) {
      return
    }
    localDatabaseUpdate(GDriveDataType.FOLDER, folder.uuid, true)
  }

  fun resync(onSyncCompleted: () -> Unit) {
    if (!isValidController) {
      onSyncCompleted()
      return
    }

    GlobalScope.launch {
      gDriveDatabase?.all?.forEach {
        val sameDelete = it.localStateDeleted == it.gDriveStateDeleted
        val sameUpdateTime = it.lastUpdateTimestamp == it.gDriveUpdateTimestamp
        if (!sameUpdateTime) {
          var note: FirebaseNote? = null
          var tag: FirebaseTag? = null
          var folder: FirebaseFolder? = null
          when (it.type) {
            GDriveDataType.NOTE.name -> note = CoreConfig.instance.notesDatabase().getByUUID(it.uuid)?.getFirebaseNote()
            GDriveDataType.TAG.name -> tag = CoreConfig.instance.tagsDatabase().getByUUID(it.uuid)?.getFirebaseTag()
            GDriveDataType.FOLDER.name -> folder = CoreConfig.instance.foldersDatabase().getByUUID(it.uuid)?.getFirebaseFolder()
          }

          when {
            sameDelete && it.lastUpdateTimestamp > it.gDriveUpdateTimestamp -> {
              when {
                note !== null -> insert(note)
                tag !== null -> insert(tag)
                folder !== null -> insert(folder)
              }
            }
            sameDelete && it.lastUpdateTimestamp < it.gDriveUpdateTimestamp -> {
              when {
                note !== null -> onRemoteInsert(note)
                tag !== null -> onRemoteInsert(tag)
                folder !== null -> onRemoteInsert(folder)
              }
            }
            !sameDelete && it.lastUpdateTimestamp > it.gDriveUpdateTimestamp -> {
              when {
                note !== null -> remove(note)
                tag !== null -> remove(tag)
                folder !== null -> remove(folder)
              }
            }
            !sameDelete && it.lastUpdateTimestamp < it.gDriveUpdateTimestamp -> {
              when {
                note !== null -> onRemoteRemove(note)
                tag !== null -> onRemoteRemove(tag)
                folder !== null -> onRemoteRemove(folder)
              }
            }
          }
        }
      }
      onSyncCompleted()
    }
  }

  private fun localDatabaseUpdate(itemType: GDriveDataType, itemUUID: String, removed: Boolean = false) {
    GlobalScope.launch {
      val database = gDriveDatabase
      if (database === null) {
        return@launch
      }

      val existing = database.getByUUID(itemType.name, itemUUID) ?: GDriveUploadData()
      existing.apply {
        uuid = itemUUID
        type = itemType.name
        lastUpdateTimestamp = Math.max(gDriveUpdateTimestamp + 1, getTrueCurrentTime())
        localStateDeleted = removed
        save(database)
      }
    }
  }

  private fun remoteDatabaseUpdate(itemType: GDriveDataType, itemUUID: String) {
    GlobalScope.launch {
      val database = gDriveDatabase
      if (database === null) {
        return@launch
      }

      val existing = database.getByUUID(itemType.name, itemUUID) ?: GDriveUploadData()
      existing.apply {
        uuid = itemUUID
        type = GDriveDataType.NOTE.name
        lastUpdateTimestamp = gDriveUpdateTimestamp
        localStateDeleted = gDriveStateDeleted
        save(database)
      }
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
      // imageSync?.insert(it)
    }
    remoteDatabaseUpdate(GDriveDataType.NOTE, note.uuid)
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
    remoteDatabaseUpdate(GDriveDataType.NOTE, note.uuid)
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
    remoteDatabaseUpdate(GDriveDataType.TAG, tag.uuid)
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
    remoteDatabaseUpdate(GDriveDataType.TAG, tag.uuid)
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
    remoteDatabaseUpdate(GDriveDataType.FOLDER, folder.uuid)
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
    remoteDatabaseUpdate(GDriveDataType.FOLDER, folder.uuid)
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