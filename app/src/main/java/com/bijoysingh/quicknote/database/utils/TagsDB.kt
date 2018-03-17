package com.bijoysingh.quicknote.database.utils

import android.util.Log
import com.bijoysingh.quicknote.MaterialNotes
import com.bijoysingh.quicknote.database.Tag
import com.bijoysingh.quicknote.database.TagDao
import com.google.gson.Gson

class TagsDB {

  val tags = HashMap<String, Tag>()

  fun notifyInsertTag(tag: Tag) {
    maybeLoadFromDB()
    tags[tag.uuid] = tag
  }

  fun delete(tag: Tag) {
    maybeLoadFromDB()
    tags.remove(tag.uuid)
    db().delete(tag)
  }

  fun getCount(): Int {
    maybeLoadFromDB()
    return tags.size
  }

  fun getAll(): List<Tag> {
    maybeLoadFromDB()
    return tags.values.toList()
  }

  fun getByID(uid: Int): Tag? {
    maybeLoadFromDB()
    return tags.values.firstOrNull { it.uid == uid }
  }

  fun getByUUID(uuid: String): Tag? {
    maybeLoadFromDB()
    return tags[uuid]
  }

  fun getByTitle(title: String): Tag? {
    maybeLoadFromDB()
    return tags.values.firstOrNull { it.title == title }
  }

  fun maybeLoadFromDB() {
    if (tags.isNotEmpty()) {
      return
    }
    db().all.forEach {
      tags[it.uuid] = it
    }
  }

  fun clear() {
    tags.clear()
  }

  companion object {

    val db = TagsDB()

    fun db(): TagDao {
      return MaterialNotes.db().tags()
    }
  }
}