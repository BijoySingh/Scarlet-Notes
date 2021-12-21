package com.maubis.markdown.spannable

import com.maubis.markdown.inliner.MarkdownInlineType
import com.maubis.markdown.segmenter.MarkdownSegmentType

enum class MarkdownType {
  INVALID,
  HEADING_1,
  HEADING_2,
  HEADING_3,
  CODE,
  BULLET_1,
  BULLET_2,
  BULLET_3,
  QUOTE,
  NORMAL,
  BOLD,
  ITALICS,
  UNDERLINE,
  INLINE_CODE,
  STRIKE,
  SEPARATOR,
  CHECKLIST_UNCHECKED,
  CHECKLIST_CHECKED,
  IMAGE,
}

data class SpanResult(val text: String, val spans: List<SpanInfo>)

data class SpanInfo(val markdownType: MarkdownType, val start: Int, val end: Int)

fun map(type: MarkdownSegmentType): MarkdownType {
  return when (type) {
    MarkdownSegmentType.INVALID -> MarkdownType.INVALID
    MarkdownSegmentType.HEADING_1 -> MarkdownType.HEADING_1
    MarkdownSegmentType.HEADING_2 -> MarkdownType.HEADING_2
    MarkdownSegmentType.HEADING_3 -> MarkdownType.HEADING_3
    MarkdownSegmentType.NORMAL -> MarkdownType.NORMAL
    MarkdownSegmentType.CODE -> MarkdownType.CODE
    MarkdownSegmentType.BULLET_1 -> MarkdownType.BULLET_1
    MarkdownSegmentType.BULLET_2 -> MarkdownType.BULLET_2
    MarkdownSegmentType.BULLET_3 -> MarkdownType.BULLET_3
    MarkdownSegmentType.QUOTE -> MarkdownType.QUOTE
    MarkdownSegmentType.SEPARATOR -> MarkdownType.SEPARATOR
    MarkdownSegmentType.CHECKLIST_UNCHECKED -> MarkdownType.CHECKLIST_UNCHECKED
    MarkdownSegmentType.CHECKLIST_CHECKED -> MarkdownType.CHECKLIST_CHECKED
    MarkdownSegmentType.IMAGE -> MarkdownType.IMAGE
  }
}

fun map(type: MarkdownInlineType): MarkdownType {
  return when (type) {
    MarkdownInlineType.INVALID -> MarkdownType.INVALID
    MarkdownInlineType.NORMAL -> MarkdownType.NORMAL
    MarkdownInlineType.BOLD -> MarkdownType.BOLD
    MarkdownInlineType.ITALICS -> MarkdownType.ITALICS
    MarkdownInlineType.UNDERLINE -> MarkdownType.UNDERLINE
    MarkdownInlineType.INLINE_CODE -> MarkdownType.INLINE_CODE
    MarkdownInlineType.STRIKE -> MarkdownType.STRIKE
    MarkdownInlineType.IGNORE_CHAR -> MarkdownType.NORMAL
  }
}