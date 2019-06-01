package com.maubis.scarlet.base.note

import com.maubis.markdown.segmenter.MarkdownSegmentType
import com.maubis.markdown.segmenter.TextSegmenter
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatType

fun String.toInternalFormats(): List<Format> {
  return toInternalFormats(arrayOf(
      MarkdownSegmentType.HEADING_1,
      MarkdownSegmentType.HEADING_2,
      MarkdownSegmentType.CODE,
      MarkdownSegmentType.QUOTE,
      MarkdownSegmentType.CHECKLIST_UNCHECKED,
      MarkdownSegmentType.CHECKLIST_CHECKED,
      MarkdownSegmentType.SEPARATOR,
      MarkdownSegmentType.IMAGE))
}

/**
 * Converts a string to the internal format types using the Markdown Segmentation Library.
 * It's possible to pass specific formats which will be preserved in the formats
 */
fun String.toInternalFormats(whitelistedSegments: Array<MarkdownSegmentType>): List<Format> {
  val extractedFormats = emptyList<Format>().toMutableList()
  val segments = TextSegmenter(this).get()

  var lastFormat: Format? = null
  segments.forEach { segment ->
    val isSegmentWhitelisted = whitelistedSegments.contains(segment.type())
    val newFormat = when {
      !isSegmentWhitelisted -> null
      segment.type() == MarkdownSegmentType.HEADING_1 -> Format(FormatType.HEADING, segment.strip())
      segment.type() == MarkdownSegmentType.HEADING_2 -> Format(FormatType.SUB_HEADING, segment.strip())
      segment.type() == MarkdownSegmentType.CODE -> Format(FormatType.CODE, segment.strip())
      segment.type() == MarkdownSegmentType.QUOTE -> Format(FormatType.QUOTE, segment.strip())
      segment.type() == MarkdownSegmentType.CHECKLIST_UNCHECKED -> Format(FormatType.CHECKLIST_UNCHECKED, segment.strip())
      segment.type() == MarkdownSegmentType.CHECKLIST_CHECKED -> Format(FormatType.CHECKLIST_CHECKED, segment.strip())
      segment.type() == MarkdownSegmentType.SEPARATOR -> Format(FormatType.SEPARATOR)
      segment.type() == MarkdownSegmentType.IMAGE -> Format(FormatType.IMAGE, segment.strip().trim())
      else -> null
    }

    val tempLastFormat = lastFormat
    when {
      tempLastFormat !== null && newFormat !== null -> {
        extractedFormats.add(tempLastFormat)
        extractedFormats.add(newFormat)
        lastFormat = null
      }
      tempLastFormat === null && newFormat !== null -> {
        extractedFormats.add(newFormat)
      }
      tempLastFormat !== null && newFormat === null -> {
        tempLastFormat.text += "\n"
        tempLastFormat.text += segment.text()
        lastFormat = tempLastFormat
      }
      tempLastFormat == null && newFormat === null -> {
        lastFormat = Format(FormatType.TEXT, segment.text())
      }
    }
  }

  val tempLastFormat = lastFormat
  if (tempLastFormat !== null) {
    extractedFormats.add(tempLastFormat)
  }
  return extractedFormats
}