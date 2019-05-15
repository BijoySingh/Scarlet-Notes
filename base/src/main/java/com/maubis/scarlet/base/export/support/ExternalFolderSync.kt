package com.maubis.scarlet.base.export.support

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.content.ContextCompat
import com.github.bijoysingh.starter.util.ToastHelper
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.ApplicationBase.Companion.folderSync
import com.maubis.scarlet.base.export.data.ExportableFolder
import com.maubis.scarlet.base.export.data.ExportableNote
import com.maubis.scarlet.base.export.data.ExportableTag
import com.maubis.scarlet.base.export.remote.FolderRemoteDatabase
import com.maubis.scarlet.base.export.sheet.NOTES_EXPORT_FOLDER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference


const val KEY_EXTERNAL_FOLDER_SYNC_ENABLED = "external_folder_sync_enabled"
const val KEY_EXTERNAL_FOLDER_SYNC_LAST_SCAN = "external_folder_sync_last_sync"
const val KEY_EXTERNAL_FOLDER_SYNC_BACKUP_LOCKED = "external_folder_sync_backup_locked"
const val KEY_EXTERNAL_FOLDER_SYNC_PATH = "external_folder_sync_path"

var sExternalFolderSync: Boolean
  get() = ApplicationBase.instance.store().get(KEY_EXTERNAL_FOLDER_SYNC_ENABLED, false)
  set(value) = ApplicationBase.instance.store().put(KEY_EXTERNAL_FOLDER_SYNC_ENABLED, value)

var sFolderSyncPath: String
  get() = ApplicationBase.instance.store().get(KEY_EXTERNAL_FOLDER_SYNC_PATH, "$NOTES_EXPORT_FOLDER/Sync/")
  set(value) = ApplicationBase.instance.store().put(KEY_EXTERNAL_FOLDER_SYNC_PATH, value)

var sFolderSyncBackupLocked: Boolean
  get() = ApplicationBase.instance.store().get(KEY_EXTERNAL_FOLDER_SYNC_BACKUP_LOCKED, true)
  set(value) = ApplicationBase.instance.store().put(KEY_EXTERNAL_FOLDER_SYNC_BACKUP_LOCKED, value)

object ExternalFolderSync {

  fun hasPermission(context: Context): Boolean {
    return !(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
  }

  fun enable(context: Context, enabled: Boolean) {
    if (enabled) {
      if (!hasPermission(context)) {
        GlobalScope.launch(Dispatchers.Main) {
          ToastHelper.show(context, R.string.permission_layout_give_permission_details)
          folderSync?.reset()
        }
        return
      }
      sExternalFolderSync = true
      loadFirstTime()
    } else {
      sExternalFolderSync = false
      folderSync?.reset()
    }
  }

  fun loadFirstTime() {
    folderSync?.init(
        {
          ApplicationBase.instance.notesDatabase().getAll().forEach {
            folderSync?.insert(ExportableNote(it))
          }
        },
        {
          ApplicationBase.instance.tagsDatabase().getAll().forEach {
            folderSync?.insert(ExportableTag(it))
          }
        },
        {
          ApplicationBase.instance.foldersDatabase().getAll().forEach {
            folderSync?.insert(ExportableFolder(it))
          }
        })
  }

  fun setup(context: Context) {
    if (!sExternalFolderSync) {
      return
    }

    if (!hasPermission(context)) {
      sExternalFolderSync = false
      return
    }
    folderSync = FolderRemoteDatabase(WeakReference(context))
    folderSync?.init()
  }
}