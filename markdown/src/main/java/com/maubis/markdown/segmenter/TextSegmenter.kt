package com.maubis.markdown.segmenter

import android.util.Log

class TextSegmenter(val text: String) {

  object Delimiters {
    const val HEADING_1 = "# "
    const val HEADING_2 = "## "
    const val HEADING_3 = "### "
    const val CODE = "```"
    const val BULLET = "- "
    const val QUOTE = "> "
    const val SEPARATOR = "---"
  }

  private var currentSegment = MarkdownSegmentBuilder()
  private val processedSegments = ArrayList<MarkdownSegment>()

  fun get(): List<MarkdownSegment> {
    processSegments()
    return processedSegments
  }

  fun processSegments() {
    processedSegments.clear()

    val segments = text.split("\n")
    for (segment in segments) {
      if (segment.trim() == Delimiters.CODE) {
        if (currentSegment.markdownType == MarkdownSegmentType.CODE) {
          currentSegment.builder.append("\n")
          currentSegment.builder.append(segment)
          maybeAddCurrentSegment()
          continue
        }

        maybeAddCurrentSegment()
        currentSegment.markdownType = MarkdownSegmentType.CODE
        currentSegment.builder.append(segment)
        continue
      }

      if (currentSegment.markdownType == MarkdownSegmentType.CODE) {
        currentSegment.builder.append("\n")
        currentSegment.builder.append(segment)
        continue
      }

      if (segment.startsWith(Delimiters.CODE)) {
        maybeAddCurrentSegment()
        currentSegment.markdownType = MarkdownSegmentType.CODE
        currentSegment.builder.append(segment)
        continue
      }

      if (segment == Delimiters.SEPARATOR) {
        maybeAddCurrentSegment()
        currentSegment.markdownType = MarkdownSegmentType.SEPARATOR
        currentSegment.builder.append(segment)
        continue
      }

      if (segment.startsWith(Delimiters.HEADING_1)
          || segment.startsWith(Delimiters.HEADING_2)
          || segment.startsWith(Delimiters.HEADING_3)) {
        maybeAddCurrentSegment()
        currentSegment.markdownType = when {
          segment.startsWith(Delimiters.HEADING_3) -> MarkdownSegmentType.HEADING_3
          segment.startsWith(Delimiters.HEADING_2) -> MarkdownSegmentType.HEADING_2
          else -> MarkdownSegmentType.HEADING_1
        }
        currentSegment.builder.append(segment)
        maybeAddCurrentSegment()
        continue
      }

      if (segment.startsWith(Delimiters.QUOTE)) {
        maybeAddCurrentSegment()
        currentSegment.markdownType = MarkdownSegmentType.QUOTE
        currentSegment.builder.append(segment)
        continue
      }

      if (segment.trim().startsWith(Delimiters.BULLET)) {
        maybeAddCurrentSegment()
        val index = segment.indexOf(Delimiters.BULLET)
        currentSegment.markdownType = when {
          index <= 0 -> MarkdownSegmentType.BULLET_1
          index <= 2 -> MarkdownSegmentType.BULLET_2
          else -> MarkdownSegmentType.BULLET_3
        }
        currentSegment.builder.append(segment)
        maybeAddCurrentSegment()
        continue
      }

      // Either extension or new Normal
      if (currentSegment.markdownType != MarkdownSegmentType.INVALID && segment.isEmpty()) {
        maybeAddCurrentSegment()
        currentSegment.markdownType = MarkdownSegmentType.NORMAL
        currentSegment.builder.append(segment)
      } else if (currentSegment.markdownType != MarkdownSegmentType.INVALID) {
        currentSegment.builder.append("\n")
        currentSegment.builder.append(segment)
      } else {
        currentSegment.markdownType = MarkdownSegmentType.NORMAL
        currentSegment.builder.append(segment)
      }
    }
    maybeAddCurrentSegment()
  }

  private fun maybeAddCurrentSegment() {
    if (currentSegment.markdownType == MarkdownSegmentType.INVALID) {
      currentSegment = MarkdownSegmentBuilder()
      return
    }
    processedSegments.add(currentSegment.build())
    currentSegment = MarkdownSegmentBuilder()
  }

}