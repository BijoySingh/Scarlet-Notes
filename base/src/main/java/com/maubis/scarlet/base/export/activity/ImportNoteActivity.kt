package com.maubis.scarlet.base.export.activity

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.github.bijoysingh.starter.recyclerview.RecyclerViewBuilder
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.export.recycler.FileRecyclerItem
import com.maubis.scarlet.base.export.support.NoteImporter
import com.maubis.scarlet.base.note.recycler.NoteAppAdapter
import com.maubis.scarlet.base.support.utils.bind
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity
import java.io.File
import java.io.FileReader


class ImportNoteActivity : ThemedActivity() {
  val adapter = NoteAppAdapter(this)

  var currentlySelectedFile: File? = null
  val background: View by bind(R.id.container_layout)
  val backButton: ImageView by bind(R.id.back_button)
  val pageTitle: TextView by bind(R.id.page_title)
  val importFile: TextView by bind(R.id.import_file)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_import_note_from_file)

    RecyclerViewBuilder(this)
        .setView(this, R.id.recycler_view)
        .setAdapter(adapter)
        .build()

    val activity = this
    backButton.setOnClickListener { onBackPressed() }
    importFile.setOnClickListener {
      MultiAsyncTask.execute(object : MultiAsyncTask.Task<Unit> {
        override fun handle(result: Unit?) {
          finish()
        }

        override fun run() {
          if (currentlySelectedFile != null) {
            val fileContent = NoteImporter().readFileInputStream(FileReader(currentlySelectedFile!!))
            if (fileContent.isBlank()) {
              return
            }
            NoteImporter().gen(activity, fileContent)
          }
        }
      })
    }
    notifyThemeChange()
    setSystemTheme()
  }

  override fun onResume() {
    super.onResume()
    MultiAsyncTask.execute(object : MultiAsyncTask.Task<List<RecyclerItem>> {
      override fun run(): List<RecyclerItem> {
        return NoteImporter().getImportableFiles()
            .map { FileRecyclerItem(it.name, it.lastModified(), it.absolutePath, it) }
            .sorted()
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

  override fun notifyThemeChange() {
    val theme = CoreConfig.instance.themeController()
    background.setBackgroundColor(theme.get(ThemeColorType.BACKGROUND))
    backButton.setColorFilter(theme.get(ThemeColorType.TOOLBAR_ICON))
    pageTitle.setTextColor(theme.get(ThemeColorType.TERTIARY_TEXT))
    importFile.setTextColor(theme.get(ThemeColorType.TERTIARY_TEXT))
    importFile.setBackgroundResource(
        if (CoreConfig.instance.themeController().isNightTheme()) R.drawable.light_circular_border_bg
        else R.drawable.dark_circular_border_bg)
  }
}
