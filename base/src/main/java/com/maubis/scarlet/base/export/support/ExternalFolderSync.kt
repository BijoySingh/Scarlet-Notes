package com.maubis.scarlet.base.export.support

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.content.ContextCompat
import com.github.bijoysingh.starter.util.ToastHelper
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.export.data.ExportableFolder
import com.maubis.scarlet.base.export.data.ExportableNote
import com.maubis.scarlet.base.export.data.ExportableTag
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

const val KEY_EXTERNAL_FOLDER_SYNC_ENABLED = "external_folder_sync_enabled"
const val KEY_EXTERNAL_FOLDER_SYNC_LAST_SCAN = "external_folder_sync_last_sync"

object ExternalFolderSync {
  fun enable(context: Context, enabled: Boolean) {
    if (enabled) {
      if (Build.VERSION.SDK_INT >= 23
          && ContextCompat.checkSelfPermission(
              context,
              Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
        launch(UI) {
          ToastHelper.show(context, R.string.permission_layout_give_permission_details)
          CoreConfig.instance.externalFolderSync().reset()
        }
        return
      }
      CoreConfig.instance.store().put(KEY_EXTERNAL_FOLDER_SYNC_ENABLED, true)
      loadFirstTime()
    } else {
      CoreConfig.instance.store().put(KEY_EXTERNAL_FOLDER_SYNC_ENABLED, false)
      CoreConfig.instance.externalFolderSync().reset()
    }
  }

  fun loadFirstTime() {
    CoreConfig.instance.externalFolderSync().init(
        {
          CoreConfig.instance.notesDatabase().getAll().forEach {
            CoreConfig.instance.externalFolderSync().insert(ExportableNote(it))
          }
        },
        {
          CoreConfig.instance.tagsDatabase().getAll().forEach {
            CoreConfig.instance.externalFolderSync().insert(ExportableTag(it))
          }
        },
        {
          CoreConfig.instance.foldersDatabase().getAll().forEach {
            CoreConfig.instance.externalFolderSync().insert(ExportableFolder(it))
          }
        })
  }

  fun load() {
    if (!CoreConfig.instance.store().get(KEY_EXTERNAL_FOLDER_SYNC_ENABLED, false)) {
      return
    }
    CoreConfig.instance.externalFolderSync().init()
  }
}