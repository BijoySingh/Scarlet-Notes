package com.maubis.scarlet.base.export.support

import android.content.Context
import android.os.Environment
import com.github.bijoysingh.starter.async.Parallel
import com.github.bijoysingh.starter.json.SafeJson
import com.google.gson.Gson
import com.maubis.scarlet.base.core.folder.FolderBuilder
import com.maubis.scarlet.base.core.note.NoteBuilder
import com.maubis.scarlet.base.core.tag.TagBuilder
import com.maubis.scarlet.base.export.data.ExportableFileFormat
import com.maubis.scarlet.base.export.data.ExportableNote
import com.maubis.scarlet.base.note.folder.saveIfUnique
import com.maubis.scarlet.base.note.save
import com.maubis.scarlet.base.note.tag.saveIfUnique
import org.json.JSONArray
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class NoteImporter() {

  fun gen(context: Context, content: String) {
    try {
      val json = SafeJson(content)
      val keyVersion = json.getInt(KEY_NOTE_VERSION, -1)
      if (keyVersion == -1) {
        // New form code
        val fileFormat = Gson().fromJson<ExportableFileFormat>(content, ExportableFileFormat::class.java)
        if (fileFormat === null) {
          importNoteFallback(content, context)
          return
        }
        fileFormat.tags.forEach {
          val tag = TagBuilder().copy(it)
          tag.saveIfUnique()
        }
        fileFormat.notes.forEach {
          it.saveIfNeeded(context)
        }
        fileFormat.folders?.forEach {
          val folder = FolderBuilder().copy(it)
          folder.saveIfUnique()
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
      importNoteFallback(content, context)
    }
  }

  private fun importNoteFallback(content: String, context: Context) {
    content
        .split(EXPORT_NOTE_SEPARATOR)
        .map {
          it.trim()
        }
        .filter { it.isNotBlank() }
        .forEach {
          NoteBuilder().gen("", it).save(context)
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


  fun readFileInputStream(inputStreamReader: InputStreamReader): String {
    lateinit var reader: BufferedReader
    try {
      reader = BufferedReader(inputStreamReader)
      val fileContents = StringBuilder()
      var line: String? = reader.readLine()
      while (line != null) {
        fileContents.append(line + "\n")
        line = reader.readLine()
      }
      return fileContents.toString()
    } catch (exception: IOException) {
      reader.close()
      return ""
    }
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