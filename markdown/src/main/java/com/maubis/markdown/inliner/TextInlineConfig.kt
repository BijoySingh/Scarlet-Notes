package com.maubis.markdown.inliner

interface IInlineConfig {
  fun type(): MarkdownInlineType = MarkdownInlineType.INVALID
  fun isStart(segment: String, index: Int) = false
  fun startIncrement() = 0
  fun isEnd(segment: String, index: Int) = false
  fun endIncrement() = 0
  fun identifier() = ""
}

class InvalidInline(val type: MarkdownInlineType) : IInlineConfig {
  override fun type() = type
}

class StartMarkerInline(
    val type: MarkdownInlineType, val startDelimiter: String) : IInlineConfig {
  override fun type() = type

  override fun isStart(segment: String, index: Int): Boolean {
    if (index + startDelimiter.length > segment.length) {
      return false
    }
    return segment.regionMatches(index, startDelimiter, 0, startDelimiter.length, true)
  }

  override fun isEnd(segment: String, index: Int): Boolean = true

  override fun startIncrement(): Int = startDelimiter.length
  override fun identifier(): String = "${type().name}($startDelimiter)"
}

class PhraseDelimiterInline(
    val type: MarkdownInlineType,
    val startDelimiter: String,
    val endDelimiter: String) : IInlineConfig {
  override fun type() = type

  override fun isStart(segment: String, index: Int): Boolean {
    if (index + startDelimiter.length > segment.length) {
      return false
    }
    return segment.regionMatches(index, startDelimiter, 0, startDelimiter.length, true)
  }

  override fun isEnd(segment: String, index: Int): Boolean {
    if (index + endDelimiter.length > segment.length) {
      return false
    }
    return segment.regionMatches(index, endDelimiter, 0, endDelimiter.length, true)
  }

  override fun startIncrement(): Int = startDelimiter.length
  override fun endIncrement(): Int = endDelimiter.length
  override fun identifier(): String = "${type().name}($startDelimiter,$endDelimiter)"
}

class TextInlineConfig(builder: Builder) {
  val configuration: List<IInlineConfig>
  val outputConfiguration: Map<MarkdownInlineType, IInlineConfig>

  init {
    builder.build()
    configuration = builder.configuration
    outputConfiguration = builder.outputConfiguration
  }

  class Builder {
    internal val configuration = ArrayList<IInlineConfig>()
    internal val outputConfiguration = HashMap<MarkdownInlineType, IInlineConfig>()

    fun addConfig(config: IInlineConfig) {
      configuration.add(config)
    }

    fun setOutputConfig(config: IInlineConfig) {
      outputConfiguration[config.type()] = config
    }

    fun build() {
      MarkdownInlineType.values().forEach {
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

    fun getDefaultConfig(type: MarkdownInlineType): Array<IInlineConfig> {
      return when (type) {
        MarkdownInlineType.INVALID -> arrayOf(InvalidInline(MarkdownInlineType.INVALID))
        MarkdownInlineType.NORMAL -> arrayOf(InvalidInline(MarkdownInlineType.NORMAL))
        MarkdownInlineType.BOLD -> arrayOf(
            PhraseDelimiterInline(MarkdownInlineType.BOLD, "**", "**"),
            PhraseDelimiterInline(MarkdownInlineType.BOLD, "<b>", "</b>"),
            PhraseDelimiterInline(MarkdownInlineType.BOLD, "__", "__"),
            PhraseDelimiterInline(MarkdownInlineType.BOLD, "<strong>", "</strong>"))
        MarkdownInlineType.ITALICS -> arrayOf(
            PhraseDelimiterInline(MarkdownInlineType.ITALICS, "*", "*"),
            PhraseDelimiterInline(MarkdownInlineType.ITALICS, "<em>", "</em>"),
            PhraseDelimiterInline(MarkdownInlineType.ITALICS, "<i>", "</i>"))
        MarkdownInlineType.UNDERLINE -> arrayOf(
            PhraseDelimiterInline(MarkdownInlineType.UNDERLINE, "<u>", "</u>"),
            PhraseDelimiterInline(MarkdownInlineType.UNDERLINE, "_", "_"))
        MarkdownInlineType.INLINE_CODE -> arrayOf(
            PhraseDelimiterInline(MarkdownInlineType.INLINE_CODE, "<var>", "</var>"),
            PhraseDelimiterInline(MarkdownInlineType.INLINE_CODE, "`", "`"),
            PhraseDelimiterInline(MarkdownInlineType.INLINE_CODE, "<code>", "</code>"))
        MarkdownInlineType.STRIKE -> arrayOf(
            PhraseDelimiterInline(MarkdownInlineType.STRIKE, "~", "~"),
            PhraseDelimiterInline(MarkdownInlineType.STRIKE, "~~", "~~"),
            PhraseDelimiterInline(MarkdownInlineType.STRIKE, "~", "~"))
        MarkdownInlineType.IGNORE_CHAR -> arrayOf(
            StartMarkerInline(MarkdownInlineType.IGNORE_CHAR, "\\")
        )
      }
    }

    fun getDefaultOutputConfig(type: MarkdownInlineType): IInlineConfig {
      return when (type) {
        MarkdownInlineType.INVALID -> InvalidInline(MarkdownInlineType.INVALID)
        MarkdownInlineType.NORMAL -> InvalidInline(MarkdownInlineType.NORMAL)
        MarkdownInlineType.BOLD -> PhraseDelimiterInline(MarkdownInlineType.BOLD, "**", "**")
        MarkdownInlineType.ITALICS -> PhraseDelimiterInline(MarkdownInlineType.ITALICS, "<i>", "</i>")
        MarkdownInlineType.UNDERLINE -> PhraseDelimiterInline(MarkdownInlineType.UNDERLINE, "<u>", "</u>")
        MarkdownInlineType.INLINE_CODE -> PhraseDelimiterInline(MarkdownInlineType.INLINE_CODE, "`", "`")
        MarkdownInlineType.STRIKE -> PhraseDelimiterInline(MarkdownInlineType.STRIKE, "~~", "~~")
        MarkdownInlineType.IGNORE_CHAR -> StartMarkerInline(MarkdownInlineType.IGNORE_CHAR, "\\")
      }
    }
  }
}