package com.maubis.scarlet.base.note

import android.content.Context
import com.github.bijoysingh.starter.util.RandomHelper
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.format.Format
import com.maubis.scarlet.base.format.FormatBuilder
import com.maubis.scarlet.base.format.FormatType
import java.io.File

class NoteImage(val context: Context) {
  fun renameOrCopy(note: Note, imageFile: File): File {
    val targetFile = getFile(note.uuid, RandomHelper.getRandom() + ".jpg")
    targetFile.mkdirs()
    deleteIfExist(targetFile)

    val renamed = imageFile.renameTo(targetFile)
    if (!renamed) {
      imageFile.copyTo(targetFile, true)
    }
    return targetFile
  }

  fun getFile(noteUUID: String, imageFormat: Format): File {
    if (imageFormat.formatType != FormatType.IMAGE) {
      throw IllegalArgumentException("Format should be an Image")
    }
    return getFile(noteUUID, imageFormat.text)
  }

  fun getFile(noteUUID: String, formatFileName: String): File {
    return File(context.cacheDir, "images" + File.separator + noteUUID + File.separator + formatFileName)
  }

  fun deleteAllFiles(note: Note) {
    for (format in FormatBuilder().getFormats(note.description)) {
      if (format.formatType === FormatType.IMAGE) {
        val file = getFile(note.uuid, format)
        deleteIfExist(file)
      }
    }
  }

  fun deleteAllRedundantImageFiles(uuids: Array<String>) {
    val imagesFolder = File(context.cacheDir, "images" + File.separator)
    val availableDirectories = HashSet<String>()
    for (file in imagesFolder.listFiles()) {
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

  fun deleteIfExist(file: File): Boolean {
    return when {
      file.exists() -> file.delete()
      else -> false
    }
  }
}