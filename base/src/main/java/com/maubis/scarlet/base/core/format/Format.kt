package com.maubis.scarlet.base.core.format

import com.maubis.scarlet.base.note.creation.sheet.sEditorMoveChecked
import org.json.JSONObject
import java.util.*

class Format {

  var formatType: FormatType = FormatType.TEXT

  var uid: Int = 0

  var text: String = ""

  var forcedMarkdown = false

  val markdownText: String
    get() {
      return when (formatType) {
        FormatType.NUMBERED_LIST -> "- $text"
        FormatType.HEADING -> "# $text"
        FormatType.CHECKLIST_CHECKED -> "[x] $text"
        FormatType.CHECKLIST_UNCHECKED -> "[ ] $text"
        FormatType.SUB_HEADING -> "## $text"
        FormatType.CODE -> "```\n$text\n```"
        FormatType.QUOTE -> "> $text"
        FormatType.IMAGE -> ""
        FormatType.SEPARATOR -> "\n---\n"
        FormatType.TEXT -> text
        else -> return text
      }
    }

  constructor() {}

  constructor(formatType: FormatType) {
    this.formatType = formatType

    if (formatType == FormatType.SEPARATOR) {
      text = "n/a"
    }
  }

  constructor(formatType: FormatType, text: String) {
    this.formatType = formatType
    this.text = text

    if (formatType === FormatType.TAG) {
      forcedMarkdown = true
    }
  }

  fun toJson(): JSONObject? {
    if (text.trim { it <= ' ' }.isEmpty()) {
      return null
    }

    val map = HashMap<String, Any>()
    map["format"] = formatType.name
    map["text"] = text
    return JSONObject(map)
  }
}

fun sectionPreservingSort(formats: List<Format>): List<Format> {
  if (!sEditorMoveChecked) {
    return formats
  }

  val mutableFormats = formats.toMutableList()
  var index = 0
  while (index < formats.size - 1) {
    val currentItem = mutableFormats[index]
    val nextItem = mutableFormats[index + 1]

    if (currentItem.formatType == FormatType.CHECKLIST_CHECKED
        && nextItem.formatType == FormatType.CHECKLIST_UNCHECKED) {
      Collections.swap(mutableFormats, index, index + 1)
      continue
    }
    index += 1
  }
  while (index > 0) {
    val currentItem = mutableFormats[index]
    val nextItem = mutableFormats[index - 1]

    if (currentItem.formatType == FormatType.CHECKLIST_UNCHECKED
            && nextItem.formatType == FormatType.CHECKLIST_CHECKED) {
      Collections.swap(mutableFormats, index, index - 1)
      continue
    }
    index -= 1
  }
  return mutableFormats
}