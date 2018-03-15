package com.bijoysingh.quicknote.activities.external

import android.Manifest
import android.os.AsyncTask
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import com.bijoysingh.quicknote.MaterialNotes.Companion.userPreferences
import com.bijoysingh.quicknote.activities.sheets.ExportNotesBottomSheet
import com.bijoysingh.quicknote.database.Note
import com.bijoysingh.quicknote.database.Tag
import com.bijoysingh.quicknote.database.utils.getFullText
import com.github.bijoysingh.starter.util.FileManager
import com.github.bijoysingh.starter.util.PermissionManager
import com.google.gson.Gson
import java.io.File

const val KEY_NOTE_VERSION = "KEY_NOTE_VERSION"
const val KEY_AUTO_BACKUP_MODE = "KEY_AUTO_BACKUP_MODE"
const val KEY_AUTO_BACKUP_LAST_TIMESTAMP = "KEY_AUTO_BACKUP_LAST_TIMESTAMP"
const val EXPORT_VERSION = 5
const val AUTO_BACKUP_FILENAME = "auto_backup"

class ExportableFileFormat(
    val version: Int,
    val notes: List<ExportableNote>,
    val tags: List<ExportableTag>)

fun getNotesForExport(): String {
  val notes = Note.db().all.map { ExportableNote(it) }
  val tags = Tag.db().all.map { ExportableTag(it) }
  val fileContent = ExportableFileFormat(EXPORT_VERSION, notes, tags)
  return Gson().toJson(fileContent)
}

fun maybeAutoExport() {
  AsyncTask.execute {
    val autoBackup = userPreferences().get(KEY_AUTO_BACKUP_MODE, false)
    if (!autoBackup) {
      return@execute
    }
    val lastBackup = userPreferences().get(KEY_AUTO_BACKUP_LAST_TIMESTAMP, 0L)
    val lastTimestamp = Note.db().getLastTimestamp()
    if (lastBackup >= lastTimestamp) {
      return@execute
    }

    val exportFile = getExportFile(AUTO_BACKUP_FILENAME)
    if (exportFile === null) {
      return@execute
    }
    saveFile(exportFile, getNotesForExport())
  }
}

fun searchInNote(note: Note, keyword: String): Boolean {
  return note.getFullText().contains(keyword, true)
}

fun getStoragePermissionManager(activity: AppCompatActivity): PermissionManager {
  val manager = PermissionManager(activity)
  manager.setPermissions(arrayOf(
      Manifest.permission.WRITE_EXTERNAL_STORAGE,
      Manifest.permission.READ_EXTERNAL_STORAGE))
  return manager
}

fun saveFile(text: String): Boolean {
  val file = getExportFile()
  if (file === null) {
    return false
  }
  return saveFile(file, text)
}

fun saveFile(file: File, text: String): Boolean {
  return FileManager.writeToFile(file, text)
}

fun getExportFile(): File? {
  return getExportFile(ExportNotesBottomSheet.FILENAME)
}

fun getExportFile(filename: String): File? {
  val folder = createFolder()
  if (folder === null) {
    return null
  }

  return File(folder, filename + ".txt")
}

fun createFolder(): File? {
  val folder = File(Environment.getExternalStorageDirectory(), ExportNotesBottomSheet.MATERIAL_NOTES_FOLDER)
  if (!folder.exists() && !folder.mkdirs()) {
    return null
  }

  return folder
}
