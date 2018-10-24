package com.maubis.scarlet.base.support.database

import android.content.Context
import com.github.bijoysingh.starter.async.SimpleThreadExecutor
import com.maubis.scarlet.base.core.note.NoteImage.Companion.deleteIfExist
import com.maubis.scarlet.base.note.delete
import com.maubis.scarlet.base.note.save
import java.io.File
import java.util.*

class HouseKeeper(val context: Context) {

  private val houseKeeperTasks: Array<() -> Unit> = arrayOf(
      { removeOlderClips() },
      { removeDecoupledFolders() },
      { deleteRedundantImageFiles() }
  )

  fun start() {
    SimpleThreadExecutor.execute {
      for (task in houseKeeperTasks) {
        task()
      }
    }
  }

  private fun removeOlderReminders() {

  }

  private fun removeOlderClips() {
    val notes = notesDB.database().getOldTrashedNotes(
        Calendar.getInstance().timeInMillis - 1000 * 60 * 60 * 24 * 7)
    for (note in notes) {
      note.delete(context)
    }
  }

  private fun removeDecoupledFolders() {
    val folders = foldersDB.getAll().map { it.uuid }
    notesDB.getAll()
        .filter { it.folder.isNotBlank() }
        .forEach {
          if (!folders.contains(it.folder)) {
            it.folder = ""
            it.save(context)
          }
        }
  }

  private fun deleteRedundantImageFiles() {
    val uuids = notesDB.getAllUUIDs()

    val imagesFolder = File(context.cacheDir, "images" + File.separator)
    val uuidFiles = imagesFolder.listFiles()
    if (uuidFiles === null || uuidFiles.isEmpty()) {
      return
    }

    val availableDirectories = HashSet<String>()
    for (file in uuidFiles) {
      if (file.isDirectory) {
        availableDirectories.add(file.name)
      }
    }
    for (id in uuids) {
      availableDirectories.remove(id)
    }
    for (uuid in availableDirectories) {
      val noteFolder = File(imagesFolder, uuid)
      for (file in noteFolder.listFiles()) {
        deleteIfExist(file)
      }
    }
  }


}