package com.bijoysingh.quicknote.drive

import android.content.Context
import com.bijoysingh.quicknote.Scarlet.Companion.gDriveConfig
import com.bijoysingh.quicknote.database.GDriveDataType
import com.bijoysingh.quicknote.database.GDriveUploadData
import com.bijoysingh.quicknote.database.GDriveUploadDataDao
import com.bijoysingh.quicknote.database.genGDriveUploadDatabase
import com.bijoysingh.quicknote.firebase.data.getFirebaseNote
import com.google.gson.Gson
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.ApplicationBase.Companion.noteImagesFolder
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.config.auth.IPendingUploadListener
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.note.NoteBuilder
import com.maubis.scarlet.base.core.note.getFormats
import com.maubis.scarlet.base.database.remote.IRemoteDatabaseUtils
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.database.room.tag.Tag
import com.maubis.scarlet.base.export.data.*
import com.maubis.scarlet.base.settings.sheet.sNoteDefaultColor
import com.maubis.scarlet.base.support.utils.log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

const val FOLDER_NAME_IMAGES = "images"
const val FOLDER_NAME_NOTES = "notes"
const val FOLDER_NAME_NOTES_META = "notes_meta"
const val FOLDER_NAME_TAGS = "tags"
const val FOLDER_NAME_FOLDERS = "folders"
const val FOLDER_NAME_DELETED_NOTES = "deleted_notes"
const val FOLDER_NAME_DELETED_TAGS = "deleted_tags"
const val FOLDER_NAME_DELETED_FOLDERS = "deleted_folders"

const val KEY_G_DRIVE_FIRST_TIME_SYNC_NOTE = "g_drive_first_time_sync_note"
const val KEY_G_DRIVE_FIRST_TIME_SYNC_NOTE_META = "g_drive_first_time_sync_note_meta"
const val KEY_G_DRIVE_FIRST_TIME_SYNC_TAG = "g_drive_first_time_sync_tag"
const val KEY_G_DRIVE_FIRST_TIME_SYNC_FOLDER = "g_drive_first_time_sync_folder"
const val KEY_G_DRIVE_FIRST_TIME_SYNC_IMAGE = "g_drive_first_time_sync_image"
var sGDriveFirstSyncNoteMeta: Boolean
  get() = gDriveConfig?.get(KEY_G_DRIVE_FIRST_TIME_SYNC_NOTE_META, false) ?: false
  set(value) = gDriveConfig?.put(KEY_G_DRIVE_FIRST_TIME_SYNC_NOTE_META, value) ?: Unit
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

  private var notesSync: GDriveRemoteFolder<String>? = null
  private var notesMetaSync: GDriveRemoteFolder<ExportableNoteMeta>? = null
  private var foldersSync: GDriveRemoteFolder<ExportableFolder>? = null
  private var tagsSync: GDriveRemoteFolder<ExportableTag>? = null
  private var imageSync: GDriveRemoteImageFolder? = null
  private var syncing = HashMap<GDriveDataType, AtomicBoolean>()

  private var lastSyncState: AtomicBoolean = AtomicBoolean(true)
  private var syncListener: IPendingUploadListener? = null

  fun init(helper: GDriveServiceHelper) {
    val context = weakContext.get()
    if (context === null) {
      return
    }

    isValidController = true
    driveHelper = helper
    gDriveDatabase = genGDriveUploadDatabase(context)

    syncing[GDriveDataType.NOTE] = AtomicBoolean(false)
    syncing[GDriveDataType.TAG] = AtomicBoolean(false)
    syncing[GDriveDataType.FOLDER] = AtomicBoolean(false)
    syncing[GDriveDataType.NOTE_META] = AtomicBoolean(false)
    syncing[GDriveDataType.IMAGE] = AtomicBoolean(false)

    notesSync = GDriveRemoteFolder(
        dataType = GDriveDataType.NOTE,
        database = gDriveDatabase!!,
        helper = helper,
        serialiser = { it },
        uuidToObject = {
          ApplicationBase.instance.notesDatabase().getByUUID(it)?.toExportedMarkdown()
        })
    notesMetaSync = GDriveRemoteFolder(
        dataType = GDriveDataType.NOTE_META,
        database = gDriveDatabase!!,
        helper = helper,
        serialiser = { Gson().toJson(it) },
        uuidToObject = {
          ApplicationBase.instance.notesDatabase().getByUUID(it)?.getExportableNoteMeta()
        })
    tagsSync = GDriveRemoteFolder(
        dataType = GDriveDataType.TAG,
        database = gDriveDatabase!!,
        helper = helper,
        serialiser = { Gson().toJson(it) },
        uuidToObject = {
          ApplicationBase.instance.tagsDatabase().getByUUID(it)?.getExportableTag()
        })
    foldersSync = GDriveRemoteFolder(
        dataType = GDriveDataType.FOLDER,
        database = gDriveDatabase!!,
        helper = helper,
        serialiser = { Gson().toJson(it) },
        uuidToObject = {
          ApplicationBase.instance.foldersDatabase().getByUUID(it)?.getExportableFolder()
        })
    imageSync = GDriveRemoteImageFolder(dataType = GDriveDataType.IMAGE, database = gDriveDatabase!!, helper = helper)

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

  /**
   * Initialisation Methods
   */
  private fun initSubRootFolder(folderName: String, folderId: String) {
    when (folderName) {
      FOLDER_NAME_NOTES -> notesSync?.initContentFolderId(folderId) {
        if (!sGDriveFirstSyncNote) {
          GlobalScope.launch { resyncDataSync(GDriveDataType.NOTE) }
          sGDriveFirstSyncNote = true
        }
      }
      FOLDER_NAME_NOTES_META -> notesMetaSync?.initContentFolderId(folderId) {
        if (!sGDriveFirstSyncNoteMeta) {
          GlobalScope.launch { resyncDataSync(GDriveDataType.NOTE_META) }
          sGDriveFirstSyncNoteMeta = true
        }
      }
      FOLDER_NAME_TAGS -> tagsSync?.initContentFolderId(folderId) {
        if (!sGDriveFirstSyncTag) {
          GlobalScope.launch { resyncDataSync(GDriveDataType.TAG) }
          sGDriveFirstSyncTag = true
        }
      }
      FOLDER_NAME_FOLDERS -> foldersSync?.initContentFolderId(folderId) {
        if (!sGDriveFirstSyncFolder) {
          GlobalScope.launch { resyncDataSync(GDriveDataType.FOLDER) }
          sGDriveFirstSyncFolder = true
        }
      }
      FOLDER_NAME_IMAGES -> imageSync?.initContentFolderId(folderId) {
        if (!sGDriveFirstSyncImage) {
          GlobalScope.launch { resyncDataSync(GDriveDataType.IMAGE) }
          sGDriveFirstSyncImage = true
        }
      }
      FOLDER_NAME_DELETED_NOTES -> notesSync?.initDeletedFolderId(folderId) {}
      FOLDER_NAME_DELETED_TAGS -> tagsSync?.initDeletedFolderId(folderId) {}
      FOLDER_NAME_DELETED_FOLDERS -> foldersSync?.initDeletedFolderId(folderId) {}
    }
  }

  private fun onRootFolderLoaded(rootFolderId: String) {
    createFolders(rootFolderId, listOf(FOLDER_NAME_IMAGES, FOLDER_NAME_NOTES, FOLDER_NAME_TAGS, FOLDER_NAME_FOLDERS, FOLDER_NAME_NOTES_META))
    createFolders(rootFolderId, listOf(FOLDER_NAME_DELETED_NOTES, FOLDER_NAME_DELETED_TAGS, FOLDER_NAME_DELETED_FOLDERS))
  }

  private fun createFolders(rootFolderId: String, expectedFolders: List<String>) {
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

  /**
   * Pending Upload
   */

  fun setPendingUploadListener(listener: IPendingUploadListener?) {
    syncListener = listener
    verifyAndNotifyPendingStateChange()
    resync()
  }

  fun verifyAndNotifyPendingStateChange() {
    GlobalScope.launch {
      val database = gDriveDatabase
      if (database === null) {
        return@launch
      }

      val currentPendingState = database.getPendingCount() > 0
      if (currentPendingState != lastSyncState.get()) {
        lastSyncState.set(currentPendingState)
        syncListener?.onPendingStateUpdate(lastSyncState.get())
      }
    }
  }

  /**
   * Notify local changes to the notes
   */
  fun notifyInsert(data: Any) {
    if (!isValidController) {
      return
    }

    when {
      data is Tag -> localDatabaseUpdate(GDriveDataType.TAG, data.uuid)
      data is Folder -> localDatabaseUpdate(GDriveDataType.FOLDER, data.uuid)
      data is Note -> notifyInsertImpl(data)
    }
  }

  fun notifyInsertImpl(note: Note) {
    val noteUuid = note.uuid
    localDatabaseUpdate(GDriveDataType.NOTE, noteUuid)
    localDatabaseUpdate(GDriveDataType.NOTE_META, noteUuid)

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

  fun notifyRemove(data: Any) {
    if (!isValidController) {
      return
    }

    when {
      data is Tag -> localDatabaseUpdate(GDriveDataType.TAG, data.uuid, true)
      data is Folder -> localDatabaseUpdate(GDriveDataType.FOLDER, data.uuid, true)
      data is Note -> {
        localDatabaseUpdate(GDriveDataType.NOTE, data.uuid, true)
        localDatabaseUpdate(GDriveDataType.NOTE_META, data.uuid, true)
      }
    }
  }

  /**
   * Resync Functions
   */

  @Synchronized
  fun resync() {
    if (!isValidController) {
      return
    }

    /**
    if (sGDriveLastSync >= getTrueCurrentTime() - KEY_G_DRIVE_LAST_SYNC_DELTA_MS) {
      return
    }
    sGDriveLastSync = getTrueCurrentTime()
    **/

    GlobalScope.launch {
      resyncDataSync(GDriveDataType.NOTE)
      resyncDataSync(GDriveDataType.NOTE_META)
      resyncDataSync(GDriveDataType.TAG)
      resyncDataSync(GDriveDataType.FOLDER)
      resyncDataSync(GDriveDataType.IMAGE)
    }
  }

  fun resyncDataSync(type: GDriveDataType) {
    if (syncing[type]?.getAndSet(true) == true) {
      return
    }
    gDriveDatabase?.getPendingByType(type.name)?.forEach {
      log("GDrive", "resyncDataSync(${type.name}, ${it.uuid}, ${it.lastUpdateTimestamp}, ${it.gDriveUpdateTimestamp})")
      val sameDelete = it.localStateDeleted == it.gDriveStateDeleted
      val sameUpdateTime = it.lastUpdateTimestamp == it.gDriveUpdateTimestamp
      if (!sameUpdateTime) {
        when {
          sameDelete && it.lastUpdateTimestamp > it.gDriveUpdateTimestamp -> insert(type, it)
          sameDelete && it.lastUpdateTimestamp < it.gDriveUpdateTimestamp -> onRemoteInsert(type, it)
          !sameDelete && it.lastUpdateTimestamp > it.gDriveUpdateTimestamp -> remove(type, it)
          !sameDelete && it.lastUpdateTimestamp < it.gDriveUpdateTimestamp -> onRemoteRemove(type, it)
        }
      }
    }
    syncing[type]?.set(false)
  }

  /**
   * Update the database about information
   */

  private fun localDatabaseUpdate(itemType: GDriveDataType, itemUUID: String, removed: Boolean = false) {
    GlobalScope.launch {
      val database = gDriveDatabase
      if (database === null) {
        return@launch
      }

      log("GDrive", "localDatabaseUpdate(${itemType.name}, $itemUUID)")
      val existing = database.getByUUID(itemType.name, itemUUID) ?: GDriveUploadData()
      existing.apply {
        uuid = itemUUID
        type = itemType.name
        lastUpdateTimestamp = Math.max(gDriveUpdateTimestamp + 1, getTrueCurrentTime())
        localStateDeleted = removed
        save(database)
      }

      verifyAndNotifyPendingStateChange()
    }
  }

  private fun remoteDatabaseUpdate(itemType: GDriveDataType, itemUUID: String) {
    GlobalScope.launch {
      val database = gDriveDatabase
      if (database === null) {
        return@launch
      }

      log("GDrive", "remoteDatabaseUpdate(${itemType.name}, $itemUUID)")
      val existing = database.getByUUID(itemType.name, itemUUID) ?: GDriveUploadData()
      existing.apply {
        uuid = itemUUID
        type = itemType.name
        lastUpdateTimestamp = gDriveUpdateTimestamp
        localStateDeleted = gDriveStateDeleted
        save(database)
      }

      verifyAndNotifyPendingStateChange()
    }
  }

  /**
   * Core Data Functions
   */

  private fun insert(type: GDriveDataType, data: GDriveUploadData) {
    if (!isValidController) {
      return
    }

    when (type) {
      GDriveDataType.NOTE -> {
        ApplicationBase.instance.notesDatabase().getByUUID(data.uuid)?.toExportedMarkdown()?.apply {
          notesSync?.insert(data.uuid, this)
        }
      }
      GDriveDataType.NOTE_META -> {
        ApplicationBase.instance.notesDatabase().getByUUID(data.uuid)?.getExportableNoteMeta()?.apply {
          notesMetaSync?.insert(data.uuid, this)
        }
      }
      GDriveDataType.TAG -> {
        ApplicationBase.instance.tagsDatabase().getByUUID(data.uuid)?.getExportableTag()?.apply {
          tagsSync?.insert(this.uuid, this)
        }
      }
      GDriveDataType.FOLDER -> {
        ApplicationBase.instance.foldersDatabase().getByUUID(data.uuid)?.getExportableFolder()?.apply {
          foldersSync?.insert(this.uuid, this)
        }
      }
      GDriveDataType.IMAGE -> {
        toImageUUID(data.uuid)?.apply {
          imageSync?.insert(this)
        }
      }
    }
  }

  private fun remove(type: GDriveDataType, data: GDriveUploadData) {
    if (!isValidController) {
      return
    }

    val uuid = data.uuid
    when (type) {
      GDriveDataType.NOTE -> notesSync?.delete(uuid)
      GDriveDataType.NOTE_META -> notesMetaSync?.delete(uuid)
      GDriveDataType.TAG -> tagsSync?.delete(uuid)
      GDriveDataType.FOLDER -> foldersSync?.delete(uuid)
      GDriveDataType.IMAGE -> {
        val imageUUID = toImageUUID(uuid)
        if (imageUUID !== null) {
          imageSync?.delete(imageUUID)
        }
      }
    }
  }

  private fun onRemoteInsert(type: GDriveDataType, data: GDriveUploadData) {
    if (!isValidController) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }

    when (type) {
      GDriveDataType.NOTE -> {
        onRemoteInsertImpl(data.fileId) {
          // TODO: De-duplicate meta data and note update
          try {
            val itemDescription = fromExportedMarkdown(it)
            val existingNote = CoreConfig.notesDb.getByUUID(data.uuid)
                ?: NoteBuilder().emptyNote(sNoteDefaultColor).apply { uuid = data.uuid }
            val temporaryNote = NoteBuilder().copy(existingNote)
            temporaryNote.description = itemDescription
            IRemoteDatabaseUtils.onRemoteInsert(context, temporaryNote.getFirebaseNote())

            remoteDatabaseUpdate(GDriveDataType.NOTE, data.uuid)
          } catch (exception: Exception) {
          }
        }
      }
      GDriveDataType.NOTE_META -> {
        // TODO: De-duplicate meta data and note update
        onRemoteInsertImpl(data.fileId) {
          try {
            val item = Gson().fromJson(it, ExportableNoteMeta::class.java)

            val existingNote = CoreConfig.notesDb.getByUUID(data.uuid)
                ?: NoteBuilder().emptyNote(sNoteDefaultColor).apply { uuid = data.uuid }
            val temporaryNote = NoteBuilder().copy(existingNote)
            temporaryNote.mergeMetas(item)
            IRemoteDatabaseUtils.onRemoteInsert(context, temporaryNote.getFirebaseNote())

            remoteDatabaseUpdate(GDriveDataType.NOTE_META, data.uuid)
          } catch (exception: Exception) {
          }
        }
      }
      GDriveDataType.TAG -> {
        onRemoteInsertImpl(data.fileId) {
          try {
            val item = Gson().fromJson(it, ExportableTag::class.java)
            IRemoteDatabaseUtils.onRemoteInsert(context, item)
            remoteDatabaseUpdate(GDriveDataType.TAG, data.uuid)
          } catch (exception: Exception) {
          }
        }
      }
      GDriveDataType.FOLDER -> {
        onRemoteInsertImpl(data.fileId) {
          try {
            val item = Gson().fromJson(it, ExportableFolder::class.java)
            IRemoteDatabaseUtils.onRemoteInsert(context, item)
            remoteDatabaseUpdate(GDriveDataType.FOLDER, data.uuid)
          } catch (exception: Exception) {
          }
        }
      }
      GDriveDataType.IMAGE -> {
        val imageUUID = toImageUUID(data.uuid)
        if (imageUUID !== null) {
          val imageFile = noteImagesFolder.getFile(imageUUID.noteUuid, imageUUID.imageUuid)
          if (imageFile.exists()) {
            remoteDatabaseUpdate(GDriveDataType.IMAGE, data.uuid)
            return
          }

          driveHelper?.readFile(data.fileId, imageFile)?.addOnCompleteListener {
            if (it.result == true) {
              remoteDatabaseUpdate(GDriveDataType.IMAGE, data.uuid)
            }
          }
        }
      }
    }
  }

  private fun onRemoteRemove(type: GDriveDataType, data: GDriveUploadData) {
    if (!isValidController) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }

    when (type) {
      GDriveDataType.NOTE -> {
        IRemoteDatabaseUtils.onRemoteRemoveNote(context, data.uuid)
        remoteDatabaseUpdate(GDriveDataType.NOTE_META, data.uuid)
      }
      GDriveDataType.NOTE_META -> {
      } // Should never happen as note is handling this deletion
      GDriveDataType.TAG -> IRemoteDatabaseUtils.onRemoteRemoveTag(context, data.uuid)
      GDriveDataType.FOLDER -> IRemoteDatabaseUtils.onRemoteRemoveFolder(context, data.uuid)
      GDriveDataType.IMAGE -> {
        val imageUUID = toImageUUID(data.uuid)
        if (imageUUID !== null) {
          val imageFile = noteImagesFolder.getFile(imageUUID.noteUuid, imageUUID.imageUuid)
          imageFile.delete()
        }
      }
    }
    remoteDatabaseUpdate(type, data.uuid)
  }

  /**
   * Additional internal methods
   */

  private fun onRemoteInsertImpl(fileId: String, onDataAvailable: (String) -> Unit) {
    driveHelper?.readFile(fileId)?.addOnCompleteListener {
      val data = it.result
      if (data !== null) {
        onDataAvailable(data)
      }
    }
  }

  private fun notifyImageIds(note: Note, onImageUUID: (ImageUUID) -> Unit) {
    val imageIds = note.getFormats()
        .filter { it.formatType == FormatType.IMAGE }
        .map { it.text }
        .toSet()
    imageIds.forEach {
      onImageUUID(ImageUUID(note.uuid, it))
    }
  }
}