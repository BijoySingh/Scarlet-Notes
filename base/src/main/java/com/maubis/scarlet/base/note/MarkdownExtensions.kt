package com.maubis.scarlet.base.note

import com.maubis.markdown.segmenter.MarkdownSegment
import com.maubis.markdown.segmenter.MarkdownSegmentType
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatType

fun MarkdownSegment.toFormat(): Format {
  return Format(type().toFormatType(), strip())
}

fun MarkdownSegment.toRawFormat(): Format {
  return Format(type().toFormatType(), text())
}

fun MarkdownSegmentType.toFormatType(): FormatType {
  return when (this) {
    MarkdownSegmentType.INVALID -> FormatType.EMPTY
    MarkdownSegmentType.HEADING_1 -> FormatType.HEADING
    MarkdownSegmentType.HEADING_2 -> FormatType.SUB_HEADING
    MarkdownSegmentType.HEADING_3 -> FormatType.HEADING_3
    MarkdownSegmentType.NORMAL -> FormatType.TEXT
    MarkdownSegmentType.CODE -> FormatType.CODE
    MarkdownSegmentType.BULLET_1 -> FormatType.TEXT
    MarkdownSegmentType.BULLET_2 -> FormatType.TEXT
    MarkdownSegmentType.BULLET_3 -> FormatType.TEXT
    MarkdownSegmentType.QUOTE -> FormatType.QUOTE
    MarkdownSegmentType.SEPARATOR -> FormatType.SEPARATOR
    MarkdownSegmentType.CHECKLIST_UNCHECKED -> FormatType.CHECKLIST_UNCHECKED
    MarkdownSegmentType.CHECKLIST_CHECKED -> FormatType.CHECKLIST_CHECKED
  }
}