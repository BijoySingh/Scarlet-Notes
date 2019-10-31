package com.maubis.scarlet.base.export.recycler

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.TextView
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.github.bijoysingh.starter.util.LocaleManager
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.export.activity.ImportNoteActivity
import com.maubis.scarlet.base.support.recycler.RecyclerItem
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.utils.sDateFormat
import java.io.File

class FileImportViewHolder(context: Context, root: View)
  : RecyclerViewHolder<RecyclerItem>(context, root) {

  private val fileName: TextView = findViewById(R.id.file_name)
  private val filePath: TextView = findViewById(R.id.file_path)
  private val fileDate: TextView = findViewById(R.id.file_date)
  private val fileSize: TextView = findViewById(R.id.file_size)

  init {
    fileName.setTextColor(sAppTheme.get(ThemeColorType.SECONDARY_TEXT))
    filePath.setTextColor(sAppTheme.get(ThemeColorType.HINT_TEXT))
    fileDate.setTextColor(sAppTheme.get(ThemeColorType.TERTIARY_TEXT))
    fileSize.setTextColor(sAppTheme.get(ThemeColorType.TERTIARY_TEXT))
  }

  override fun populate(data: RecyclerItem, extra: Bundle?) {
    val item = data as FileRecyclerItem
    fileName.text = item.name
    filePath.text = getPath(item)
    fileDate.text = getSubtitleText(item.file)
    fileSize.text = getMetaText(item.file)

    root.setOnClickListener {
      (context as ImportNoteActivity).select(item)
    }
    root.setBackgroundColor(
      if (item.selected) sAppTheme.get(
        context, R.color.material_grey_100, R.color.dark_hint_text) else Color.TRANSPARENT)
  }

  private fun getPath(item: FileRecyclerItem): String {
    var path = item.path.removePrefix(Environment.getExternalStorageDirectory().absolutePath)
    path = path.removeSuffix(item.name)
    return path
  }

  private fun getSubtitleText(file: File): String {
    return sDateFormat.readableFullTime(file.lastModified())
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