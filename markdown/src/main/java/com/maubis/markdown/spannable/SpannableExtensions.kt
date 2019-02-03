package com.maubis.markdown.spannable

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.Spanned
import android.text.style.*

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
  this.setSpan(RelativeSizeSpan(relativeSize), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
  return this
}

fun Spannable.monospace(start: Int, end: Int): Spannable {
  this.setSpan(TypefaceSpan("monospace"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
  return this
}

fun Spannable.quote(color: Int, start: Int, end: Int): Spannable {
  this.setSpan(QuoteSpan(color), start, end, 0)
  return this
}

fun Spannable.setFormats(info: SpanInfo) {
  when (info.markdownType) {
    MarkdownType.INVALID -> {
    }
    MarkdownType.HEADING_1 -> relativeSize(2f, info.start, info.end)
    MarkdownType.HEADING_2 -> relativeSize(1.5f, info.start, info.end)
    MarkdownType.HEADING_3 -> relativeSize(1.2f, info.start, info.end)
    MarkdownType.CODE -> monospace(info.start, info.end).background(Color.GRAY, info.start, info.end)
    MarkdownType.BULLET_1 -> {
    }
    MarkdownType.BULLET_2 -> {
    }
    MarkdownType.BULLET_3 -> {
    }
    MarkdownType.QUOTE -> quote(Color.GRAY, info.start, info.end)
    MarkdownType.NORMAL -> {
    }
    MarkdownType.BOLD -> bold(info.start, info.end)
    MarkdownType.ITALICS -> italic(info.start, info.end)
    MarkdownType.UNDERLINE -> underline(info.start, info.end)
    MarkdownType.INLINE_CODE -> monospace(info.start, info.end).background(Color.GRAY, info.start, info.end)
    MarkdownType.STRIKE -> strike(info.start, info.end)
  }
}

fun Spannable.setFormats(info: List<SpanInfo>) {
  info.forEach { setFormats(it) }
}