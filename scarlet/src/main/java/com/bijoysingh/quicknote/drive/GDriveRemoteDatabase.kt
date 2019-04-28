package com.bijoysingh.quicknote.drive

import android.content.Context
import com.bijoysingh.quicknote.Scarlet.Companion.gDrive
import com.bijoysingh.quicknote.firebase.data.*
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.folder.IFolderContainer
import com.maubis.scarlet.base.core.note.INoteContainer
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.core.tag.ITagContainer
import com.maubis.scarlet.base.database.remote.IRemoteDatabase
import com.maubis.scarlet.base.database.remote.IRemoteDatabaseUtils
import com.maubis.scarlet.base.note.getImageIds
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

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

    notesSync = GDriveRemoteFolder(
        FirebaseNote::class.java,
        helper,
        { it -> CoreConfig.instance.notesDatabase().getByUUID(it)?.getFirebaseNote() },
        { it -> onRemoteInsert(it) },
        { it -> onRemoteRemove(FirebaseNote(it, "", 0L, 0L, 0, NoteState.DEFAULT.name, "", false, false, "")) })
    tagsSync = GDriveRemoteFolder(
        FirebaseTag::class.java,
        helper,
        { it -> CoreConfig.instance.tagsDatabase().getByUUID(it)?.getFirebaseTag() },
        { it -> onRemoteInsert(it) },
        { it -> onRemoteRemove(FirebaseTag(it, "")) })
    foldersSync = GDriveRemoteFolder(
        FirebaseFolder::class.java,
        helper,
        { it -> CoreConfig.instance.foldersDatabase().getByUUID(it)?.getFirebaseFolder() },
        { it -> onRemoteInsert(it) },
        { it -> onRemoteRemove(FirebaseFolder(it, "", 0L, 0L, 0)) })
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

  fun onRootFolderLoaded(rootFolderId: String) {
    driveHelper?.getOrCreateDirectory(rootFolderId, "notes") {
      if (it !== null) {
        notesSync?.init(it) {
          if (!sGDriveFirstSyncNote) {
            CoreConfig.instance.notesDatabase().getAll().forEach {
              gDrive?.insert(it.getFirebaseNote())
            }
            sGDriveFirstSyncNote = true
          } else {
            val ids = CoreConfig.instance.notesDatabase().getUUIDs()
            notesSync?.notifyingExistingIds(ids)
          }
        }
      }
    }
    driveHelper?.getOrCreateDirectory(rootFolderId, "tags") {
      if (it !== null) {
        tagsSync?.init(it) {
          if (!sGDriveFirstSyncTag) {
            CoreConfig.instance.tagsDatabase().getAll().forEach {
              gDrive?.insert(it.getFirebaseTag())
            }
            sGDriveFirstSyncTag = true
          } else {
            val ids = CoreConfig.instance.tagsDatabase().getUUIDs()
            tagsSync?.notifyingExistingIds(ids)
          }
        }
      }
    }
    driveHelper?.getOrCreateDirectory(rootFolderId, "folders") {
      if (it !== null) {
        foldersSync?.init(it) {
          if (!sGDriveFirstSyncFolder) {
            CoreConfig.instance.foldersDatabase().getAll().forEach {
              gDrive?.insert(it.getFirebaseFolder())
            }
            sGDriveFirstSyncFolder = true
          } else {
            val ids = CoreConfig.instance.foldersDatabase().getUUIDs()
            foldersSync?.notifyingExistingIds(ids)
          }
        }
      }
    }
    driveHelper?.getOrCreateDirectory(rootFolderId, "images") {
      if (it !== null) {
        imageSync?.init(it) {
          val imageIds = emptySet<ImageUUID>().toMutableSet()
          CoreConfig.instance.notesDatabase().getAll().map {
            Pair(it.uuid, it.getImageIds())
          }.forEach { idImagesPair ->
            idImagesPair.second.forEach { imageId ->
              imageIds.add(ImageUUID(idImagesPair.first, imageId))
            }
          }
          imageSync?.notifyingExistingIds(imageIds)
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

}