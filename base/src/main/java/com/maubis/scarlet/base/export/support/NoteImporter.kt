package com.maubis.scarlet.base.export.support

import android.content.Context
import android.os.Environment
import com.github.bijoysingh.starter.async.Parallel
import com.github.bijoysingh.starter.json.SafeJson
import com.google.gson.Gson
import com.maubis.scarlet.base.core.note.NoteBuilder
import com.maubis.scarlet.base.core.tag.TagBuilder
import com.maubis.scarlet.base.export.data.ExportableFileFormat
import com.maubis.scarlet.base.export.data.ExportableNote
import com.maubis.scarlet.base.note.save
import com.maubis.scarlet.base.note.tag.saveIfUnique
import org.json.JSONArray
import java.io.File

class NoteImporter() {

  fun gen(context: Context, content: String) {
    try {
      val json = SafeJson(content)
      val keyVersion = json.getInt(KEY_NOTE_VERSION, -1)
      if (keyVersion == -1) {
        // New form code
        val fileFormat = Gson().fromJson<ExportableFileFormat>(content, ExportableFileFormat::class.java)
        if (fileFormat === null) {
          NoteBuilder().gen("", content).save(context)
          return
        }
        fileFormat.tags.forEach {
          val tag = TagBuilder().copy(it)
          tag.saveIfUnique()
        }
        fileFormat.notes.forEach {
          it.saveIfNeeded(context)
        }
        return
      }

      val notes = json[ExportableNote.KEY_NOTES] as JSONArray
      for (index in 0 until notes.length()) {
        val exportableNote = when (keyVersion) {
          2 -> ExportableNote.fromJSONObjectV2(notes.getJSONObject(index))
          3 -> ExportableNote.fromJSONObjectV3(notes.getJSONObject(index))
          4 -> ExportableNote.fromJSONObjectV4(notes.getJSONObject(index))
          else -> null
        }
        exportableNote?.saveIfNeeded(context)
      }
    } catch (exception: Exception) {
      NoteBuilder().gen("", content).save(context)
    }
  }

  fun getImportableFiles(): List<File> {
    return getFiles(Environment.getExternalStorageDirectory())
  }

  private fun getFiles(directory: File): List<File> {
    val files = ArrayList<File>()
    val folders = ArrayList<File>()
    val allFiles = directory.listFiles()
    if (allFiles != null) {
      for (file in allFiles) {
        if (file.isDirectory()) {
          folders.add(file)
        } else if (isValidFile(file.getPath())) {
          files.add(file)
        }
      }
    }

    val parallel = Parallel<File, List<File>>()
    parallel.setListener { input -> getFiles(input) }

    try {
      val childFiles = parallel.For(folders)
      for (childFile in childFiles) {
        files.addAll(childFile)
      }
    } catch (exception: Exception) {
      // Failed
    }

    return files
  }

  private fun isValidFile(filePath: String): Boolean {
    val validExtensions = arrayOf("txt", "md")
    return validExtensions.firstOrNull { isValidFile(filePath, it) } !== null
  }

  private fun isValidFile(filePath: String, validExtension: String): Boolean {
    return filePath.endsWith("." + validExtension)
        || filePath.endsWith("." + validExtension.toUpperCase())
  }

}