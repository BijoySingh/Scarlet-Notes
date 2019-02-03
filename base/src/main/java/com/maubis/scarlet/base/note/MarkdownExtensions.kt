package com.maubis.scarlet.base.note

import com.maubis.markdown.segmenter.MarkdownSegment
import com.maubis.markdown.segmenter.MarkdownSegmentType
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatType

fun MarkdownSegment.toFormat(): Format {
  return Format(type().toFormatType(), strip())
}

fun MarkdownSegmentType.toFormatType(): FormatType {
  return when (this) {
    MarkdownSegmentType.INVALID -> FormatType.EMPTY
    MarkdownSegmentType.HEADING_1 -> FormatType.HEADING
    MarkdownSegmentType.HEADING_2 -> FormatType.SUB_HEADING
    MarkdownSegmentType.HEADING_3 -> FormatType.SUB_HEADING
    MarkdownSegmentType.NORMAL -> FormatType.TEXT
    MarkdownSegmentType.CODE -> FormatType.CODE
    MarkdownSegmentType.BULLET_1 -> FormatType.BULLET_1
    MarkdownSegmentType.BULLET_2 -> FormatType.BULLET_2
    MarkdownSegmentType.BULLET_3 -> FormatType.BULLET_3
    MarkdownSegmentType.QUOTE -> FormatType.QUOTE
    MarkdownSegmentType.SEPARATOR -> FormatType.SEPARATOR
  }
}