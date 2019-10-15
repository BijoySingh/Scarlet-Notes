package com.bijoysingh.quicknote.database

import android.content.Context
import com.bijoysingh.quicknote.Scarlet.Companion.remoteConfig
import com.bijoysingh.quicknote.drive.GOOGLE_DRIVE_ROOT_FOLDER
import com.bijoysingh.quicknote.drive.getTrueCurrentTime
import com.bijoysingh.quicknote.drive.sSyncingCount
import com.bijoysingh.quicknote.drive.toImageUUID
import com.bijoysingh.quicknote.firebase.data.getFirebaseNote
import com.google.gson.Gson
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.ApplicationBase.Companion.noteImagesFolder
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.config.auth.IPendingUploadListener
import com.maubis.scarlet.base.core.note.NoteBuilder
import com.maubis.scarlet.base.database.remote.IRemoteDatabaseUtils
import com.maubis.scarlet.base.export.data.*
import com.maubis.scarlet.base.note.creation.sheet.sNoteDefaultColor
import com.maubis.scarlet.base.support.utils.log
import com.maubis.scarlet.base.support.utils.maybeThrow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
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
var sRemoteFirstSyncNoteMeta: Boolean
  get() = remoteConfig.get(KEY_G_DRIVE_FIRST_TIME_SYNC_NOTE_META, false)
  set(value) = remoteConfig.put(KEY_G_DRIVE_FIRST_TIME_SYNC_NOTE_META, value)
var sRemoteFirstSyncNote: Boolean
  get() = remoteConfig.get(KEY_G_DRIVE_FIRST_TIME_SYNC_NOTE, false)
  set(value) = remoteConfig.put(KEY_G_DRIVE_FIRST_TIME_SYNC_NOTE, value)
var sRemoteFirstSyncTag: Boolean
  get() = remoteConfig.get(KEY_G_DRIVE_FIRST_TIME_SYNC_TAG, false)
  set(value) = remoteConfig.put(KEY_G_DRIVE_FIRST_TIME_SYNC_TAG, value)
var sRemoteFirstSyncFolder: Boolean
  get() = remoteConfig.get(KEY_G_DRIVE_FIRST_TIME_SYNC_FOLDER, false)
  set(value) = remoteConfig.put(KEY_G_DRIVE_FIRST_TIME_SYNC_FOLDER, value)
var sRemoteFirstSyncImage: Boolean
  get() = remoteConfig.get(KEY_G_DRIVE_FIRST_TIME_SYNC_IMAGE, false)
  set(value) = remoteConfig.put(KEY_G_DRIVE_FIRST_TIME_SYNC_IMAGE, value)

const val KEY_G_DRIVE_LAST_FULL_SYNC_TIME = "g_drive_last_full_sync_time"
var sRemoteLastFullSyncTime: Long
  get() = remoteConfig.get(KEY_G_DRIVE_LAST_FULL_SYNC_TIME, 0L)
  set(value) = remoteConfig.put(KEY_G_DRIVE_LAST_FULL_SYNC_TIME, value)

abstract class RemoteController<ResourceId, FileType, FileListType>(private val weakContext: WeakReference<Context>) {

  private val isValidController: AtomicBoolean = AtomicBoolean(true)

  lateinit var remoteDatabaseController: RemoteDatabaseStateController
  lateinit var remoteService: IRemoteService<ResourceId, FileType, FileListType>
  lateinit var remoteDatabase: RemoteUploadDataDao

  lateinit var notesSync: RemoteFolder<ResourceId, String>
  lateinit var notesMetaSync: RemoteFolder<ResourceId, ExportableNoteMeta>
  lateinit var foldersSync: RemoteFolder<ResourceId, ExportableFolder>
  lateinit var tagsSync: RemoteFolder<ResourceId, ExportableTag>
  lateinit var imageSync: RemoteFolder<ResourceId, File>

  private var syncing = HashMap<RemoteDataType, AtomicBoolean>()
  private var syncListener: IPendingUploadListener? = null
  private var databaseUpdateLambda: () -> Unit = { verifyAndNotifyPendingStateChange() }

  fun init(service: IRemoteService<ResourceId, FileType, FileListType>) {
    val context = weakContext.get()
    if (context === null) {
      return
    }

    isValidController.set(true)
    remoteService = service
    remoteDatabase = genRemoteDatabase(context)!!
    remoteDatabaseController = RemoteDatabaseStateController(context)

    syncing[RemoteDataType.NOTE] = AtomicBoolean(false)
    syncing[RemoteDataType.TAG] = AtomicBoolean(false)
    syncing[RemoteDataType.FOLDER] = AtomicBoolean(false)
    syncing[RemoteDataType.NOTE_META] = AtomicBoolean(false)
    syncing[RemoteDataType.IMAGE] = AtomicBoolean(false)

    initSyncs()
    initRootFolder()
  }

  fun isValid(): Boolean {
    return isValidController.get()
  }

  fun logout() {
    GlobalScope.launch {
      reset()
      remoteConfig.clearSync()
    }
  }

  fun reset() {
    isValidController.set(false)

    notesSync.invalidate()
    foldersSync.invalidate()
    tagsSync.invalidate()
    imageSync.invalidate()
  }

  /**
   * Abstract Methods
   */

  abstract fun initSyncs()
  abstract fun getResourceId(data: RemoteUploadData): ResourceId
  abstract fun getResourceIdForFolderName(folderName: String): ResourceId?
  abstract fun storeResourceIdForFolderName(folderName: String, resource: ResourceId)

  /**
   * Initialisation Methods
   */

  private fun initRootFolder() {
    GlobalScope.launch {
      sRemoteLastFullSyncTime = getTrueCurrentTime()
      val fuid = getResourceIdForFolderName(GOOGLE_DRIVE_ROOT_FOLDER)
      when {
        fuid !== null -> onRootFolderLoaded(fuid)
        else -> {
          remoteService.getOrCreateDirectory(null, GOOGLE_DRIVE_ROOT_FOLDER) { folderResource ->
            when {
              (folderResource === null) -> reset()
              else -> {
                storeResourceIdForFolderName(GOOGLE_DRIVE_ROOT_FOLDER, folderResource)
                onRootFolderLoaded(folderResource)
              }
            }
          }
        }
      }
    }
  }

  private fun onRootFolderLoaded(rootFolderId: ResourceId) {
    createFolders(rootFolderId, listOf(FOLDER_NAME_NOTES, FOLDER_NAME_NOTES_META, FOLDER_NAME_FOLDERS, FOLDER_NAME_TAGS, FOLDER_NAME_IMAGES))
    createFolders(rootFolderId, listOf(FOLDER_NAME_DELETED_NOTES, FOLDER_NAME_DELETED_TAGS, FOLDER_NAME_DELETED_FOLDERS))
  }

  private fun createFolders(rootFolderId: ResourceId, expectedFolders: List<String>) {
    val knownFolderIds = expectedFolders.filter { getResourceIdForFolderName(it) !== null }
    knownFolderIds.forEach {
      GlobalScope.launch { initSubRootFolder(it, getResourceIdForFolderName(it)!!) }
    }

    val unknownFolderNames = expectedFolders.filter { !knownFolderIds.contains(it) }
    if (unknownFolderNames.isEmpty()) {
      return
    }

    remoteService.getDirectories(rootFolderId, unknownFolderNames) { items ->
      val pendingNames = emptyList<String>().toMutableList()
      items.forEach { pair ->
        val name = pair.first
        val resourceId = pair.second
        when {
          resourceId !== null -> {
            storeResourceIdForFolderName(name, resourceId)
            initSubRootFolder(name, resourceId)
          }
          else -> pendingNames.add(name)
        }
      }
      pendingNames.forEach { name ->
        remoteService.createDirectory(rootFolderId, name) { folderId ->
          if (folderId !== null) {
            storeResourceIdForFolderName(name, folderId)
            GlobalScope.launch { initSubRootFolder(name, folderId) }
          }
        }
      }
    }
  }

  private fun initSubRootFolder(folderName: String, folderId: ResourceId) {
    when (folderName) {
      FOLDER_NAME_NOTES -> notesSync.initContentFolder(folderId) {
        if (!sRemoteFirstSyncNote) {
          GlobalScope.launch { resyncDataSync(RemoteDataType.NOTE) }
          sRemoteFirstSyncNote = true
        }
      }
      FOLDER_NAME_NOTES_META -> {
        notesMetaSync.initContentFolder(folderId) {
          if (!sRemoteFirstSyncNoteMeta) {
            GlobalScope.launch { resyncDataSync(RemoteDataType.NOTE_META) }
            sRemoteFirstSyncNoteMeta = true
          }
        }
        notesMetaSync.initDeletedFolder(null) {}
      }
      FOLDER_NAME_TAGS -> tagsSync.initContentFolder(folderId) {
        if (!sRemoteFirstSyncTag) {
          GlobalScope.launch { resyncDataSync(RemoteDataType.TAG) }
          sRemoteFirstSyncTag = true
        }
      }
      FOLDER_NAME_FOLDERS -> foldersSync.initContentFolder(folderId) {
        if (!sRemoteFirstSyncFolder) {
          GlobalScope.launch { resyncDataSync(RemoteDataType.FOLDER) }
          sRemoteFirstSyncFolder = true
        }
      }
      FOLDER_NAME_IMAGES -> imageSync.initContentFolder(folderId) {
        if (!sRemoteFirstSyncImage) {
          GlobalScope.launch { resyncDataSync(RemoteDataType.IMAGE) }
          sRemoteFirstSyncImage = true
        }
      }
      FOLDER_NAME_DELETED_NOTES -> notesSync.initDeletedFolder(folderId) {

      }
      FOLDER_NAME_DELETED_TAGS -> tagsSync.initDeletedFolder(folderId) {

      }
      FOLDER_NAME_DELETED_FOLDERS -> foldersSync.initDeletedFolder(folderId) {

      }
    }
  }

  /**
   * Pending Upload
   */

  fun setPendingUploadListener(listener: IPendingUploadListener?) {
    syncListener = listener
    if (listener !== null) {
      verifyAndNotifyPendingStateChange()
    }
  }

  fun notifyPendingSyncChange(action: String) {
    log("GDrive", "notifyPendingSyncChange($action)")
    val count = sSyncingCount.get()
    when {
      count <= 0 -> syncListener?.onPendingSyncsUpdate(false)
      count >= 1 -> syncListener?.onPendingSyncsUpdate(true)
    }
  }

  fun verifyAndNotifyPendingStateChange() {
    GlobalScope.launch {
      val currentPendingState = remoteDatabase.getPendingCount() > 0
      val pending = remoteDatabase.getAllPending().map { "type=${it.type}, uuid=${it.uuid}, fid=${it.fileId}" }.joinToString(separator = "\n")
      log("GDrive", "getPendingCount(${remoteDatabase.getPendingCount()})\n$pending")
      notifyPendingSyncChange("verifyAndNotifyPendingStateChange")
      syncListener?.onPendingStateUpdate(currentPendingState)
    }
  }

  /**
   * Notify local changes to the notes
   */
  fun notifyChange() {
    if (!isValid()) {
      return
    }

    verifyAndNotifyPendingStateChange()
  }

  /**
   * Resync Functions
   */

  @Synchronized
  fun resync(forced: Boolean) {
    if (!isValid()) {
      return
    }

    if (forced || (getTrueCurrentTime() - sRemoteLastFullSyncTime > 1000 * 60 * 60 * 24)) {
      GlobalScope.launch {
        remoteDatabase.resetAttempts()
        initRootFolder()
      }
      return
    }

    GlobalScope.launch {
      resyncDataSync(RemoteDataType.NOTE)
      resyncDataSync(RemoteDataType.NOTE_META)
      resyncDataSync(RemoteDataType.TAG)
      resyncDataSync(RemoteDataType.FOLDER)
      resyncDataSync(RemoteDataType.IMAGE)
    }
  }

  private fun resyncDataSync(type: RemoteDataType) {
    if (syncing[type]?.getAndSet(true) == true) {
      return
    }

    val pendingItems = remoteDatabase.getPendingByType(type.name)
    for (pendingItem in pendingItems) {
      if (!remoteDatabaseController.notifyAttempt(type, pendingItem.uuid)) {
        continue
      }

      val sameDelete = pendingItem.localStateDeleted == pendingItem.remoteStateDeleted
      val localDeleted = pendingItem.localStateDeleted
      val remoteDeleted = pendingItem.remoteStateDeleted
      val sameUpdateTime = pendingItem.lastUpdateTimestamp == pendingItem.remoteUpdateTimestamp
      val isLocalMoreRecent = pendingItem.lastUpdateTimestamp > pendingItem.remoteUpdateTimestamp
      val isRemoteMoreRecent = pendingItem.lastUpdateTimestamp < pendingItem.remoteUpdateTimestamp
      when {
        sameUpdateTime -> remoteDatabaseController.remoteDatabaseUpdate(type, pendingItem.uuid, databaseUpdateLambda)
        !sameDelete && isRemoteMoreRecent && remoteDeleted -> onRemoteRemove(type, pendingItem)
        !sameDelete && isLocalMoreRecent && localDeleted -> remove(type, pendingItem)
        !sameDelete && isLocalMoreRecent && remoteDeleted -> insert(type, pendingItem)
        !sameDelete && isRemoteMoreRecent && localDeleted -> onRemoteInsert(type, pendingItem)
        sameDelete && isLocalMoreRecent && localDeleted -> remove(type, pendingItem)
        sameDelete && isLocalMoreRecent && !localDeleted -> insert(type, pendingItem)
        sameDelete && isRemoteMoreRecent && localDeleted -> onRemoteRemove(type, pendingItem)
        sameDelete && isRemoteMoreRecent && !localDeleted -> onRemoteInsert(type, pendingItem)
      }
    }
    syncing[type]?.set(false)
  }

  /**
   * Core Data Functions
   */

  @Suppress("IMPLICIT_CAST_TO_ANY")
  private fun insert(type: RemoteDataType, data: RemoteUploadData) {
    if (!isValid()) {
      return
    }

    val localItem = when (type) {
      RemoteDataType.NOTE -> {
        ApplicationBase.instance.notesDatabase().getByUUID(data.uuid)?.toExportedMarkdown()?.apply {
          notesSync.insert(data, this)
        }
      }
      RemoteDataType.NOTE_META -> {
        ApplicationBase.instance.notesDatabase().getByUUID(data.uuid)?.getExportableNoteMeta()?.apply {
          notesMetaSync.insert(data, this)
        }
      }
      RemoteDataType.TAG -> {
        ApplicationBase.instance.tagsDatabase().getByUUID(data.uuid)?.getExportableTag()?.apply {
          tagsSync.insert(data, this)
        }
      }
      RemoteDataType.FOLDER -> {
        ApplicationBase.instance.foldersDatabase().getByUUID(data.uuid)?.getExportableFolder()?.apply {
          foldersSync.insert(data, this)
        }
      }
      RemoteDataType.IMAGE -> {
        val imageUUID = toImageUUID(data.uuid)
        when {
          imageUUID !== null -> noteImagesFolder.getFile(imageUUID.noteUuid, imageUUID.imageUuid).apply {
            imageSync.insert(data, this)
          }
          else -> null
        }
      }
    }

    if (localItem === null) {
      remoteDatabaseController.localDatabaseRemove(type, data.uuid) {}
    }
  }

  private fun remove(type: RemoteDataType, data: RemoteUploadData) {
    if (!isValid()) {
      return
    }

    val logInfo = "remove(${type.name}, ${data.uuid})"
    log("GDriveRemote", logInfo)
    when (type) {
      RemoteDataType.NOTE -> notesSync.delete(data)
      RemoteDataType.NOTE_META -> notesMetaSync.delete(data)
      RemoteDataType.TAG -> tagsSync.delete(data)
      RemoteDataType.FOLDER -> foldersSync.delete(data)
      RemoteDataType.IMAGE -> imageSync.delete(data)
    }
  }

  private fun onRemoteInsert(type: RemoteDataType, data: RemoteUploadData) {
    if (!isValid()) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }

    when (type) {
      RemoteDataType.NOTE -> {
        remoteService.readFile(getResourceId(data)) { content ->
          // TODO: De-duplicate meta data and note update
          try {
            val itemDescription = fromExportedMarkdown(content)
            val existingNote = CoreConfig.notesDb.getByUUID(data.uuid)
                ?: NoteBuilder().emptyNote(sNoteDefaultColor).apply { uuid = data.uuid }
            val temporaryNote = NoteBuilder().copy(existingNote)
            temporaryNote.description = itemDescription
            IRemoteDatabaseUtils.onRemoteInsert(context, temporaryNote.getFirebaseNote())

            remoteDatabaseController.remoteDatabaseUpdate(RemoteDataType.NOTE, data.uuid, databaseUpdateLambda)
          } catch (exception: Exception) {
            maybeThrow(exception)
          }
        }
      }
      RemoteDataType.NOTE_META -> {
        // TODO: De-duplicate meta data and note update
        remoteService.readFile(getResourceId(data)) { content ->
          try {
            val item = Gson().fromJson(content, ExportableNoteMeta::class.java)

            val existingNote = CoreConfig.notesDb.getByUUID(data.uuid)
                ?: NoteBuilder().emptyNote(sNoteDefaultColor).apply { uuid = data.uuid }
            val temporaryNote = NoteBuilder().copy(existingNote)
            temporaryNote.mergeMetas(item)
            IRemoteDatabaseUtils.onRemoteInsert(context, temporaryNote.getFirebaseNote())

            remoteDatabaseController.remoteDatabaseUpdate(RemoteDataType.NOTE_META, data.uuid, databaseUpdateLambda)
          } catch (exception: Exception) {
            maybeThrow(exception)
          }
        }
      }
      RemoteDataType.TAG -> {
        remoteService.readFile(getResourceId(data)) { content ->
          try {
            val item = Gson().fromJson(content, ExportableTag::class.java)
            IRemoteDatabaseUtils.onRemoteInsert(context, item)
            remoteDatabaseController.remoteDatabaseUpdate(RemoteDataType.TAG, data.uuid, databaseUpdateLambda)
          } catch (exception: Exception) {
            maybeThrow(exception)
          }
        }
      }
      RemoteDataType.FOLDER -> {
        remoteService.readFile(getResourceId(data)) { content ->
          try {
            val item = Gson().fromJson(content, ExportableFolder::class.java)
            IRemoteDatabaseUtils.onRemoteInsert(context, item)
            remoteDatabaseController.remoteDatabaseUpdate(RemoteDataType.FOLDER, data.uuid, databaseUpdateLambda)
          } catch (exception: Exception) {
            maybeThrow(exception)
          }
        }
      }
      RemoteDataType.IMAGE -> {
        val imageUUID = toImageUUID(data.uuid)
        if (imageUUID !== null) {
          val imageFile = ApplicationBase.noteImagesFolder.getFile(imageUUID.noteUuid, imageUUID.imageUuid)
          if (imageFile.exists()) {
            remoteDatabaseController.remoteDatabaseUpdate(RemoteDataType.IMAGE, data.uuid, databaseUpdateLambda)
            return
          }

          remoteService.readIntoFile(getResourceId(data), imageFile) { success ->
            if (success) {
              remoteDatabaseController.remoteDatabaseUpdate(RemoteDataType.IMAGE, data.uuid, databaseUpdateLambda)
            }
          }
        }
      }
    }
  }

  private fun onRemoteRemove(type: RemoteDataType, data: RemoteUploadData) {
    if (!isValid()) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }

    val logInfo = "onRemoteRemove(${type.name}, ${data.uuid})"
    log("GDriveRemote", logInfo)
    when (type) {
      RemoteDataType.NOTE -> {
        IRemoteDatabaseUtils.onRemoteRemoveNote(context, data.uuid)
        remoteDatabaseController.remoteDatabaseUpdate(RemoteDataType.NOTE_META, data.uuid, databaseUpdateLambda)
      }
      RemoteDataType.NOTE_META -> {
      } // Should never happen as note is handling this deletion
      RemoteDataType.TAG -> IRemoteDatabaseUtils.onRemoteRemoveTag(context, data.uuid)
      RemoteDataType.FOLDER -> IRemoteDatabaseUtils.onRemoteRemoveFolder(context, data.uuid)
      RemoteDataType.IMAGE -> {
        val imageUUID = toImageUUID(data.uuid)
        if (imageUUID !== null) {
          val imageFile = ApplicationBase.noteImagesFolder.getFile(imageUUID.noteUuid, imageUUID.imageUuid)
          imageFile.delete()
        }
      }
    }
    remoteDatabaseController.remoteDatabaseUpdate(type, data.uuid, databaseUpdateLambda)
  }

}