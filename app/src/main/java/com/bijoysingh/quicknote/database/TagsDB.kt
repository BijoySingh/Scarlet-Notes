package com.bijoysingh.quicknote.database

import com.bijoysingh.quicknote.MaterialNotes.Companion.db
import com.maubis.scarlet.base.database.TagsProvider
import com.maubis.scarlet.base.database.room.tag.TagDao

val tagsDB = TagsDB()

class TagsDB : TagsProvider() {

  override fun database(): TagDao = db().tags()

}