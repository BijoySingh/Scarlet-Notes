package com.maubis.markdown.segmenter

import com.maubis.markdown.MarkdownConfig.Companion.config

class TextSegmenter(val text: String) {

  private val segmentConfig = config.segmenterConfig
  private var currentSegment = MarkdownSegmentBuilder()
  private val processedSegments = ArrayList<MarkdownSegment>()

  fun get(): List<MarkdownSegment> {
    processSegments()
    return processedSegments
  }

  private fun processSegments() {
    processedSegments.clear()
    val allMultilineSegments = segmentConfig.configuration.filter { it is MultilineDelimiterSegment }
    val allFullLineSegments = segmentConfig.configuration.filter { it is FullLineSegment }
    val allMultilineStartSegments = segmentConfig.configuration.filter { it is MultilineStartSegment }
    val allSingleLineSegments = segmentConfig.configuration.filter { it is LineStartSegment || it is LineDelimiterSegment }

    val segments = text.split("\n")
    for (segment in segments) {
      val startCurrentConfig = currentSegment

      // Multiline Code is finishing
      if (startCurrentConfig.config is MultilineDelimiterSegment
          && startCurrentConfig.config.isEnd(segment)) {
        currentSegment.builder.append("\n")
        currentSegment.builder.append(segment)
        maybeAddCurrentSegment()
        continue
      }

      // Continuing the multiline code
      if (startCurrentConfig.config is MultilineDelimiterSegment) {
        currentSegment.builder.append("\n")
        currentSegment.builder.append(segment)
        continue
      }

      // Check if full line segment
      val fullLineSegment = allFullLineSegments.firstOrNull { it.isValid(segment) }
      if (fullLineSegment !== null) {
        maybeAddCurrentSegment()
        currentSegment.config = fullLineSegment
        currentSegment.builder.append(segment)
        maybeAddCurrentSegment()
        continue
      }

      // Check if multiline segment start
      val multilineSegment = allMultilineSegments.firstOrNull { it.isStart(segment) }
      if (multilineSegment !== null) {
        maybeAddCurrentSegment()
        currentSegment.config = multilineSegment
        currentSegment.builder.append(segment)
        continue
      }

      // Check if start of multiline start segment
      val multilineStartSegment = allMultilineStartSegments.firstOrNull { it.isStart(segment) }
      if (multilineStartSegment !== null) {
        maybeAddCurrentSegment()
        currentSegment.config = multilineStartSegment
        currentSegment.builder.append(segment)
        continue
      }

      // Check if single line segments
      val singleLineSegment = allSingleLineSegments.firstOrNull { it.isValid(segment) }
      if (singleLineSegment !== null) {
        maybeAddCurrentSegment()
        currentSegment.config = singleLineSegment
        currentSegment.builder.append(segment)
        maybeAddCurrentSegment()
        continue
      }

      // Multiline start segment in progress, end if double new line
      if (currentSegment.config is MultilineStartSegment && segment.isEmpty()) {
        maybeAddCurrentSegment()
        currentSegment.config = InvalidSegment(MarkdownSegmentType.NORMAL)
        currentSegment.builder.append(segment)
        continue
      }

      if (currentSegment.config.type() == MarkdownSegmentType.INVALID) {
        currentSegment.config = InvalidSegment(MarkdownSegmentType.NORMAL)
        currentSegment.builder.append(segment)
        continue
      }

      // Normal or multiline start segment
      currentSegment.builder.append("\n")
      currentSegment.builder.append(segment)
    }
    maybeAddCurrentSegment()
  }

  private fun maybeAddCurrentSegment() {
    if (currentSegment.config.type() == MarkdownSegmentType.INVALID) {
      currentSegment = MarkdownSegmentBuilder()
      return
    }
    processedSegments.add(currentSegment.build())
    currentSegment = MarkdownSegmentBuilder()
  }

}