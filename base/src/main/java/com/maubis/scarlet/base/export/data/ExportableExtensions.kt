package com.maubis.scarlet.base.export.data

import com.maubis.markdown.segmenter.MarkdownSegmentType
import com.maubis.markdown.segmenter.TextSegmenter
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatBuilder
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.core.note.getFormats
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.database.room.note.Note
import com.maubis.scarlet.base.database.room.tag.Tag
import com.maubis.scarlet.base.note.toInternalFormats

/**
 * Converts the note which is markdown into internal format
 */
fun fromExportedMarkdown(content: String): String {
  val formats = content.toInternalFormats()
  return FormatBuilder().getDescription(formats)
}

/**
 * Converts the note's internal description format into markdown which can be used to export.
 */
fun Note.toExportedMarkdown(): String {
  val markdownBuilder = StringBuilder()
  getFormats().forEach { format ->
    val text = format.text
    val formatMarkdown = when (format.formatType) {
      FormatType.NUMBERED_LIST -> "- $text"
      FormatType.HEADING -> "# $text"
      FormatType.CHECKLIST_CHECKED -> "[x] $text"
      FormatType.CHECKLIST_UNCHECKED -> "[ ] $text"
      FormatType.SUB_HEADING -> "## $text"
      FormatType.CODE -> "```\n$text\n```"
      FormatType.QUOTE -> "> $text"
      // TODO: Fix the fact that markdown parsing wont parse this correctly
      FormatType.IMAGE -> "<scarlet::image>$text</scarlet::image>"
      FormatType.SEPARATOR -> "\n---\n"
      FormatType.TEXT -> text

      // NOTE: All the following states should never happen at this place
      FormatType.HEADING_3 -> text
      FormatType.TAG -> ""
      FormatType.EMPTY -> ""
    }
    markdownBuilder.append(formatMarkdown)
    markdownBuilder.append("\n")
  }
  return markdownBuilder.toString().trim()
}

fun Note.getExportableSplitNote(): ExportableSplitNote {
  return ExportableSplitNote(
      toExportedMarkdown(),
      getExportableNoteMeta())
}

fun Note.getExportableNoteMeta(): ExportableNoteMeta {
  return ExportableNoteMeta(
      uuid,
      timestamp,
      updateTimestamp,
      color,
      state,
      if (tags == null) "" else tags,
      locked,
      pinned,
      folder
  )
}

fun Note.mergeMetas(meta: ExportableNoteMeta) {
  uuid = meta.uuid
  state = meta.state
  timestamp = meta.timestamp
  updateTimestamp = meta.updateTimestamp
  color = meta.color
  tags = meta.tags
  pinned = meta.pinned
  locked = meta.locked
  folder = meta.folder
}


fun Folder.getExportableFolder(): ExportableFolder {
  return ExportableFolder(
      uuid,
      title,
      timestamp,
      updateTimestamp,
      color
  )
}

fun Tag.getExportableTag(): ExportableTag = ExportableTag(uuid, title)