package com.maubis.scarlet.base.core.folder

import com.github.bijoysingh.starter.util.RandomHelper
import com.maubis.scarlet.base.core.database.room.folder.Folder

class FolderBuilder() {
  fun emptyFolder(): Folder {
    val folder = Folder()
    folder.uid = 0
    folder.title = ""
    folder.uuid = RandomHelper.getRandomString(24)
    return folder
  }
}