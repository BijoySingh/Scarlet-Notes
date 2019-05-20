package com.bijoysingh.quicknote.drive

import android.content.Context
import com.bijoysingh.quicknote.Scarlet
import com.bijoysingh.quicknote.Scarlet.Companion.gDriveConfig
import com.bijoysingh.quicknote.database.GDriveDataType
import com.bijoysingh.quicknote.database.GDriveUploadData
import com.bijoysingh.quicknote.database.GDriveUploadDataDao
import com.bijoysingh.quicknote.database.genGDriveUploadDatabase
import com.bijoysingh.quicknote.firebase.data.*
import com.github.bijoysingh.starter.prefs.Store
import com.google.gson.Gson
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.ApplicationBase.Companion.noteImagesFolder
import com.maubis.scarlet.base.core.folder.IFolderContainer
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.note.INoteContainer
import com.maubis.scarlet.base.core.tag.ITagContainer
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

const val KEY_G_DRIVE_FIRST_TIME_SYNC_NOTE = "g_drive_first_time_sync_note"
const val KEY_G_DRIVE_FIRST_TIME_SYNC_TAG = "g_drive_first_time_sync_tag"
const val KEY_G_DRIVE_FIRST_TIME_SYNC_FOLDER = "g_drive_first_time_sync_folder"
const val KEY_G_DRIVE_FIRST_TIME_SYNC_IMAGE = "g_drive_first_time_sync_image"
var sGDriveFirstSyncNote: Boolean
  get() = gDriveConfig?.get(KEY_G_DRIVE_FIRST_TIME_SYNC_NOTE, false) ?: false
  set(value) = gDriveConfig?.put(KEY_G_DRIVE_FIRST_TIME_SYNC_NOTE, value) ?: Unit
var sGDriveFirstSyncTag: Boolean
  get() = gDriveConfig?.get(KEY_G_DRIVE_FIRST_TIME_SYNC_TAG, false) ?: false
  set(value) = gDriveConfig?.put(KEY_G_DRIVE_FIRST_TIME_SYNC_TAG, value) ?: Unit
var sGDriveFirstSyncFolder: Boolean
  get() = gDriveConfig?.get(KEY_G_DRIVE_FIRST_TIME_SYNC_FOLDER, false) ?: false
  set(value) = gDriveConfig?.put(KEY_G_DRIVE_FIRST_TIME_SYNC_FOLDER, value) ?: Unit
var sGDriveFirstSyncImage: Boolean
  get() = gDriveConfig?.get(KEY_G_DRIVE_FIRST_TIME_SYNC_IMAGE, false) ?: false
  set(value) = gDriveConfig?.put(KEY_G_DRIVE_FIRST_TIME_SYNC_IMAGE, value) ?: Unit

const val KEY_G_DRIVE_LAST_SYNC_DELTA_MS = 1000 * 60
const val KEY_G_DRIVE_FIRST_TIME_SYNC_LAST_SYNC = "g_drive_first_time_sync_last_sync"
var sGDriveLastSync: Long
  get() = gDriveConfig?.get(KEY_G_DRIVE_FIRST_TIME_SYNC_LAST_SYNC, 0L) ?: 0L
  set(value) = gDriveConfig?.put(KEY_G_DRIVE_FIRST_TIME_SYNC_LAST_SYNC, value) ?: Unit

fun folderIdForFolderName(folderName: String, folderId: String = ""): String {
  val key = "g_drive_folder_if_for_$folderName"
  return when (folderId.isEmpty()) {
    true -> gDriveConfig?.get(key, "") ?: ""
    false -> {
      gDriveConfig?.put(key, folderId)
      folderId
    }
  }
}

class GDriveRemoteDatabase(val weakContext: WeakReference<Context>) {

  var gDriveDatabase: GDriveUploadDataDao? = null

  private var isValidController: Boolean = true
  private var driveHelper: GDriveServiceHelper? = null

  private var notesSync: GDriveRemoteFolder<FirebaseNote>? = null
  private var foldersSync: GDriveRemoteFolder<FirebaseFolder>? = null
  private var tagsSync: GDriveRemoteFolder<FirebaseTag>? = null
  private var imageSync: GDriveRemoteImageFolder? = null

  fun init(helper: GDriveServiceHelper) {
    val context = weakContext.get()
    if (context === null) {
      return
    }

    isValidController = true
    driveHelper = helper
    gDriveDatabase = genGDriveUploadDatabase(context)
    gDriveConfig = Store.get(context, "gdrive_config")

    notesSync = GDriveRemoteFolder(GDriveDataType.NOTE, gDriveDatabase!!, helper) {
      ApplicationBase.instance.notesDatabase().getByUUID(it)?.getFirebaseNote()
    }
    tagsSync = GDriveRemoteFolder(GDriveDataType.TAG, gDriveDatabase!!, helper) {
      ApplicationBase.instance.tagsDatabase().getByUUID(it)?.getFirebaseTag()
    }
    foldersSync = GDriveRemoteFolder(GDriveDataType.FOLDER, gDriveDatabase!!, helper) {
      ApplicationBase.instance.foldersDatabase().getByUUID(it)?.getFirebaseFolder()
    }
    imageSync = GDriveRemoteImageFolder(GDriveDataType.IMAGE, gDriveDatabase!!, helper)

    GlobalScope.launch {
      val fuid = folderIdForFolderName(GOOGLE_DRIVE_ROOT_FOLDER)
      when {
        fuid.isNotBlank() -> onRootFolderLoaded(fuid)
        else -> {
          driveHelper?.getOrCreateDirectory("", GOOGLE_DRIVE_ROOT_FOLDER) {
            when {
              (it === null) -> reset()
              else -> {
                folderIdForFolderName(GOOGLE_DRIVE_ROOT_FOLDER, it)
                onRootFolderLoaded(it)
              }
            }
          }
        }
      }
    }
  }

  fun initSubRootFolder(folderName: String, folderId: String) {
    when (folderName) {
      FOLDER_NAME_NOTES -> notesSync?.initContentFolderId(folderId) {
        if (!sGDriveFirstSyncNote) {
          GlobalScope.launch { resyncNotesSync { } }
          sGDriveFirstSyncNote = true
        }
      }
      FOLDER_NAME_TAGS -> tagsSync?.initContentFolderId(folderId) {
        if (!sGDriveFirstSyncTag) {
          GlobalScope.launch { resyncTagsSync { } }
          sGDriveFirstSyncTag = true
        }
      }
      FOLDER_NAME_FOLDERS -> foldersSync?.initContentFolderId(folderId) {
        if (!sGDriveFirstSyncFolder) {
          GlobalScope.launch { resyncFoldersSync { } }
          sGDriveFirstSyncFolder = true
        }
      }
      FOLDER_NAME_IMAGES -> imageSync?.initContentFolderId(folderId) {
        if (!sGDriveFirstSyncImage) {
          GlobalScope.launch { resyncImagessSync { } }
          sGDriveFirstSyncImage = true
        }
      }
      FOLDER_NAME_DELETED_NOTES -> notesSync?.initDeletedFolderId(folderId) {}
      FOLDER_NAME_DELETED_TAGS -> tagsSync?.initDeletedFolderId(folderId) {}
      FOLDER_NAME_DELETED_FOLDERS -> foldersSync?.initDeletedFolderId(folderId) {}
    }
  }

  fun onRootFolderLoaded(rootFolderId: String) {
    createFolders(rootFolderId, listOf(FOLDER_NAME_IMAGES, FOLDER_NAME_NOTES, FOLDER_NAME_TAGS, FOLDER_NAME_FOLDERS))
    createFolders(rootFolderId, listOf(FOLDER_NAME_DELETED_NOTES, FOLDER_NAME_DELETED_TAGS, FOLDER_NAME_DELETED_FOLDERS))
  }

  fun createFolders(rootFolderId: String, expectedFolders: List<String>) {
    val knownFolderIds = expectedFolders.filter { folderIdForFolderName(it).isNotEmpty() }
    knownFolderIds.forEach {
      GlobalScope.launch { initSubRootFolder(it, folderIdForFolderName(it)) }
    }

    val unknownFolderIds = expectedFolders.filter { !knownFolderIds.contains(it) }
    if (unknownFolderIds.isEmpty()) {
      return
    }

    driveHelper?.getSubRootFolders(rootFolderId, unknownFolderIds)?.addOnCompleteListener {
      val fileIds = it.result?.files ?: emptyList()
      val existingFiles = fileIds.map { it.name }
      fileIds.forEach {
        GlobalScope.launch {
          folderIdForFolderName(it.name, it.id)
          initSubRootFolder(it.name, it.id)
        }
      }
      unknownFolderIds.forEach { expectedFolder ->
        if (!existingFiles.contains(expectedFolder)) {
          driveHelper?.createFolder(rootFolderId, expectedFolder)?.addOnCompleteListener { fileIdTask ->
            val file = fileIdTask.result
            if (file !== null) {
              folderIdForFolderName(expectedFolder, file.id)
              GlobalScope.launch { initSubRootFolder(expectedFolder, file.id) }
            }
          }
        }
      }
    }
  }

  fun reset() {
    isValidController = false
    driveHelper = null
    notesSync = null
    foldersSync = null
    tagsSync = null
    imageSync = null
  }

  fun logout() {
    GlobalScope.launch {
      reset()
      gDriveDatabase?.drop()
      gDriveConfig?.clearSync()
    }
  }

  private fun deleteEverything() {
    if (!isValidController) {
      return
    }
  }

  fun notifyInsert(note: INoteContainer) {
    if (!isValidController || note !is FirebaseNote) {
      return
    }
    localDatabaseUpdate(GDriveDataType.NOTE, note.uuid)

    val database = gDriveDatabase
    if (database === null) {
      return
    }

    GlobalScope.launch {
      val imageUUIDs = HashSet<ImageUUID>()
      notifyImageIds(note) { imageUUIDs.add(it) }

      database.getByType(GDriveDataType.IMAGE.name)
          .filter {
            val uuid = toImageUUID(it.uuid)
            uuid?.noteUuid == note.uuid && !imageUUIDs.contains(uuid)
          }.forEach {
            it.apply {
              lastUpdateTimestamp = getTrueCurrentTime()
              localStateDeleted = true
              save(database)
            }
          }

      imageUUIDs.forEach {
        val existing = database.getByUUID(GDriveDataType.IMAGE.name, it.name())
        if (existing !== null) {
          return@launch
        }

        GDriveUploadData().apply {
          uuid = it.name()
          type = GDriveDataType.IMAGE.name
          lastUpdateTimestamp = getTrueCurrentTime()
          localStateDeleted = false
          save(database)
        }
      }
    }
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

  fun resyncNotesSync(onSyncCompleted: () -> Unit) {
    gDriveDatabase?.getByType(GDriveDataType.NOTE.name)?.forEach {
      val sameDelete = it.localStateDeleted == it.gDriveStateDeleted
      val sameUpdateTime = it.lastUpdateTimestamp == it.gDriveUpdateTimestamp
      if (!sameUpdateTime) {
        when {
          sameDelete && it.lastUpdateTimestamp > it.gDriveUpdateTimestamp ->
            ApplicationBase.instance.notesDatabase().getByUUID(it.uuid)?.getFirebaseNote()?.apply {
              insert(this)
            }
          sameDelete && it.lastUpdateTimestamp < it.gDriveUpdateTimestamp -> onRemoteInsertNote(it)
          !sameDelete && it.lastUpdateTimestamp > it.gDriveUpdateTimestamp -> removeNote(it.uuid)
          !sameDelete && it.lastUpdateTimestamp < it.gDriveUpdateTimestamp -> onRemoteRemoveNote(it)
        }
      }
    }
    onSyncCompleted()
  }

  fun resyncTagsSync(onSyncCompleted: () -> Unit) {
    gDriveDatabase?.getByType(GDriveDataType.TAG.name)?.forEach {
      val sameDelete = it.localStateDeleted == it.gDriveStateDeleted
      val sameUpdateTime = it.lastUpdateTimestamp == it.gDriveUpdateTimestamp
      if (!sameUpdateTime) {
        when {
          sameDelete && it.lastUpdateTimestamp > it.gDriveUpdateTimestamp ->
            ApplicationBase.instance.tagsDatabase().getByUUID(it.uuid)?.getFirebaseTag()?.apply {
              insert(this)
            }
          sameDelete && it.lastUpdateTimestamp < it.gDriveUpdateTimestamp -> onRemoteInsertTag(it)
          !sameDelete && it.lastUpdateTimestamp > it.gDriveUpdateTimestamp -> removeTag(it.uuid)
          !sameDelete && it.lastUpdateTimestamp < it.gDriveUpdateTimestamp -> onRemoteRemoveTag(it)
        }
      }
    }
    onSyncCompleted()
  }

  fun resyncFoldersSync(onSyncCompleted: () -> Unit) {
    gDriveDatabase?.getByType(GDriveDataType.FOLDER.name)?.forEach {
      val sameDelete = it.localStateDeleted == it.gDriveStateDeleted
      val sameUpdateTime = it.lastUpdateTimestamp == it.gDriveUpdateTimestamp
      if (!sameUpdateTime) {
        when {
          sameDelete && it.lastUpdateTimestamp > it.gDriveUpdateTimestamp ->
            ApplicationBase.instance.foldersDatabase().getByUUID(it.uuid)?.getFirebaseFolder()?.apply {
              insert(this)
            }
          sameDelete && it.lastUpdateTimestamp < it.gDriveUpdateTimestamp -> onRemoteInsertFolder(it)
          !sameDelete && it.lastUpdateTimestamp > it.gDriveUpdateTimestamp -> removeFolder(it.uuid)
          !sameDelete && it.lastUpdateTimestamp < it.gDriveUpdateTimestamp -> onRemoteRemoveFolder(it)
        }
      }
    }
    onSyncCompleted()
  }

  fun resyncImagessSync(onSyncCompleted: () -> Unit) {
    gDriveDatabase?.getByType(GDriveDataType.IMAGE.name)?.forEach {
      val sameDelete = it.localStateDeleted == it.gDriveStateDeleted
      val sameUpdateTime = it.lastUpdateTimestamp == it.gDriveUpdateTimestamp
      if (!sameUpdateTime) {
        when {
          sameDelete && it.lastUpdateTimestamp > it.gDriveUpdateTimestamp ->
            toImageUUID(it.uuid)?.apply {
              insert(this)
            }
          sameDelete && it.lastUpdateTimestamp < it.gDriveUpdateTimestamp -> onRemoteInsertImage(it)
          !sameDelete && it.lastUpdateTimestamp > it.gDriveUpdateTimestamp -> removeImage(it.uuid)
          !sameDelete && it.lastUpdateTimestamp < it.gDriveUpdateTimestamp -> onRemoteRemoveImage(it)
        }
      }
    }
    onSyncCompleted()
  }

  @Synchronized
  fun resync(force: Boolean, onSyncCompleted: () -> Unit) {
    if (!isValidController) {
      onSyncCompleted()
      return
    }

    if (!force && sGDriveLastSync > getTrueCurrentTime() - KEY_G_DRIVE_LAST_SYNC_DELTA_MS) {
      onSyncCompleted()
      return
    }
    sGDriveLastSync = getTrueCurrentTime()

    GlobalScope.launch {
      resyncNotesSync {}
      resyncTagsSync {}
      resyncFoldersSync {}
      resyncImagessSync {}
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
        type = itemType.name
        lastUpdateTimestamp = gDriveUpdateTimestamp
        localStateDeleted = gDriveStateDeleted
        save(database)
      }
    }
  }

  private fun insert(note: INoteContainer) {
    if (!isValidController || note !is FirebaseNote) {
      return
    }
    notesSync?.insert(note.uuid, note)
  }

  private fun insert(imageUUID: ImageUUID) {
    if (!isValidController) {
      return
    }
    imageSync?.insert(imageUUID)
  }

  private fun insert(tag: ITagContainer) {
    if (!isValidController || tag !is FirebaseTag) {
      return
    }
    tagsSync?.insert(tag.uuid, tag)
  }

  private fun insert(folder: IFolderContainer) {
    if (!isValidController || folder !is FirebaseFolder) {
      return
    }
    foldersSync?.insert(folder.uuid, folder)
  }

  private fun removeNote(uuid: String) {
    if (!isValidController) {
      return
    }
    notesSync?.delete(uuid)
  }

  private fun removeTag(uuid: String) {
    if (!isValidController) {
      return
    }
    tagsSync?.delete(uuid)
  }

  private fun removeFolder(uuid: String) {
    if (!isValidController) {
      return
    }
    foldersSync?.delete(uuid)
  }

  private fun removeImage(uuid: String) {
    if (!isValidController) {
      return
    }
    val imageUUID = toImageUUID(uuid)
    if (imageUUID !== null) {
      imageSync?.delete(imageUUID)
    }
  }

  private fun onRemoteInsertNote(data: GDriveUploadData) {
    if (!isValidController) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }

    onRemoteInsert(data.fileId) {
      try {
        val item = Gson().fromJson(it, FirebaseNote::class.java)
        IRemoteDatabaseUtils.onRemoteInsert(context, item)
        remoteDatabaseUpdate(GDriveDataType.NOTE, item.uuid)
      } catch (exception: Exception) {
      }
    }
  }

  private fun onRemoteInsertTag(data: GDriveUploadData) {
    if (!isValidController) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }

    onRemoteInsert(data.fileId) {
      try {
        val item = Gson().fromJson(it, FirebaseTag::class.java)
        IRemoteDatabaseUtils.onRemoteInsert(context, item)
        remoteDatabaseUpdate(GDriveDataType.TAG, data.uuid)
      } catch (exception: Exception) {
      }
    }
  }

  private fun onRemoteInsertFolder(data: GDriveUploadData) {
    if (!isValidController) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }

    onRemoteInsert(data.fileId) {
      try {
        val item = Gson().fromJson(it, FirebaseFolder::class.java)
        IRemoteDatabaseUtils.onRemoteInsert(context, item)
        remoteDatabaseUpdate(GDriveDataType.FOLDER, data.uuid)
      } catch (exception: Exception) {
      }
    }
  }

  private fun onRemoteInsertImage(data: GDriveUploadData) {
    if (!isValidController) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }

    val imageUUID = toImageUUID(data.uuid)
    if (imageUUID !== null) {
      val imageFile = noteImagesFolder.getFile(imageUUID.noteUuid, imageUUID.imageUuid)
      if (imageFile.exists()) {
        remoteDatabaseUpdate(GDriveDataType.IMAGE, data.uuid)
        return
      }

      driveHelper?.readFile(data.fileId, imageFile)?.addOnCompleteListener {
        remoteDatabaseUpdate(GDriveDataType.IMAGE, data.uuid)
      }
    }
  }

  private fun onRemoteRemoveNote(data: GDriveUploadData) {
    if (!isValidController) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }
    IRemoteDatabaseUtils.onRemoteRemoveNote(context, data.uuid)
    remoteDatabaseUpdate(GDriveDataType.NOTE, data.uuid)
  }

  private fun onRemoteRemoveTag(data: GDriveUploadData) {
    if (!isValidController) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }
    IRemoteDatabaseUtils.onRemoteRemoveTag(context, data.uuid)
    remoteDatabaseUpdate(GDriveDataType.TAG, data.uuid)
  }

  private fun onRemoteRemoveFolder(data: GDriveUploadData) {
    if (!isValidController) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }
    IRemoteDatabaseUtils.onRemoteRemoveFolder(context, data.uuid)
    remoteDatabaseUpdate(GDriveDataType.FOLDER, data.uuid)
  }

  private fun onRemoteRemoveImage(data: GDriveUploadData) {
    if (!isValidController) {
      return
    }

    val imageUUID = toImageUUID(data.uuid)
    if (imageUUID !== null) {
      val imageFile = noteImagesFolder.getFile(imageUUID.noteUuid, imageUUID.imageUuid)
      imageFile.delete()
      remoteDatabaseUpdate(GDriveDataType.IMAGE, data.uuid)
    }
  }

  private fun onRemoteInsert(fileId: String, onDataAvailable: (String) -> Unit) {
    driveHelper?.readFile(fileId)?.addOnCompleteListener {
      val data = it.result
      if (data !== null) {
        onDataAvailable(data)
      }
    }
  }

  private fun notifyImageIds(note: INoteContainer, onImageUUID: (ImageUUID) -> Unit) {
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