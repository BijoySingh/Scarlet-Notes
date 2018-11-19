package com.maubis.scarlet.base.support.database

import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.database.FoldersProvider
import com.maubis.scarlet.base.database.TagsProvider
import com.maubis.scarlet.base.database.room.folder.FolderDao
import com.maubis.scarlet.base.database.room.tag.TagDao

val foldersDB: FoldersProvider get() = CoreConfig.instance.foldersDatabase()

class FoldersDB : FoldersProvider() {

  override fun database(): FolderDao = CoreConfig.instance.database().folders()

}