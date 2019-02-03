package com.maubis.markdown.segmenter

import android.text.Spannable
import android.text.SpannableStringBuilder
import com.maubis.markdown.inliner.TextInliner
import com.maubis.markdown.spannable.relativeSize

class MarkdownSegmentBuilder {
  val builder: StringBuilder = StringBuilder()
  var markdownType: MarkdownSegmentType = MarkdownSegmentType.INVALID

  fun build(): MarkdownSegment {
    val text = builder.toString()
    return when (markdownType) {
      MarkdownSegmentType.NORMAL, MarkdownSegmentType.INVALID -> NormalSegment(text)
      MarkdownSegmentType.HEADING_1 -> Heading1Segment(text)
      MarkdownSegmentType.HEADING_2 -> Heading2Segment(text)
      MarkdownSegmentType.HEADING_3 -> Heading3Segment(text)
      MarkdownSegmentType.BULLET_1 -> Bullet1Segment(text)
      MarkdownSegmentType.BULLET_2 -> Bullet2Segment(text)
      MarkdownSegmentType.BULLET_3 -> Bullet3Segment(text)
      MarkdownSegmentType.QUOTE -> QuoteSegment(text)
      MarkdownSegmentType.CODE -> CodeSegment(text)
      MarkdownSegmentType.SEPARATOR -> SeparatorSegment(text)
    }
  }
}

abstract class MarkdownSegment(val text: String) {

  abstract fun type(): MarkdownSegmentType

  abstract fun strip(): String

  abstract fun spannable(strip: Boolean): Spannable
}

open class NormalSegment(text: String) : MarkdownSegment(text) {
  override fun type() = MarkdownSegmentType.NORMAL

  override fun strip(): String {
    return text
  }

  override fun spannable(strip: Boolean): Spannable {
    val spannableBaseText = if (strip) strip() else text
    return TextInliner(spannableBaseText).get().spannable(strip)
  }
}

class Heading1Segment(text: String) : MarkdownSegment(text) {
  override fun type() = MarkdownSegmentType.HEADING_1

  override fun strip(): String {
    return text.removePrefix(TextSegmenter.Delimiters.HEADING_1)
  }

  override fun spannable(strip: Boolean): Spannable {
    val spannableBaseText = if (strip) strip() else text
    val spannable = TextInliner(spannableBaseText).get().spannable(strip)
    return spannable.relativeSize(2f, 0, spannable.length)
  }
}

class Heading2Segment(text: String) : MarkdownSegment(text) {
  override fun type() = MarkdownSegmentType.HEADING_2

  override fun strip(): String {
    return text.removePrefix(TextSegmenter.Delimiters.HEADING_2)
  }

  override fun spannable(strip: Boolean): Spannable {
    val spannableBaseText = if (strip) strip() else text
    val spannable = TextInliner(spannableBaseText).get().spannable(strip)
    return spannable.relativeSize(1.5f, 0, spannable.length)
  }
}

class Heading3Segment(text: String) : MarkdownSegment(text) {
  override fun type() = MarkdownSegmentType.HEADING_3

  override fun strip(): String {
    return text.removePrefix(TextSegmenter.Delimiters.HEADING_3)
  }

  override fun spannable(strip: Boolean): Spannable {
    val spannableBaseText = if (strip) strip() else text
    val spannable = TextInliner(spannableBaseText).get().spannable(strip)
    return spannable.relativeSize(1.2f, 0, spannable.length)
  }
}

class Bullet1Segment(text: String) : MarkdownSegment(text) {
  override fun type() = MarkdownSegmentType.BULLET_1

  override fun strip(): String {
    return text.trim().removePrefix(TextSegmenter.Delimiters.BULLET)
  }

  override fun spannable(strip: Boolean): Spannable {
    if (!strip) {
      return TextInliner(text).get().spannable(strip)
    }

    val builder = SpannableStringBuilder()
    builder.append("\u2022 ")
    builder.append(TextInliner(strip()).get().spannable(strip))
    return builder
  }
}

class Bullet2Segment(text: String) : MarkdownSegment(text) {
  override fun type() = MarkdownSegmentType.BULLET_2

  override fun strip(): String {
    return text.trim().removePrefix(TextSegmenter.Delimiters.BULLET)
  }

  override fun spannable(strip: Boolean): Spannable {
    if (!strip) {
      return TextInliner(text).get().spannable(strip)
    }

    val builder = SpannableStringBuilder()
    builder.append("  \u25E6 ")
    builder.append(TextInliner(strip()).get().spannable(strip))
    return builder
  }
}

class Bullet3Segment(text: String) : MarkdownSegment(text) {
  override fun type() = MarkdownSegmentType.BULLET_3

  override fun strip(): String {
    return text.trim().removePrefix(TextSegmenter.Delimiters.BULLET)
  }

  override fun spannable(strip: Boolean): Spannable {
    if (!strip) {
      return TextInliner(text).get().spannable(strip)
    }

    val builder = SpannableStringBuilder()
    builder.append("    \u2023 ")
    builder.append(TextInliner(strip()).get().spannable(strip))
    return builder
  }
}

class QuoteSegment(text: String) : MarkdownSegment(text) {
  override fun type() = MarkdownSegmentType.QUOTE

  override fun strip(): String {
    return text.removePrefix(TextSegmenter.Delimiters.QUOTE)
  }

  override fun spannable(strip: Boolean): Spannable {
    val spannableBaseText = if (strip) strip() else text
    return TextInliner(spannableBaseText).get().spannable(strip)
  }
}

class CodeSegment(text: String) : MarkdownSegment(text) {
  override fun type() = MarkdownSegmentType.CODE

  override fun strip(): String {
    return text.trim()
        .removePrefix(TextSegmenter.Delimiters.CODE)
        .removeSuffix(TextSegmenter.Delimiters.CODE)
        .trim()
  }

  override fun spannable(strip: Boolean): Spannable {
    val spannableBaseText = if (strip) strip() else text
    return TextInliner(spannableBaseText).get().spannable(strip)
  }
}

class SeparatorSegment(text: String): NormalSegment(text) {
  override fun type() = MarkdownSegmentType.SEPARATOR
}