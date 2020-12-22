package com.maubis.markdown.segmenter

interface ISegmentConfig {
  fun type(): MarkdownSegmentType = MarkdownSegmentType.INVALID
  fun isValid(segment: String) = false
  fun isStart(segment: String) = false
  fun isEnd(segment: String) = false
}

class InvalidSegment(val type: MarkdownSegmentType) : ISegmentConfig {
  override fun type() = type
}

class FullLineSegment(val type: MarkdownSegmentType, val lineToken: String) : ISegmentConfig {
  override fun type() = type

  override fun isValid(segment: String): Boolean {
    return segment.trim() == lineToken
  }
}

class LineStartSegment(val type: MarkdownSegmentType,
                       val lineStartToken: String) : ISegmentConfig {
  override fun type() = type

  override fun isValid(segment: String): Boolean {
    return segment.startsWith(lineStartToken)
  }
}

class LineDelimiterSegment(
    val type: MarkdownSegmentType,
    val lineStartToken: String,
    val lineEndToken: String) : ISegmentConfig {
  override fun type() = type

  override fun isValid(segment: String): Boolean {
    return segment.startsWith(lineStartToken) && segment.trimEnd().endsWith(lineEndToken)
  }
}

class MultilineDelimiterSegment(
    val type: MarkdownSegmentType,
    val multilineStartToken: String,
    val multilineEndToken: String) : ISegmentConfig {
  override fun type() = type

  override fun isStart(segment: String): Boolean {
    return segment.trim() == multilineStartToken
  }

  override fun isEnd(segment: String): Boolean {
    return segment.trim() == multilineEndToken
  }
}

class MultilineStartSegment(
    val type: MarkdownSegmentType,
    val multilineStartToken: String) : ISegmentConfig {
  override fun type() = type

  override fun isStart(segment: String): Boolean {
    return segment.startsWith(multilineStartToken)
  }
}

class TextSegmentConfig(builder: Builder) {
  val configuration: List<ISegmentConfig>
  val outputConfiguration: Map<MarkdownSegmentType, ISegmentConfig>

  init {
    builder.build()
    configuration = builder.configuration
    outputConfiguration = builder.outputConfiguration
  }

  class Builder {
    internal val configuration = ArrayList<ISegmentConfig>()
    internal val outputConfiguration = HashMap<MarkdownSegmentType, ISegmentConfig>()

    fun addConfig(config: ISegmentConfig) {
      configuration.add(config)
    }

    fun setOutputConfig(config: ISegmentConfig) {
      outputConfiguration[config.type()] = config
    }

    fun build() {
      MarkdownSegmentType.values().forEach {
        if (!configuration.any { config -> config.type() == it }) {
          configuration.addAll(getDefaultConfig(it))
        }
        if (!outputConfiguration.containsKey(it)) {
          outputConfiguration[it] = getDefaultOutputConfig(it)
        }
      }
    }
  }

  companion object {

    fun getDefaultConfig(type: MarkdownSegmentType): Array<ISegmentConfig> {
      return when (type) {
        MarkdownSegmentType.INVALID -> arrayOf(
            InvalidSegment(MarkdownSegmentType.INVALID))
        MarkdownSegmentType.HEADING_1 -> arrayOf(
            LineStartSegment(MarkdownSegmentType.HEADING_1, "# "))
        MarkdownSegmentType.HEADING_2 -> arrayOf(
            LineStartSegment(MarkdownSegmentType.HEADING_2, "## "))
        MarkdownSegmentType.HEADING_3 -> arrayOf(
            LineStartSegment(MarkdownSegmentType.HEADING_3, "### "))
        MarkdownSegmentType.NORMAL -> arrayOf(
            InvalidSegment(MarkdownSegmentType.NORMAL))
        MarkdownSegmentType.CODE -> arrayOf(
            MultilineDelimiterSegment(MarkdownSegmentType.CODE, "```", "```"))
        MarkdownSegmentType.BULLET_1 -> arrayOf(
            LineStartSegment(MarkdownSegmentType.BULLET_1, "- "))
        MarkdownSegmentType.BULLET_2 -> arrayOf(
            LineStartSegment(MarkdownSegmentType.BULLET_2, "  - "),
            LineStartSegment(MarkdownSegmentType.BULLET_2, " - "))
        MarkdownSegmentType.BULLET_3 -> arrayOf(
            LineStartSegment(MarkdownSegmentType.BULLET_3, "    - "),
            LineStartSegment(MarkdownSegmentType.BULLET_3, "    - "),
            LineStartSegment(MarkdownSegmentType.BULLET_3, "     - "))
        MarkdownSegmentType.QUOTE -> arrayOf(
            MultilineStartSegment(MarkdownSegmentType.QUOTE, "> "))
        MarkdownSegmentType.SEPARATOR -> arrayOf(
            FullLineSegment(MarkdownSegmentType.SEPARATOR, "---"),
            FullLineSegment(MarkdownSegmentType.SEPARATOR, "___"),
            FullLineSegment(MarkdownSegmentType.SEPARATOR, "----"),
            FullLineSegment(MarkdownSegmentType.SEPARATOR, "-----"),
            FullLineSegment(MarkdownSegmentType.SEPARATOR, "------"),
            FullLineSegment(MarkdownSegmentType.SEPARATOR, "-------"))
        MarkdownSegmentType.CHECKLIST_UNCHECKED -> arrayOf(
            LineStartSegment(MarkdownSegmentType.CHECKLIST_UNCHECKED, "[] "),
            LineStartSegment(MarkdownSegmentType.CHECKLIST_UNCHECKED, "[ ] "))
        MarkdownSegmentType.CHECKLIST_CHECKED -> arrayOf(
            LineStartSegment(MarkdownSegmentType.CHECKLIST_CHECKED, "[x] "),
            LineStartSegment(MarkdownSegmentType.CHECKLIST_CHECKED, "[X] "))
        MarkdownSegmentType.IMAGE -> arrayOf(
            LineDelimiterSegment(MarkdownSegmentType.IMAGE, "<<img src=\"", "\"/>"),
            LineDelimiterSegment(MarkdownSegmentType.IMAGE, "<image>", "</image>"))
      }
    }

    fun getDefaultOutputConfig(type: MarkdownSegmentType): ISegmentConfig {
      return when (type) {
        MarkdownSegmentType.INVALID -> InvalidSegment(MarkdownSegmentType.INVALID)
        MarkdownSegmentType.HEADING_1 -> LineStartSegment(MarkdownSegmentType.HEADING_1, "# ")
        MarkdownSegmentType.HEADING_2 -> LineStartSegment(MarkdownSegmentType.HEADING_2, "## ")
        MarkdownSegmentType.HEADING_3 -> LineStartSegment(MarkdownSegmentType.HEADING_3, "### ")
        MarkdownSegmentType.NORMAL -> InvalidSegment(MarkdownSegmentType.NORMAL)
        MarkdownSegmentType.CODE -> MultilineDelimiterSegment(MarkdownSegmentType.CODE, "```", "```")
        MarkdownSegmentType.BULLET_1 -> LineStartSegment(MarkdownSegmentType.BULLET_1, "- ")
        MarkdownSegmentType.BULLET_2 -> LineStartSegment(MarkdownSegmentType.BULLET_2, "  - ")
        MarkdownSegmentType.BULLET_3 -> LineStartSegment(MarkdownSegmentType.BULLET_3, "    - ")
        MarkdownSegmentType.QUOTE -> MultilineStartSegment(MarkdownSegmentType.QUOTE, "> ")
        MarkdownSegmentType.SEPARATOR -> LineStartSegment(MarkdownSegmentType.SEPARATOR, "---")
        MarkdownSegmentType.CHECKLIST_UNCHECKED -> LineStartSegment(MarkdownSegmentType.CHECKLIST_UNCHECKED, "[ ] ")
        MarkdownSegmentType.CHECKLIST_CHECKED -> LineStartSegment(MarkdownSegmentType.CHECKLIST_CHECKED, "[x] ")
        MarkdownSegmentType.IMAGE -> LineDelimiterSegment(MarkdownSegmentType.IMAGE, "<img src=\"", "\"/>")
      }
    }
  }
}