package com.bijoysingh.quicknote.activities.external

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.View.GONE
import android.widget.ProgressBar
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.database.utils.save
import com.bijoysingh.quicknote.items.FileRecyclerItem
import com.bijoysingh.quicknote.items.RecyclerItem
import com.bijoysingh.quicknote.recyclerview.NoteAppAdapter
import com.bijoysingh.quicknote.utils.genEmptyNote
import com.bijoysingh.quicknote.utils.genImportedNote
import com.bijoysingh.quicknote.utils.genImportedTag
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.github.bijoysingh.starter.async.Parallel
import com.github.bijoysingh.starter.json.SafeJson
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder
import com.google.gson.Gson
import org.json.JSONArray
import java.io.*


class ImportNoteFromFileActivity : AppCompatActivity() {

  val adapter = NoteAppAdapter(this)
  var currentlySelectedFile: File? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_import_note_from_file)

    RecyclerViewBuilder(this)
        .setView(this, R.id.recycler_view)
        .setAdapter(adapter)
        .build()

    val activity = this
    findViewById<View>(R.id.import_file).setOnClickListener {
      MultiAsyncTask.execute(activity, object: MultiAsyncTask.Task<Unit> {
        override fun handle(result: Unit?) {
          if (currentlySelectedFile != null) {
            val fileContent = getStringFromFile(currentlySelectedFile!!.absolutePath)
            if (fileContent.isBlank()) {
              return
            }

            try {
              val json = SafeJson(fileContent)
              val keyVersion = json.getInt(KEY_NOTE_VERSION, -1)
              if (keyVersion == -1) {
                // New form code
                val fileFormat = Gson().fromJson<ExportableFileFormat>(fileContent, ExportableFileFormat::class.java)
                if (fileFormat === null) {
                  genEmptyNote("", fileContent).save(activity)
                  return
                }
                fileFormat.tags.forEach {
                  genImportedTag(it)
                }
                fileFormat.notes.forEach {
                  genImportedNote(activity, it)
                }
                return
              }

              val notes = json[ExportableNote.KEY_NOTES] as JSONArray
              for (index in 0 until notes.length()) {
                val exportableNote = when(keyVersion) {
                  2 -> ExportableNote.fromJSONObjectV2(notes.getJSONObject(index))
                  3 -> ExportableNote.fromJSONObjectV3(notes.getJSONObject(index))
                  4 -> ExportableNote.fromJSONObjectV4(notes.getJSONObject(index))
                  else -> null
                }
                if (exportableNote !== null) {
                  genImportedNote(activity, exportableNote)
                }
              }
            } catch (exception: Exception) {
              genEmptyNote("", fileContent).save(activity)
            }
          }
        }

        override fun run() {
          finish()
        }
      })
    }
  }

  override fun onResume() {
    super.onResume()
    MultiAsyncTask.execute(this, object : MultiAsyncTask.Task<List<RecyclerItem>> {
      override fun run(): List<RecyclerItem> {
        val files = getFiles(Environment.getExternalStorageDirectory())
        val items = ArrayList<RecyclerItem>()
        for (file in files) {
          items.add(FileRecyclerItem(
              file.name,
              file.lastModified(),
              file.absolutePath,
              file))
        }
        return items
      }

      override fun handle(result: List<RecyclerItem>) {
        findViewById<ProgressBar>(R.id.progress_bar).visibility = GONE
        adapter.items = result
      }
    })
  }

  fun select(selectedFile: FileRecyclerItem) {
    val files = ArrayList<RecyclerItem>()
    val selectedPath = currentlySelectedFile?.absolutePath
    var newlySelected = selectedFile.path != selectedPath
    currentlySelectedFile = null
    for (item in adapter.items) {
      if (item is FileRecyclerItem) {
        val isThisNewlySelected = selectedFile.path == item.path && newlySelected
        item.selected = isThisNewlySelected
        if (isThisNewlySelected) currentlySelectedFile = item.file
      }
      files.add(item)
    }
    adapter.items = files
  }

  fun getFiles(directory: File): List<File> {
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
    val validExtension = "txt"
    return filePath.endsWith("." + validExtension)
        || filePath.endsWith("." + validExtension.toUpperCase())
  }

  private fun getStringFromFile(filePath: String): String {
    try {
      val fl = File(filePath)
      val fin = FileInputStream(fl)
      val ret = convertStreamToString(fin)
      fin.close()
      return ret
    } catch (exception: Exception) {
      return ""
    }
  }

  companion object {
    @Throws(Exception::class)
    fun convertStreamToString(inputStream: InputStream): String {
      val reader = BufferedReader(InputStreamReader(inputStream))
      val sb = StringBuilder()
      var line: String? = reader.readLine()
      while (line != null) {
        sb.append(line).append("\n")
        line = reader.readLine()
      }
      reader.close()
      return sb.toString()
    }
  }
}
