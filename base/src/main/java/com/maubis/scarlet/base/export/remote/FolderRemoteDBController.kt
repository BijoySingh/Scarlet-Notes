package com.maubis.scarlet.base.export.remote

import com.github.bijoysingh.starter.util.FileManager
import com.google.gson.Gson
import kotlinx.coroutines.experimental.launch
import java.io.File

class FolderRemote<T>(val folder: File,
                      val klass: Class<T>,
                      val onRemoteInsert: (T) -> Unit,
                      val onRemoteDelete: (String) -> Unit) {

  val deletedFolder = File(folder, "deleted")
  val uuids = HashSet<String>()
  val deletedUuids = HashSet<String>()

  init {
    launch {
      deletedFolder.mkdirs()
      val files = folder.listFiles() ?: emptyArray()
      files.forEach {
        uuids.add(it.name)
        launch {
          try {
            val item = Gson().fromJson(FileManager.readFromFile(it), klass)
            if (item !== null) {
              onRemoteInsert(item)
            }
          } catch (exception: Exception) {
          }
        }
      }

      val deletedFiles = deletedFolder.listFiles() ?: emptyArray()
      deletedFiles.forEach {
        deletedUuids.add(it.name)
        launch { onRemoteDelete(it.name) }
      }
    }
  }

  fun file(uuid: String): File = File(folder, uuid)
  fun deletedFile(uuid: String): File = File(deletedFolder, uuid)

  fun insert(uuid: String, item: T) {
    try {
      val data = Gson().toJson(item)
      val file = file(uuid)
      FileManager.writeToFile(file, data)
    } catch (exception: Exception) {
    }
  }

  fun delete(uuid: String) {
    uuids.remove(uuid)
    deletedUuids.add(uuid)
    file(uuid).delete()
    FileManager.writeToFile(deletedFile(uuid), "${System.currentTimeMillis()}")
  }
}