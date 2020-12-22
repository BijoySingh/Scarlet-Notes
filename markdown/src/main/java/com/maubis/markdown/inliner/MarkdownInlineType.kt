package com.maubis.markdown.inliner

enum class MarkdownInlineType {
  INVALID,
  NORMAL,
  BOLD,
  ITALICS,
  UNDERLINE,
  INLINE_CODE,
  STRIKE,
  IGNORE_CHAR,
}