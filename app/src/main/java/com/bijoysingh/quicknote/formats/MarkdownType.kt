package com.bijoysingh.quicknote.formats

enum class MarkdownType(val startToken: String, val endToken: String = "", val requiresNewLine: Boolean = false) {
  BOLD(startToken = "**", endToken = "**"),
  UNDERLINE(startToken = "<u>", endToken = "</u>"),
  ITALICS(startToken = "<i>", endToken = "</i>"),
  HEADER(startToken = "### ", requiresNewLine = true),
  UNORDERED(startToken = "- ", requiresNewLine = true),
  CODE(startToken = "`", endToken = "`"),
  STRIKE_THROUGH(startToken = "~~", endToken = "~~"),
}