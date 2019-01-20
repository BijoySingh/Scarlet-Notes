package com.maubis.scarlet.base.export.remote

import android.content.Context
import android.os.Environment
import com.maubis.scarlet.base.core.folder.IFolderContainer
import com.maubis.scarlet.base.core.note.INoteContainer
import com.maubis.scarlet.base.core.note.NoteState
import com.maubis.scarlet.base.core.tag.ITagContainer
import com.maubis.scarlet.base.database.remote.IRemoteDatabase
import com.maubis.scarlet.base.database.remote.IRemoteDatabaseUtils
import com.maubis.scarlet.base.export.data.ExportableFolder
import com.maubis.scarlet.base.export.data.ExportableNote
import com.maubis.scarlet.base.export.data.ExportableTag
import com.maubis.scarlet.base.export.sheet.ExternalFolderSyncBottomSheet.Companion.folderSyncBackupLocked
import com.maubis.scarlet.base.export.sheet.ExternalFolderSyncBottomSheet.Companion.folderSyncPath
import java.io.File
import java.lang.ref.WeakReference

class FolderRemoteDatabase(val weakContext: WeakReference<Context>) : IRemoteDatabase {

  private var isValidController: Boolean = true

  private var rootFolder: File? = null
  private var notesRemoteFolder: RemoteFolder<ExportableNote>? = null
  private var tagsRemoteFolder: RemoteFolder<ExportableTag>? = null
  private var foldersRemoteFolder: RemoteFolder<ExportableFolder>? = null

  override fun init(userId: String) {}

  fun init(onNotesInit: () -> Unit = {}, onTagsInit: () -> Unit = {}, onFoldersInit: () -> Unit = {}) {
    isValidController = true
    rootFolder = File(Environment.getExternalStorageDirectory(), folderSyncPath)
    notesRemoteFolder = RemoteFolder(
        File(rootFolder, "notes"),
        ExportableNote::class.java,
        { it -> onRemoteInsert(it) },
        { it -> onRemoteRemove(ExportableNote(it, "", 0L, 0L, 0, NoteState.DEFAULT.name, "", emptyMap(), "")) },
        onNotesInit)
    tagsRemoteFolder = RemoteFolder(
        File(rootFolder, "tags"),
        ExportableTag::class.java,
        { it -> onRemoteInsert(it) },
        { it -> onRemoteRemove(ExportableTag(it, "")) },
        onTagsInit)
    foldersRemoteFolder = RemoteFolder(
        File(rootFolder, "folders"),
        ExportableFolder::class.java,
        { it -> onRemoteInsert(it) },
        { it -> onRemoteRemove(ExportableFolder(it, "", 0L, 0L, 0)) },
        onFoldersInit)
  }

  override fun reset() {
    isValidController = false
    notesRemoteFolder = null
    tagsRemoteFolder = null
    foldersRemoteFolder = null
  }

  override fun logout() {
    reset()
  }

  override fun deleteEverything() {
    if (!isValidController) {
      return
    }
    notesRemoteFolder?.deleteEverything()
    tagsRemoteFolder?.deleteEverything()
    foldersRemoteFolder?.deleteEverything()
  }

  override fun insert(note: INoteContainer) {
    if (!isValidController || note !is ExportableNote) {
      return
    }
    if (note.locked() && folderSyncBackupLocked) {
      notesRemoteFolder?.lock(note.uuid())
    }
    notesRemoteFolder?.insert(note.uuid(), note)
  }

  override fun insert(tag: ITagContainer) {
    if (!isValidController || tag !is ExportableTag) {
      return
    }
    tagsRemoteFolder?.insert(tag.uuid(), tag)
  }

  override fun insert(folder: IFolderContainer) {
    if (!isValidController || folder !is ExportableFolder) {
      return
    }
    foldersRemoteFolder?.insert(folder.uuid(), folder)
  }

  override fun remove(note: INoteContainer) {
    if (!isValidController || note !is ExportableNote) {
      return
    }
    notesRemoteFolder?.delete(note.uuid())
  }

  override fun remove(tag: ITagContainer) {
    if (!isValidController || tag !is ExportableTag) {
      return
    }
    tagsRemoteFolder?.delete(tag.uuid())
  }

  override fun remove(folder: IFolderContainer) {
    if (!isValidController || folder !is ExportableFolder) {
      return
    }
    foldersRemoteFolder?.delete(folder.uuid())
  }

  override fun onRemoteInsert(note: INoteContainer) {
    if (!isValidController || note !is ExportableNote) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }
    IRemoteDatabaseUtils.onRemoteInsert(context, note)
  }

  override fun onRemoteRemove(note: INoteContainer) {
    if (!isValidController || note !is ExportableNote) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }
    IRemoteDatabaseUtils.onRemoteRemove(context, note)
  }

  override fun onRemoteInsert(tag: ITagContainer) {
    if (!isValidController || tag !is ExportableTag) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }
    IRemoteDatabaseUtils.onRemoteInsert(context, tag)
  }

  override fun onRemoteRemove(tag: ITagContainer) {
    if (!isValidController || tag !is ExportableTag) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }
    IRemoteDatabaseUtils.onRemoteRemove(context, tag)
  }

  override fun onRemoteInsert(folder: IFolderContainer) {
    if (!isValidController || folder !is ExportableFolder) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }
    IRemoteDatabaseUtils.onRemoteInsert(context, folder)
  }

  override fun onRemoteRemove(folder: IFolderContainer) {
    if (!isValidController || folder !is ExportableFolder) {
      return
    }

    val context = weakContext.get()
    if (context === null) {
      return
    }
    IRemoteDatabaseUtils.onRemoteRemove(context, folder)
  }

}