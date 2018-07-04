package com.maubis.scarlet.base.support.database

import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.core.database.FoldersProvider
import com.maubis.scarlet.base.core.database.TagsProvider
import com.maubis.scarlet.base.core.database.room.folder.FolderDao
import com.maubis.scarlet.base.core.database.room.tag.TagDao

val foldersDB: FoldersProvider get() = CoreConfig.instance.foldersDatabase()

class FoldersDB : FoldersProvider() {

  override fun database(): FolderDao = CoreConfig.instance.database().folders()

}