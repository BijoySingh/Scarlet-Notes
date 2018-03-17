package com.bijoysingh.quicknote.database.utils

import com.bijoysingh.quicknote.MaterialNotes
import com.bijoysingh.quicknote.database.Tag
import com.bijoysingh.quicknote.database.TagDao
import java.util.concurrent.ConcurrentHashMap

class TagsDB {

  val tags = ConcurrentHashMap<String, Tag>()

  fun notifyInsertTag(tag: Tag) {
    maybeLoadFromDB()
    tags[tag.uuid] = tag
  }

  fun notifyDelete(tag: Tag) {
    maybeLoadFromDB()
    tags.remove(tag.uuid)
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

  @Synchronized
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