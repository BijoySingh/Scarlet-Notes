package com.maubis.scarlet.base.database

import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.database.room.folder.FolderDao
import java.util.concurrent.ConcurrentHashMap

class FoldersProvider {

  val folders = ConcurrentHashMap<String, Folder>()

  fun notifyInsertFolder(folder: Folder) {
    maybeLoadFromDB()
    folders[folder.uuid] = folder
  }

  fun notifyDelete(folder: Folder) {
    maybeLoadFromDB()
    folders.remove(folder.uuid)
  }

  fun getCount(): Int {
    maybeLoadFromDB()
    return folders.size
  }

  fun getUUIDs(): List<String> {
    maybeLoadFromDB()
    return folders.values.map { it.uuid }
  }

  fun getAll(): List<Folder> {
    maybeLoadFromDB()
    return folders.values.toList()
  }

  fun getByID(uid: Int): Folder? {
    maybeLoadFromDB()
    return folders.values.firstOrNull { it.uid == uid }
  }

  fun getByUUID(uuid: String): Folder? {
    maybeLoadFromDB()
    return folders[uuid]
  }

  fun getByTitle(title: String): Folder? {
    maybeLoadFromDB()
    return folders.values.firstOrNull { it.title == title }
  }

  fun search(string: String): List<Folder> {
    maybeLoadFromDB()
    return folders.values
        .filter { string.isBlank() || it.title.contains(string, true) }
  }

  @Synchronized
  fun maybeLoadFromDB() {
    if (folders.isNotEmpty()) {
      return
    }
    database().all.forEach {
      folders[it.uuid] = it
    }
  }

  fun clear() {
    folders.clear()
  }

  fun database(): FolderDao {
    return ApplicationBase.instance.database().folders()
  }
}