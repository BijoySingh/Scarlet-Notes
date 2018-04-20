package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.content.Intent
import android.support.v4.content.FileProvider
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.bijoysingh.quicknote.MaterialNotes.Companion.appTheme
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.export.support.NoteExporter
import com.bijoysingh.quicknote.utils.Flavor
import com.bijoysingh.quicknote.utils.GenericFileProvider
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.bijoysingh.quicknote.utils.getAppFlavor
import com.github.bijoysingh.starter.async.MultiAsyncTask
import java.io.File


class ExportNotesBottomSheet : ThemedBottomSheetFragment() {
  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val exportTitle = dialog.findViewById<TextView>(R.id.export_title)
    val filename = dialog.findViewById<TextView>(R.id.filename)
    val progressBar = dialog.findViewById<ProgressBar>(R.id.progress_bar)
    val resultLayout = dialog.findViewById<View>(R.id.results_layout)
    val exportDone = dialog.findViewById<TextView>(R.id.export_done)
    val exportShare = dialog.findViewById<TextView>(R.id.export_share)

    val activity = themedActivity()
    MultiAsyncTask.execute(activity, object : MultiAsyncTask.Task<Boolean> {
      override fun run(): Boolean {
        val notes = NoteExporter().getExportContent()
        return NoteExporter().saveToManualExportFile(notes)
      }

      override fun handle(result: Boolean) {
        resultLayout.visibility = View.VISIBLE
        progressBar.visibility = View.INVISIBLE
        exportTitle.setText(
            if (result) R.string.import_export_layout_exported
            else R.string.import_export_layout_export_failed)
        exportDone.visibility = if (result) View.VISIBLE else View.GONE
      }
    })
    exportDone.setOnClickListener {
      dismiss()
    }
    exportShare.setOnClickListener {
      val file = NoteExporter().getOrCreateManualExportFile()
      if (file == null || !file.exists()) {
        return@setOnClickListener
      }

      val uri = FileProvider.getUriForFile(activity, GenericFileProvider.PROVIDER, file)

      val intent = Intent(Intent.ACTION_SEND)
      intent.type = "text/plain"
      intent.putExtra(Intent.EXTRA_STREAM, uri)
      startActivity(Intent.createChooser(intent, getString(R.string.share_using)))

      dismiss()
    }

    exportTitle.setTextColor(appTheme().get(ThemeColorType.TERTIARY_TEXT))
    filename.setTextColor(appTheme().get(ThemeColorType.HINT_TEXT))
    filename.text = (MATERIAL_NOTES_FOLDER + File.separator + FILENAME + ".txt")
    exportShare.setTextColor(appTheme().get(ThemeColorType.HINT_TEXT))
    exportDone.setTextColor(appTheme().get(ThemeColorType.ACCENT_TEXT))
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_import_export

  companion object {

    val MATERIAL_NOTES_FOLDER = if (getAppFlavor() === Flavor.NONE) "MaterialNotes" else "Scarlet"
    val FILENAME = "manual_backup"

    fun openSheet(activity: MainActivity) {
      val sheet = ExportNotesBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}