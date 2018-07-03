package com.maubis.scarlet.base.core.folder

import com.maubis.scarlet.base.core.database.room.folder.Folder

fun Folder.isUnsaved(): Boolean {
  return uid == 0
}
