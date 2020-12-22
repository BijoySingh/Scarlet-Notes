package com.maubis.markdown.spannable

import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.QuoteSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import com.maubis.markdown.MarkdownConfig.Companion.config
import com.maubis.markdown.spans.CodeSegmentSpan
import com.maubis.markdown.spans.CodeSpan
import com.maubis.markdown.spans.CustomTypefaceSpan
import com.maubis.markdown.spans.ICustomSpan
import com.maubis.markdown.spans.QuoteSegmentSpan
import com.maubis.markdown.spans.SeparatorSegmentSpan

fun Editable.clearMarkdownSpans() {
  val spans = getSpans(0, length, Any::class.java)
  for (span in spans) {
    if (span is RelativeSizeSpan
      || span is QuoteSpan
      || span is StyleSpan
      || span is TypefaceSpan
      || span is UnderlineSpan
      || span is ICustomSpan
      || span is ForegroundColorSpan
      || span is BackgroundColorSpan) {
      removeSpan(span)
    }
  }
}

fun Spannable.color(color: String, start: Int, end: Int): Spannable {
  this.setSpan(ForegroundColorSpan(Color.parseColor(color)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
  return this
}

fun Spannable.bold(start: Int, end: Int): Spannable {
  this.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
  return this
}

fun Spannable.underline(start: Int, end: Int): Spannable {
  this.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
  return this
}

fun Spannable.italic(start: Int, end: Int): Spannable {
  this.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
  return this
}

fun Spannable.strike(start: Int, end: Int): Spannable {
  this.setSpan(StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
  return this
}

fun Spannable.background(color: Int, start: Int, end: Int): Spannable {
  this.setSpan(BackgroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
  return this
}

fun Spannable.relativeSize(relativeSize: Float, start: Int, end: Int): Spannable {
  this.setSpan(RelativeSizeSpan(relativeSize), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
  return this
}

fun Spannable.monospace(start: Int, end: Int): Spannable {
  this.setSpan(TypefaceSpan("monospace"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
  return this
}

fun Spannable.quote(start: Int, end: Int): Spannable {
  this.setSpan(QuoteSegmentSpan(), start, end, 0)
  return this
}

fun Spannable.code(start: Int, end: Int): Spannable {
  this.setSpan(CodeSegmentSpan(), start, end, 0)
  return this
}

fun Spannable.inlineCode(start: Int, end: Int): Spannable {
  this.setSpan(CodeSpan(), start, end, 0)
  return this
}

fun Spannable.separator(start: Int, end: Int): Spannable {
  this.setSpan(SeparatorSegmentSpan(), start, end, 0)
  return this
}

fun Spannable.font(font: Typeface, start: Int, end: Int): Spannable {
  this.setSpan(CustomTypefaceSpan(font), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
  return this
}

fun Spannable.setDefaultFormats(info: SpanInfo) {
  val s = info.start
  val e = info.end
  when (info.markdownType) {
    MarkdownType.HEADING_1 -> relativeSize(1.75f, s, e).font(config.spanConfig.headingTypeface, s, e).bold(s, e)
    MarkdownType.HEADING_2 -> relativeSize(1.5f, s, e).font(config.spanConfig.heading2Typeface, s, e).bold(s, e)
    MarkdownType.HEADING_3 -> relativeSize(1.25f, s, e).font(config.spanConfig.heading3Typeface, s, e).bold(s, e)
    MarkdownType.CODE -> font(config.spanConfig.codeTypeface, s, e).code(s, e)
    MarkdownType.QUOTE -> quote(s, e).italic(s, e)
    MarkdownType.BOLD -> bold(s, e)
    MarkdownType.ITALICS -> italic(s, e)
    MarkdownType.UNDERLINE -> underline(s, e)
    MarkdownType.INLINE_CODE -> inlineCode(s, e)
    MarkdownType.STRIKE -> strike(s, e)
    MarkdownType.SEPARATOR -> separator(s, e)
    MarkdownType.IMAGE -> monospace(s, e).code(s, e)
    else -> {}
  }
}

fun Spannable.setFormats(info: List<SpanInfo>) {
  info.forEach { setDefaultFormats(it) }
}