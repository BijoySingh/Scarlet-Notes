package com.maubis.scarlet.base.core.folder

import com.github.bijoysingh.starter.util.RandomHelper
import com.maubis.scarlet.base.core.database.room.folder.Folder

class FolderBuilder() {
  fun emptyFolder(): Folder {
    return emptyFolder(-0xff8695)
  }

  fun emptyFolder(color: Int): Folder {
    val folder = Folder()
    folder.uid = 0
    folder.title = ""
    folder.uuid = RandomHelper.getRandomString(24)
    folder.timestamp = System.currentTimeMillis()
    folder.updateTimestamp = System.currentTimeMillis()
    folder.color = color
    return folder
  }

  fun copy(folderContainer: IFolderContainer): Folder {
    val folder = emptyFolder()
    folder.uuid = folderContainer.uuid()
    folder.title = folderContainer.title()
    folder.timestamp = folderContainer.timestamp()
    folder.updateTimestamp = folderContainer.updateTimestamp()
    folder.color = folderContainer.color()
    return folder
  }
}