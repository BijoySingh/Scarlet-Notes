package com.maubis.scarlet.base.database

import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.database.room.tag.Tag
import com.maubis.scarlet.base.database.room.tag.TagDao
import java.util.concurrent.ConcurrentHashMap

class TagsProvider {

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

  fun getUUIDs(): List<String> {
    maybeLoadFromDB()
    return tags.values.map { it.uuid }
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

  fun search(string: String): List<Tag> {
    maybeLoadFromDB()
    return tags.values
        .filter { string.isBlank() || it.title.contains(string, true) }
  }

  @Synchronized
  fun maybeLoadFromDB() {
    if (tags.isNotEmpty()) {
      return
    }
    database().all.forEach {
      tags[it.uuid] = it
    }
  }

  fun clear() {
    tags.clear()
  }

  fun database(): TagDao {
    return ApplicationBase.instance.database().tags()
  }
}