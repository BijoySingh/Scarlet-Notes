package com.maubis.scarlet.base.core.folder

import com.maubis.scarlet.base.config.ApplicationBase.Companion.folderSync
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.export.data.ExportableFolder

open class MaterialFolderActor(val folder: Folder) : IFolderActor {
  override fun offlineSave() {
    val id = CoreConfig.instance.foldersDatabase().database().insertFolder(folder)
    folder.uid = if (folder.isUnsaved()) id.toInt() else folder.uid

    CoreConfig.instance.foldersDatabase().notifyInsertFolder(folder)
  }

  override fun onlineSave() {
    folderSync?.insert(ExportableFolder(folder))
  }

  override fun save() {
    offlineSave()
    onlineSave()
  }

  override fun offlineDelete() {
    if (folder.isUnsaved()) {
      return
    }
    CoreConfig.instance.foldersDatabase().database().delete(folder)
    CoreConfig.instance.foldersDatabase().notifyDelete(folder)
    folder.uid = 0
  }

  override fun delete() {
    offlineDelete()
    folderSync?.remove(ExportableFolder(folder))
  }

}