package com.maubis.scarlet.base.core.tag

import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.database.room.tag.Tag
import com.maubis.scarlet.base.export.data.ExportableTag

open class MaterialTagActor(val tag: Tag) : ITagActor {
  override fun offlineSave() {
    val id = ApplicationBase.instance.tagsDatabase().database().insertTag(tag)
    tag.uid = if (tag.isUnsaved()) id.toInt() else tag.uid
    ApplicationBase.instance.tagsDatabase().notifyInsertTag(tag)
  }

  override fun onlineSave() {
    ApplicationBase.folderSync?.insert(ExportableTag(tag))
  }

  override fun save() {
    offlineSave()
    onlineSave()
  }

  override fun offlineDelete() {
    if (tag.isUnsaved()) {
      return
    }
    ApplicationBase.instance.tagsDatabase().database().delete(tag)
    ApplicationBase.instance.tagsDatabase().notifyDelete(tag)
    tag.uid = 0
  }

  override fun delete() {
    offlineDelete()
    ApplicationBase.folderSync?.remove(ExportableTag(tag))
  }

}