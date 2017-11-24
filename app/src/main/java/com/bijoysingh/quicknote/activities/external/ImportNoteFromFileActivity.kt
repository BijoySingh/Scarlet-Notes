package com.bijoysingh.quicknote.activities.external

import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.items.FileRecyclerItem
import com.bijoysingh.quicknote.items.RecyclerItem
import com.bijoysingh.quicknote.recyclerview.NoteAppAdapter
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.github.bijoysingh.starter.async.Parallel
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder
import java.io.File


class ImportNoteFromFileActivity : AppCompatActivity() {

  val adapter = NoteAppAdapter(this)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_import_note_from_file)

    RecyclerViewBuilder(this)
        .setView(this, R.id.recycler_view)
        .setAdapter(adapter)
        .build()
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
        adapter.items = result
      }
    })
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
}
