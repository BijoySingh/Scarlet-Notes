package com.maubis.scarlet.base.core.folder

import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.ApplicationBase.Companion.folderSync
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.export.data.ExportableFolder

open class MaterialFolderActor(val folder: Folder) : IFolderActor {
  override fun offlineSave() {
    val id = ApplicationBase.instance.foldersDatabase().database().insertFolder(folder)
    folder.uid = if (folder.isUnsaved()) id.toInt() else folder.uid

    ApplicationBase.instance.foldersDatabase().notifyInsertFolder(folder)
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
    ApplicationBase.instance.foldersDatabase().database().delete(folder)
    ApplicationBase.instance.foldersDatabase().notifyDelete(folder)
    folder.uid = 0
  }

  override fun delete() {
    offlineDelete()
    folderSync?.remove(ExportableFolder(folder))
  }

}