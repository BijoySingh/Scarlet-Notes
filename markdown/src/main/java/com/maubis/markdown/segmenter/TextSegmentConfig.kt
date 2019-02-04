package com.maubis.markdown.segmenter

interface ISegmentConfig {}

class LineStartSegment(
    val lineStartToken: String,
    val outputLineStartToken: String = lineStartToken) : ISegmentConfig

class LineDelimiterSegment(
    val lineStartToken: String,
    val lineEndToken: String,
    val outputLineStartToken: String = lineStartToken,
    val outputLineEndToken: String = lineEndToken) : ISegmentConfig

class MultilineDelimiterSegment(
    val multilineStartToken: String,
    val multilineEndToken: String,
    val outputMultilineStartToken: String = multilineStartToken,
    val outputMultilineEndToken: String = multilineEndToken) : ISegmentConfig

class TextSegmentConfig {
  val config = HashMap<MarkdownSegmentType, List<ISegmentConfig>>()

}