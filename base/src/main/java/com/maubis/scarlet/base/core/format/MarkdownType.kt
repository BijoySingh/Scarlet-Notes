package com.maubis.scarlet.base.core.format

enum class MarkdownType(val startToken: String, val endToken: String = "", val requiresNewLine: Boolean = false) {
  BOLD(startToken = "**", endToken = "**"),
  UNDERLINE(startToken = "_", endToken = "_"),
  ITALICS(startToken = "*", endToken = "*"),
  HEADER(startToken = "# ", requiresNewLine = true),
  SUB_HEADER(startToken = "## ", requiresNewLine = true),
  UNORDERED(startToken = "- ", requiresNewLine = true),
  CHECKLIST_UNCHECKED(startToken = "[ ] ", requiresNewLine = true),
  CODE(startToken = "`", endToken = "`"),
  CODE_BLOCK(startToken = "```\n", endToken = "\n```"),
  STRIKE_THROUGH(startToken = "~~", endToken = "~~"),
}