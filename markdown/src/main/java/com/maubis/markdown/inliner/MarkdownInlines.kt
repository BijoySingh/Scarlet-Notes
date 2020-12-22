package com.maubis.markdown.inliner

import com.maubis.markdown.spannable.MarkdownType
import com.maubis.markdown.spannable.SpanInfo
import com.maubis.markdown.spannable.map

interface IMarkdownInlineBuilder {}

class NormalInlineBuilder : IMarkdownInlineBuilder {
  val builder: StringBuilder = StringBuilder()
  fun build(): NormalInlineMarkdownSegment {
    return NormalInlineMarkdownSegment(builder.toString())
  }
}

class MarkdownInlineBuilder : IMarkdownInlineBuilder {
  val children = ArrayList<MarkdownInline>()
  var config: IInlineConfig = InvalidInline(MarkdownInlineType.INVALID)
  var paired: Boolean = false

  fun build(): MarkdownInline {
    return PhraseDelimiterMarkdownInline(config, children)
  }
}

abstract class MarkdownInline {
  abstract fun type(): MarkdownInlineType

  abstract fun config(): IInlineConfig

  /**
   * The original text inside an inline,
   * e.g. for the BOLD in this example "a **b _c_ d** e" is "**b _c_ d**"
   */
  abstract fun contentText(stripDelimiters: Boolean = false): String

  /**
   * The list of spans that make up this block.
   * This can be overlapping information, which is needed for rendering
   */
  abstract fun allContentSpans(stripDelimiters: Boolean = false, startPosition: Int): List<SpanInfo>

  abstract fun toMarkwon(): String

  fun debug(): String {
    if (this !is PhraseDelimiterMarkdownInline) {
      return contentText()
    }

    val string = StringBuilder()
    string.append("{${type().name}: ")
    children.forEach {
      string.append(it.debug())
    }
    string.append("}")
    return string.toString()
  }
}

class NormalInlineMarkdownSegment(val text: String) : MarkdownInline() {
  override fun type() = MarkdownInlineType.NORMAL

  override fun config(): IInlineConfig = InvalidInline(MarkdownInlineType.NORMAL)

  override fun contentText(stripDelimiters: Boolean): String {
    return text
  }

  override fun allContentSpans(stripDelimiters: Boolean, startPosition: Int): List<SpanInfo> {
    return listOf(SpanInfo(MarkdownType.NORMAL, startPosition, startPosition + contentText(stripDelimiters).length))
  }

  override fun toMarkwon(): String {
    return text
  }
}

class PhraseDelimiterMarkdownInline(val config: IInlineConfig, val children: List<MarkdownInline>) : MarkdownInline() {

  override fun type() = config.type()

  override fun config(): IInlineConfig = config

  override fun contentText(stripDelimiters: Boolean): String {
    val builder = StringBuilder()
    if (!stripDelimiters && config is PhraseDelimiterInline) {
      builder.append(config.startDelimiter)
    }
    children.forEach { builder.append(it.contentText(stripDelimiters)) }
    if (!stripDelimiters && config is PhraseDelimiterInline) {
      builder.append(config.endDelimiter)
    }
    return builder.toString()
  }

  override fun allContentSpans(stripDelimiters: Boolean, startPosition: Int): List<SpanInfo> {
    val childrenSpans = ArrayList<SpanInfo>()
    var currentIndex = startPosition
    children.forEach {
      childrenSpans.addAll(it.allContentSpans(stripDelimiters, currentIndex))
      currentIndex += it.contentText(stripDelimiters).length
    }
    childrenSpans.add(SpanInfo(map(type()), startPosition, startPosition + contentText(stripDelimiters).length))
    return childrenSpans
  }

  override fun toMarkwon(): String {
    val builder = StringBuilder()
    val config = TextInlineConfig.getDefaultOutputConfig(type())
    when {
      config is PhraseDelimiterInline -> builder.append(config.startDelimiter)
      config is StartMarkerInline -> builder.append(config.startDelimiter)
    }
    children.forEach { builder.append(it.toMarkwon()) }
    if (config is PhraseDelimiterInline) {
      builder.append(config.endDelimiter)
    }
    return builder.toString()
  }
}
