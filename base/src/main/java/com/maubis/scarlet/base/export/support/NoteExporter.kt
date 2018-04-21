package com.maubis.scarlet.base.export.support

import android.os.AsyncTask
import android.os.Environment
import com.github.bijoysingh.starter.util.FileManager
import com.google.gson.Gson
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.export.data.ExportableFileFormat
import com.maubis.scarlet.base.export.data.ExportableNote
import com.maubis.scarlet.base.export.data.ExportableTag
import com.maubis.scarlet.base.export.sheet.ExportNotesBottomSheet
import com.maubis.scarlet.base.support.database.notesDB
import com.maubis.scarlet.base.support.database.tagsDB
import java.io.File

const val KEY_NOTE_VERSION = "KEY_NOTE_VERSION"
const val KEY_AUTO_BACKUP_MODE = "KEY_AUTO_BACKUP_MODE"
const val KEY_AUTO_BACKUP_LAST_TIMESTAMP = "KEY_AUTO_BACKUP_LAST_TIMESTAMP"

const val EXPORT_VERSION = 5

const val AUTO_BACKUP_FILENAME = "auto_backup"
const val AUTO_BACKUP_INTERVAL_MS = 1000 * 60 * 60 * 6 // 6 hours update

class NoteExporter() {
  fun getExportContent(): String {
    val notes = notesDB.getAll().map { ExportableNote(it) }
    val tags = tagsDB.getAll().map { ExportableTag(it) }
    val fileContent = ExportableFileFormat(EXPORT_VERSION, notes, tags)
    return Gson().toJson(fileContent)
  }

  fun tryAutoExport() {
    AsyncTask.execute {
      val autoBackup = CoreConfig.instance.store().get(KEY_AUTO_BACKUP_MODE, false)
      if (!autoBackup) {
        return@execute
      }
      val lastBackup = CoreConfig.instance.store().get(KEY_AUTO_BACKUP_LAST_TIMESTAMP, 0L)
      val lastTimestamp = notesDB.getLastTimestamp()
      if (lastBackup + AUTO_BACKUP_INTERVAL_MS >= lastTimestamp) {
        return@execute
      }

      val exportFile = getOrCreateFileForExport(AUTO_BACKUP_FILENAME)
      if (exportFile === null) {
        return@execute
      }
      saveToFile(exportFile, getExportContent())
      CoreConfig.instance.store().put(KEY_AUTO_BACKUP_LAST_TIMESTAMP, System.currentTimeMillis())
    }
  }

  fun getOrCreateManualExportFile(): File? {
    return getOrCreateFileForExport(ExportNotesBottomSheet.FILENAME)
  }

  fun getOrCreateFileForExport(filename: String): File? {
    val folder = createFolder()
    if (folder === null) {
      return null
    }
    return File(folder, filename + ".txt")
  }

  fun saveToManualExportFile(text: String): Boolean {
    val file = getOrCreateManualExportFile()
    if (file === null) {
      return false
    }
    return saveToFile(file, text)
  }

  fun saveToFile(file: File, text: String): Boolean {
    return FileManager.writeToFile(file, text)
  }

  fun createFolder(): File? {
    val folder = File(Environment.getExternalStorageDirectory(), ExportNotesBottomSheet.MATERIAL_NOTES_FOLDER)
    if (!folder.exists() && !folder.mkdirs()) {
      return null
    }
    return folder
  }
}