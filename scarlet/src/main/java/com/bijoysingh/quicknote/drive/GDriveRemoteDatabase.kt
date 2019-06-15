package com.bijoysingh.quicknote.drive

import android.content.Context
import com.bijoysingh.quicknote.Scarlet
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
import com.maubis.scarlet.base.core.note.NoteBuilder
import com.maubis.scarlet.base.database.remote.IRemoteDatabaseUtils
import com.maubis.scarlet.base.export.data.*
import com.maubis.scarlet.base.settings.sheet.sNoteDefaultColor
import com.maubis.scarlet.base.support.utils.log
import com.maubis.scarlet.base.support.utils.maybeThrow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

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

fun folderIdForFolderName(folderName: String, folderId: String = ""): String {
  val key = "g_drive_folder_if_for_$folderName"
  if (folderId.isEmpty()) {
    // Get Condition
    var storedValue = gDriveConfig?.get(key, "") ?: ""
    if (storedValue == INVALID_FILE_ID) {
      gDriveConfig?.put(key, "")
      storedValue = ""
    }
    log("GDrive", "folderIdForFolderName($folderName, $storedValue")
    return storedValue
  }

  if (folderId != INVALID_FILE_ID) {
    gDriveConfig?.put(key, folderId)
  }
  return folderId
}

class GDriveRemoteDatabase(private val weakContext: WeakReference<Context>) {

  lateinit var gDriveDbState: GDriveRemoteDatabaseState

  private var gDriveDatabase: GDriveUploadDataDao? = null

  private var isValidController: Boolean = true
  private var driveHelper: GDriveServiceHelper? = null

  private var notesSync: GDriveRemoteFolder<String>? = null
  private var notesMetaSync: GDriveRemoteFolder<ExportableNoteMeta>? = null
  private var foldersSync: GDriveRemoteFolder<ExportableFolder>? = null
  private var tagsSync: GDriveRemoteFolder<ExportableTag>? = null
  private var imageSync: GDriveRemoteImageFolder? = null
  private var syncing = HashMap<GDriveDataType, AtomicBoolean>()
  private var syncListener: IPendingUploadListener? = null
  private var pendingSyncs: AtomicInteger = AtomicInteger(0)
  private var databaseUpdateLambda: () -> Unit = { verifyAndNotifyPendingStateChange() }

  fun init(helper: GDriveServiceHelper) {
    val context = weakContext.get()
    if (context === null) {
      return
    }

    isValidController = true
    driveHelper = helper
    gDriveDatabase = genGDriveUploadDatabase(context)
    gDriveDbState = Scarlet.gDriveDbState!!

    syncing[GDriveDataType.NOTE] = AtomicBoolean(false)
    syncing[GDriveDataType.TAG] = AtomicBoolean(false)
    syncing[GDriveDataType.FOLDER] = AtomicBoolean(false)
    syncing[GDriveDataType.NOTE_META] = AtomicBoolean(false)
    syncing[GDriveDataType.IMAGE] = AtomicBoolean(false)

    notesSync = GDriveRemoteFolder(
        dataType = GDriveDataType.NOTE,
        database = gDriveDatabase!!,
        helper = helper,
        onPendingChange = { verifyAndNotifyPendingStateChange() },
        onPendingSyncComplete = { decrementPendingSyncs() },
        serialiser = { it },
        uuidToObject = {
          ApplicationBase.instance.notesDatabase().getByUUID(it)?.toExportedMarkdown()
        })
    notesMetaSync = GDriveRemoteFolder(
        dataType = GDriveDataType.NOTE_META,
        database = gDriveDatabase!!,
        helper = helper,
        onPendingChange = { verifyAndNotifyPendingStateChange() },
        onPendingSyncComplete = { decrementPendingSyncs() },
        serialiser = { Gson().toJson(it) },
        uuidToObject = {
          ApplicationBase.instance.notesDatabase().getByUUID(it)?.getExportableNoteMeta()
        })
    tagsSync = GDriveRemoteFolder(
        dataType = GDriveDataType.TAG,
        database = gDriveDatabase!!,
        helper = helper,
        onPendingChange = { verifyAndNotifyPendingStateChange() },
        onPendingSyncComplete = { decrementPendingSyncs() },
        serialiser = { Gson().toJson(it) },
        uuidToObject = {
          ApplicationBase.instance.tagsDatabase().getByUUID(it)?.getExportableTag()
        })
    foldersSync = GDriveRemoteFolder(
        dataType = GDriveDataType.FOLDER,
        database = gDriveDatabase!!,
        helper = helper,
        onPendingChange = { verifyAndNotifyPendingStateChange() },
        onPendingSyncComplete = { decrementPendingSyncs() },
        serialiser = { Gson().toJson(it) },
        uuidToObject = {
          ApplicationBase.instance.foldersDatabase().getByUUID(it)?.getExportableFolder()
        })
    imageSync = GDriveRemoteImageFolder(
        dataType = GDriveDataType.IMAGE,
        database = gDriveDatabase!!,
        helper = helper,
        onPendingChange = { verifyAndNotifyPendingStateChange() },
        onPendingSyncComplete = { decrementPendingSyncs() })

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
      gDriveConfig?.clearSync()
    }
  }

  /**
   * Initialisation Methods
   */
  private fun initSubRootFolder(folderName: String, folderId: String) {
    incrementPendingSyncs()
    when (folderName) {
      FOLDER_NAME_NOTES -> notesSync?.initContentFolderId(folderId) {
        if (!sGDriveFirstSyncNote) {
          GlobalScope.launch { resyncDataSync(GDriveDataType.NOTE) }
          sGDriveFirstSyncNote = true
        }
        decrementPendingSyncs()
      }
      FOLDER_NAME_NOTES_META -> {
        notesMetaSync?.initContentFolderId(folderId) {
          if (!sGDriveFirstSyncNoteMeta) {
            GlobalScope.launch { resyncDataSync(GDriveDataType.NOTE_META) }
            sGDriveFirstSyncNoteMeta = true
          }
          decrementPendingSyncs()
        }
        notesMetaSync?.initDeletedFolderId(INVALID_FILE_ID) {}
      }
      FOLDER_NAME_TAGS -> tagsSync?.initContentFolderId(folderId) {
        if (!sGDriveFirstSyncTag) {
          GlobalScope.launch { resyncDataSync(GDriveDataType.TAG) }
          sGDriveFirstSyncTag = true
        }
        decrementPendingSyncs()
      }
      FOLDER_NAME_FOLDERS -> foldersSync?.initContentFolderId(folderId) {
        if (!sGDriveFirstSyncFolder) {
          GlobalScope.launch { resyncDataSync(GDriveDataType.FOLDER) }
          sGDriveFirstSyncFolder = true
        }
        decrementPendingSyncs()
      }
      FOLDER_NAME_IMAGES -> imageSync?.initContentFolderId(folderId) {
        if (!sGDriveFirstSyncImage) {
          GlobalScope.launch { resyncDataSync(GDriveDataType.IMAGE) }
          sGDriveFirstSyncImage = true
        }
        decrementPendingSyncs()
      }
      FOLDER_NAME_DELETED_NOTES -> notesSync?.initDeletedFolderId(folderId) {
        decrementPendingSyncs()
      }
      FOLDER_NAME_DELETED_TAGS -> tagsSync?.initDeletedFolderId(folderId) {
        decrementPendingSyncs()
      }
      FOLDER_NAME_DELETED_FOLDERS -> foldersSync?.initDeletedFolderId(folderId) {
        decrementPendingSyncs()
      }
    }
  }

  private fun onRootFolderLoaded(rootFolderId: String) {
    createFolders(rootFolderId, listOf(FOLDER_NAME_NOTES, FOLDER_NAME_NOTES_META, FOLDER_NAME_FOLDERS, FOLDER_NAME_TAGS, FOLDER_NAME_IMAGES))
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
    if (listener !== null) {
      verifyAndNotifyPendingStateChange()
    }
  }

  private fun verifyAndNotifyPendingStateChange() {
    GlobalScope.launch {
      val database = gDriveDatabase
      if (database === null) {
        return@launch
      }

      val currentPendingState = database.getPendingCount() > 0

      val pending = database.getAllPending().map { "type=${it.type}, uuid=${it.uuid}, fid=${it.fileId}" }.joinToString(separator = "\n")
      log("GDrive", "getPendingCount(${database.getPendingCount()})\n$pending")
      syncListener?.onPendingStateUpdate(currentPendingState)
    }
  }

  @Synchronized
  private fun decrementPendingSyncs() {
    pendingSyncs.decrementAndGet()
    if (pendingSyncs.get() <= 0) {
      pendingSyncs.set(0)
      syncListener?.onPendingSyncsUpdate(false)
    }
  }

  @Synchronized
  private fun incrementPendingSyncs() {
    pendingSyncs.incrementAndGet()
    if (pendingSyncs.get() >= 1) {
      syncListener?.onPendingSyncsUpdate(true)
    }
  }

  /**
   * Notify local changes to the notes
   */
  fun notifyChange() {
    if (!isValidController) {
      return
    }

    verifyAndNotifyPendingStateChange()
  }

  /**
   * Resync Functions
   */

  @Synchronized
  fun resync() {
    if (!isValidController) {
      return
    }

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

    val pendingItems = gDriveDatabase?.getPendingByType(type.name) ?: emptyList()
    for (pendingItem in pendingItems) {
      if (!gDriveDbState.notifyAttempt(type, pendingItem.uuid)) {
        // Think of a better solution here...
        // gDriveDatabase?.delete(pendingItem)
        gDriveDbState.remoteDatabaseUpdate(type, pendingItem.uuid, databaseUpdateLambda)
        continue
      }

      log("GDrive", "resyncDataSync(${type.name}, ${pendingItem.uuid}, ${pendingItem.lastUpdateTimestamp}, ${pendingItem.gDriveUpdateTimestamp})")
      val sameDelete = pendingItem.localStateDeleted == pendingItem.gDriveStateDeleted
      val localDeleted = pendingItem.localStateDeleted
      val sameUpdateTime = pendingItem.lastUpdateTimestamp == pendingItem.gDriveUpdateTimestamp
      if (!sameUpdateTime || !sameUpdateTime) {
        when {
          sameDelete && pendingItem.lastUpdateTimestamp > pendingItem.gDriveUpdateTimestamp && localDeleted -> remove(type, pendingItem)
          sameDelete && pendingItem.lastUpdateTimestamp > pendingItem.gDriveUpdateTimestamp -> insert(type, pendingItem)
          sameDelete && pendingItem.lastUpdateTimestamp > pendingItem.gDriveUpdateTimestamp && localDeleted -> onRemoteRemove(type, pendingItem)
          sameDelete && pendingItem.lastUpdateTimestamp < pendingItem.gDriveUpdateTimestamp -> onRemoteInsert(type, pendingItem)
          !sameDelete && pendingItem.lastUpdateTimestamp > pendingItem.gDriveUpdateTimestamp -> remove(type, pendingItem)
          !sameDelete && pendingItem.lastUpdateTimestamp < pendingItem.gDriveUpdateTimestamp -> onRemoteRemove(type, pendingItem)
          !sameDelete && sameUpdateTime -> gDriveDbState.remoteDatabaseUpdate(type, pendingItem.uuid, databaseUpdateLambda) // Ignoring
        }
      }
    }
    syncing[type]?.set(false)
  }

  /**
   * Core Data Functions
   */

  private fun insert(type: GDriveDataType, data: GDriveUploadData) {
    if (!isValidController) {
      return
    }

    log("GDriveRemote", "insert(${type.name}, ${data.uuid})")
    incrementPendingSyncs()
    when (type) {
      GDriveDataType.NOTE -> {
        ApplicationBase.instance.notesDatabase().getByUUID(data.uuid)?.toExportedMarkdown()?.apply {
          notesSync?.insert(data.uuid, this)
        } ?: decrementPendingSyncs()
      }
      GDriveDataType.NOTE_META -> {
        ApplicationBase.instance.notesDatabase().getByUUID(data.uuid)?.getExportableNoteMeta()?.apply {
          notesMetaSync?.insert(data.uuid, this)
        } ?: decrementPendingSyncs()
      }
      GDriveDataType.TAG -> {
        ApplicationBase.instance.tagsDatabase().getByUUID(data.uuid)?.getExportableTag()?.apply {
          tagsSync?.insert(this.uuid, this)
        } ?: decrementPendingSyncs()
      }
      GDriveDataType.FOLDER -> {
        ApplicationBase.instance.foldersDatabase().getByUUID(data.uuid)?.getExportableFolder()?.apply {
          foldersSync?.insert(this.uuid, this)
        } ?: decrementPendingSyncs()
      }
      GDriveDataType.IMAGE -> {
        toImageUUID(data.uuid)?.apply {
          imageSync?.insert(this)
        } ?: decrementPendingSyncs()
      }
    }
  }

  private fun remove(type: GDriveDataType, data: GDriveUploadData) {
    if (!isValidController) {
      return
    }

    log("GDriveRemote", "remove(${type.name}, ${data.uuid})")
    incrementPendingSyncs()
    val uuid = data.uuid
    when (type) {
      GDriveDataType.NOTE -> notesSync?.delete(uuid)
      GDriveDataType.NOTE_META -> notesMetaSync?.delete(uuid)
      GDriveDataType.TAG -> tagsSync?.delete(uuid)
      GDriveDataType.FOLDER -> foldersSync?.delete(uuid)
      GDriveDataType.IMAGE -> {
        val imageUUID = toImageUUID(uuid)
        when {
          imageUUID !== null -> imageSync?.delete(imageUUID)
          else -> decrementPendingSyncs()
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

    log("GDriveRemote", "onRemoteInsert(${type.name}, ${data.uuid})")
    incrementPendingSyncs()
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

            gDriveDbState.remoteDatabaseUpdate(GDriveDataType.NOTE, data.uuid, databaseUpdateLambda)
          } catch (exception: Exception) {
            maybeThrow(exception)
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

            gDriveDbState.remoteDatabaseUpdate(GDriveDataType.NOTE_META, data.uuid, databaseUpdateLambda)
          } catch (exception: Exception) {
            maybeThrow(exception)
          }
        }
      }
      GDriveDataType.TAG -> {
        onRemoteInsertImpl(data.fileId) {
          try {
            val item = Gson().fromJson(it, ExportableTag::class.java)
            IRemoteDatabaseUtils.onRemoteInsert(context, item)
            gDriveDbState.remoteDatabaseUpdate(GDriveDataType.TAG, data.uuid, databaseUpdateLambda)
          } catch (exception: Exception) {
            maybeThrow(exception)
          }
        }
      }
      GDriveDataType.FOLDER -> {
        onRemoteInsertImpl(data.fileId) {
          try {
            val item = Gson().fromJson(it, ExportableFolder::class.java)
            IRemoteDatabaseUtils.onRemoteInsert(context, item)
            gDriveDbState.remoteDatabaseUpdate(GDriveDataType.FOLDER, data.uuid, databaseUpdateLambda)
          } catch (exception: Exception) {
            maybeThrow(exception)
          }
        }
      }
      GDriveDataType.IMAGE -> {
        val imageUUID = toImageUUID(data.uuid)
        if (imageUUID !== null) {
          val imageFile = noteImagesFolder.getFile(imageUUID.noteUuid, imageUUID.imageUuid)
          if (imageFile.exists()) {
            gDriveDbState.remoteDatabaseUpdate(GDriveDataType.IMAGE, data.uuid, databaseUpdateLambda)
            decrementPendingSyncs()
            return
          }

          driveHelper?.readFile(data.fileId, imageFile)?.addOnCompleteListener {
            if (it.result == true) {
              gDriveDbState.remoteDatabaseUpdate(GDriveDataType.IMAGE, data.uuid, databaseUpdateLambda)
            }
            decrementPendingSyncs()
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

    log("GDriveRemote", "onRemoteRemove(${type.name}, ${data.uuid})")
    incrementPendingSyncs()
    when (type) {
      GDriveDataType.NOTE -> {
        IRemoteDatabaseUtils.onRemoteRemoveNote(context, data.uuid)
        gDriveDbState.remoteDatabaseUpdate(GDriveDataType.NOTE_META, data.uuid, databaseUpdateLambda)
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
    gDriveDbState.remoteDatabaseUpdate(type, data.uuid, databaseUpdateLambda)
    decrementPendingSyncs()
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
      decrementPendingSyncs()
    }
  }
}