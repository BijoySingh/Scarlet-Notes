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
  abstract fun originalText(): String

  /**
   * The list of spans that make up this block.
   * This can be overlapping information, which is needed for rendering
   */
  abstract fun allSpans(startPosition: Int): List<SpanInfo>

  /**
   * The text inside an inline without any of the delimiters
   * e.g. for the BOLD in this example "a **b _c_ d** e" is "b c d"
   */
  abstract fun strippedText(): String

  /**
   * The list of spans that make up the stripped portion of this block
   * This can be overlapping information, which is needed for rendering
   */
  abstract fun allStrippedSpans(startPosition: Int): List<SpanInfo>


  fun debug(): String {
    if (this !is PhraseDelimiterMarkdownInline) {
      return originalText()
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

  override fun originalText(): String {
    return text
  }

  override fun allSpans(startPosition: Int): List<SpanInfo> {
    return listOf(SpanInfo(MarkdownType.NORMAL, startPosition, startPosition + originalText().length))
  }

  override fun strippedText(): String = originalText()
  override fun allStrippedSpans(startPosition: Int): List<SpanInfo> = allSpans(startPosition)
}

class PhraseDelimiterMarkdownInline(val config: IInlineConfig, val children: List<MarkdownInline>) : MarkdownInline() {

  override fun type() = config.type()

  override fun config(): IInlineConfig = config

  override fun originalText(): String = contentText(false)

  override fun allSpans(startPosition: Int): List<SpanInfo> = allContentSpans(false, startPosition)

  override fun strippedText(): String = contentText(true)

  override fun allStrippedSpans(startPosition: Int): List<SpanInfo> = allContentSpans(true, startPosition)

  private fun contentText(stripDelimiters: Boolean): String {
    val builder = StringBuilder()
    if (!stripDelimiters && config is PhraseDelimiterInline) {
      builder.append(config.startDelimiter)
    }
    children.forEach { builder.append(it.originalText()) }
    if (!stripDelimiters && config is PhraseDelimiterInline) {
      builder.append(config.endDelimiter)
    }
    return builder.toString()
  }

  private fun allContentSpans(stripDelimiters: Boolean, startPosition: Int): List<SpanInfo> {
    val childrenSpans = ArrayList<SpanInfo>()
    var currentIndex = startPosition
    children.forEach {
      when (stripDelimiters) {
        true -> {
          childrenSpans.addAll(it.allStrippedSpans(currentIndex))
          currentIndex += it.strippedText().length
        }
        false -> {
          childrenSpans.addAll(it.allSpans(currentIndex))
          currentIndex += it.originalText().length
        }
      }
    }
    childrenSpans.add(SpanInfo(map(type()), startPosition, startPosition + contentText(stripDelimiters).length))
    return childrenSpans
  }

}
