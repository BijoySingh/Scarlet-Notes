package com.bijoysingh.quicknote.recyclerview

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.external.ImportNoteFromFileActivity
import com.bijoysingh.quicknote.items.FileRecyclerItem
import com.bijoysingh.quicknote.items.RecyclerItem
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.github.bijoysingh.starter.util.DateFormatter
import com.github.bijoysingh.starter.util.LocaleManager
import java.io.File

class FileImportViewHolder(context: Context, root: View)
  : RecyclerViewHolder<RecyclerItem>(context, root) {

  private val fileName: TextView = findViewById<TextView>(R.id.file_name)
  private val filePath: TextView = findViewById<TextView>(R.id.file_path)
  private val fileDate: TextView = findViewById<TextView>(R.id.file_date)
  private val fileSize: TextView = findViewById<TextView>(R.id.file_size)

  override fun populate(data: RecyclerItem, extra: Bundle?) {
    val item = data as FileRecyclerItem
    fileName.text = item.name
    filePath.text = getPath(item)
    fileDate.text = getSubtitleText(item.file)
    fileSize.text = getMetaText(item.file)

    root.setOnClickListener {
      (context as ImportNoteFromFileActivity).select(item)
    }

    root.setBackgroundColor(ContextCompat.getColor(
        context,
        if (item.selected) R.color.material_grey_100 else R.color.transparent))
  }

  private fun getPath(item: FileRecyclerItem): String {
    var path = item.path.removePrefix(Environment.getExternalStorageDirectory().absolutePath)
    path = path.removeSuffix(item.name)
    return path
  }

  private fun getSubtitleText(file: File): String {
    return DateFormatter.getDate("dd MMM yy \u00B7 hh:mm a", file.lastModified())
  }

  private fun getMetaText(file: File): String {
    var stringResource = R.string.file_size_kb
    var fileSize = file.length() / 1024.0
    if (fileSize > 1024) {
      fileSize = fileSize / 1024.0
      stringResource = R.string.file_size_mb
    }
    if (fileSize > 1024) {
      fileSize = fileSize / 1024.0
      stringResource = R.string.file_size_gb
    }
    return context.getString(stringResource, LocaleManager.toString(fileSize, 2))
  }
}