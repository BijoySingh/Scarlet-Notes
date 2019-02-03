package com.maubis.markdown.inliner

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import com.maubis.markdown.spannable.*

interface IMarkdownInlineBuilder {}

class NormalInlineBuilder : IMarkdownInlineBuilder {
  val builder: StringBuilder = StringBuilder()
  fun build(): NormalInlineSegment {
    return NormalInlineSegment(builder.toString())
  }
}

class MarkdownInlineBuilder : IMarkdownInlineBuilder {
  val children = ArrayList<MarkdownInline>()
  var markdownType: MarkdownInlineType = MarkdownInlineType.INVALID
  var paired: Boolean = false

  fun build(): MarkdownInline {
    return when (markdownType) {
      MarkdownInlineType.NORMAL, MarkdownInlineType.INVALID -> DefaultMarkdownInline(children)
      MarkdownInlineType.BOLD -> BoldMarkdownInline(children)
      MarkdownInlineType.INLINE_CODE -> CodeMarkdownInline(children)
      MarkdownInlineType.ITALICS -> ItalicsMarkdownInline(children)
      MarkdownInlineType.UNDERLINE -> UnderlineMarkdownInline(children)
      MarkdownInlineType.STRIKE -> StrikeMarkdownInline(children)
    }
  }
}

abstract class MarkdownInline() {
  abstract fun type(): MarkdownInlineType

  abstract fun original(): String

  abstract fun fullText(): Spannable

  abstract fun spannable(strip: Boolean = false): SpannableString

  abstract fun spans(startPosition: Int): List<SpanInfo>

  fun debug(): String {
    if (this !is ComplexMarkdownInline) {
      return original()
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

class NormalInlineSegment(val text: String) : MarkdownInline() {
  override fun type() = MarkdownInlineType.NORMAL

  override fun original(): String {
    return text
  }

  override fun fullText(): Spannable {
    return SpannableString(text)
  }

  override fun spannable(strip: Boolean): SpannableString {
    return SpannableString(text)
  }

  override fun spans(startPosition: Int): List<SpanInfo> {
    return listOf(SpanInfo(MarkdownType.NORMAL, startPosition, startPosition + original().length))
  }
}

abstract class ComplexMarkdownInline(val children: List<MarkdownInline>) : MarkdownInline() {
  fun text(): CharSequence {
    val list = children.map { it.fullText() }
    return TextUtils.concat(*list.toTypedArray())
  }

  override fun original(): String {
    val builder = StringBuilder()
    children.forEach { builder.append(it.original()) }
    return builder.toString()
  }

  override fun spans(startPosition: Int): List<SpanInfo> {
    val childrenSpans = ArrayList<SpanInfo>()
    var currentIndex = startPosition
    children.forEach {
      childrenSpans.addAll(it.spans(currentIndex))
      currentIndex += it.original().length
    }
    childrenSpans.add(SpanInfo(map(type()), startPosition, startPosition + original().length))
    return childrenSpans
  }
}

class DefaultMarkdownInline(children: List<MarkdownInline>) : ComplexMarkdownInline(children) {
  override fun type(): MarkdownInlineType = MarkdownInlineType.INVALID

  override fun fullText(): Spannable {
    return SpannableString(text())
  }

  override fun spannable(strip: Boolean): SpannableString {
    val list = children.map { it.spannable(strip) }
    return SpannableString(TextUtils.concat(*list.toTypedArray()))
  }
}


class BoldMarkdownInline(children: List<MarkdownInline>) : ComplexMarkdownInline(children) {
  override fun type(): MarkdownInlineType = MarkdownInlineType.BOLD

  override fun original(): String {
    return "**${super.original()}**"
  }

  override fun fullText(): Spannable {
    val builder = SpannableStringBuilder()
    builder.append(TextInliner.Delimiters.BOLD)
    builder.append(text())
    builder.append(TextInliner.Delimiters.BOLD)
    return builder
  }

  override fun spannable(strip: Boolean): SpannableString {
    val text = if (strip) text() else fullText()
    return SpannableString(text).bold(0, text.length)
  }
}

class CodeMarkdownInline(children: List<MarkdownInline>) : ComplexMarkdownInline(children) {
  override fun type(): MarkdownInlineType = MarkdownInlineType.INLINE_CODE

  override fun original(): String {
    return "`${super.original()}`"
  }

  override fun fullText(): Spannable {
    val builder = SpannableStringBuilder()
    builder.append(TextInliner.Delimiters.INLINE_CODE)
    builder.append(text())
    builder.append(TextInliner.Delimiters.INLINE_CODE)
    return builder
  }

  override fun spannable(strip: Boolean): SpannableString {
    val text = if (strip) text() else fullText()
    return SpannableString(text)
        .monospace(0, text.length)
        .background(Color.GRAY, 0, text.length)
  }
}

class ItalicsMarkdownInline(children: List<MarkdownInline>) : ComplexMarkdownInline(children) {
  override fun type(): MarkdownInlineType = MarkdownInlineType.ITALICS

  override fun original(): String {
    return "_${super.original()}_"
  }

  override fun fullText(): Spannable {
    val builder = SpannableStringBuilder()
    builder.append(TextInliner.Delimiters.ITALICS)
    builder.append(text())
    builder.append(TextInliner.Delimiters.ITALICS)
    return builder
  }

  override fun spannable(strip: Boolean): SpannableString {
    val text = if (strip) text() else fullText()
    return SpannableString(text).italic(0, text.length)
  }
}

class UnderlineMarkdownInline(children: List<MarkdownInline>) : ComplexMarkdownInline(children) {
  override fun type(): MarkdownInlineType = MarkdownInlineType.UNDERLINE

  override fun original(): String {
    return "*${super.original()}*"
  }

  override fun fullText(): Spannable {
    val builder = SpannableStringBuilder()
    builder.append(TextInliner.Delimiters.UNDERLINE)
    builder.append(text())
    builder.append(TextInliner.Delimiters.UNDERLINE)
    return builder
  }

  override fun spannable(strip: Boolean): SpannableString {
    val text = if (strip) text() else fullText()
    return SpannableString(text).underline(0, text.length)
  }
}

class StrikeMarkdownInline(children: List<MarkdownInline>) : ComplexMarkdownInline(children) {
  override fun type(): MarkdownInlineType = MarkdownInlineType.STRIKE

  override fun original(): String {
    return "~${super.original()}~"
  }

  override fun fullText(): Spannable {
    val builder = SpannableStringBuilder()
    builder.append(TextInliner.Delimiters.STRIKE)
    builder.append(text())
    builder.append(TextInliner.Delimiters.STRIKE)
    return builder
  }

  override fun spannable(strip: Boolean): SpannableString {
    val text = if (strip) text() else fullText()
    return SpannableString(text).strike(0, text.length)
  }
}