package com.maubis.markdown.segmenter

enum class MarkdownSegmentType {
  INVALID,
  HEADING_1,
  HEADING_2,
  HEADING_3,
  NORMAL,
  IMAGE,
  CODE,
  BULLET_1,
  BULLET_2,
  BULLET_3,
  QUOTE,
  SEPARATOR,
  CHECKLIST_UNCHECKED,
  CHECKLIST_CHECKED,
}