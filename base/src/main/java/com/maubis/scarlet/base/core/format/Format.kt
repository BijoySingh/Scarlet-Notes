package com.maubis.scarlet.base.core.format

import org.json.JSONObject
import java.util.*

class Format : Comparable<Format> {

  var formatType: FormatType = FormatType.TEXT

  var uid: Int = 0

  var text: String = ""

  var forcedMarkdown = false

  val markdownText: String
    get() {
      return when (formatType) {
        FormatType.BULLET_1, FormatType.BULLET_2, FormatType.BULLET_3, FormatType.NUMBERED_LIST -> "- $text"
        FormatType.HEADING -> "# $text"
        FormatType.CHECKLIST_CHECKED -> "\u2612 $text"
        FormatType.CHECKLIST_UNCHECKED -> "\u2610 $text"
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

  override fun compareTo(other: Format): Int {
    return when {
      other.formatType == FormatType.CHECKLIST_CHECKED && formatType == FormatType.CHECKLIST_UNCHECKED -> -1
      other.formatType == FormatType.CHECKLIST_UNCHECKED && formatType == FormatType.CHECKLIST_CHECKED -> 1
      else -> 0
    }
  }
}