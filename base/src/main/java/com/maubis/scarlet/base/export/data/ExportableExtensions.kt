package com.maubis.scarlet.base.export.data

import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.database.room.tag.Tag

fun Note.getExportableNoteMeta(): ExportableNoteMeta {
  return ExportableNoteMeta(
      uuid,
      timestamp,
      updateTimestamp,
      color,
      state,
      if (tags == null) "" else tags,
      locked,
      pinned,
      folder
  )
}

fun Folder.getExportableFolder(): ExportableFolder {
  return ExportableFolder(
      uuid,
      title,
      timestamp,
      updateTimestamp,
      color
  )
}

fun Tag.getExportableTag(): ExportableTag = ExportableTag(uuid, title)