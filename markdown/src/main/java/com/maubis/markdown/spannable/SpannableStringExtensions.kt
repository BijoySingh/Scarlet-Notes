package com.maubis.markdown.spannable

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.*

fun SpannableString.color(color: String, start: Int, end: Int): SpannableString {
  this.setSpan(ForegroundColorSpan(Color.parseColor(color)), start, end, 0)
  return this
}

fun SpannableString.bold(start: Int, end: Int): SpannableString {
  this.setSpan(StyleSpan(Typeface.BOLD), start, end, 0)
  return this
}

fun SpannableString.underline(start: Int, end: Int): SpannableString {
  this.setSpan(UnderlineSpan(), start, end, 0)
  return this
}

fun SpannableString.italic(start: Int, end: Int): SpannableString {
  this.setSpan(StyleSpan(Typeface.ITALIC), start, end, 0)
  return this
}

fun SpannableString.strike(start: Int, end: Int): SpannableString {
  this.setSpan(StrikethroughSpan(), start, end, 0)
  return this
}

fun SpannableString.background(color: Int, start: Int, end: Int): SpannableString {
  this.setSpan(BackgroundColorSpan(color), start, end, 0)
  return this
}


fun SpannableString.quote(color: Int, start: Int, end: Int): SpannableString {
  this.setSpan(QuoteSpan(color), start, end, 0)
  return this
}

fun SpannableString.relativeSize(relativeSize: Float, start: Int, end: Int): SpannableString {
  this.setSpan(RelativeSizeSpan(relativeSize), start, end, 0)
  return this
}

fun SpannableString.monospace(start: Int, end: Int): SpannableString {
  this.setSpan(TypefaceSpan("monospace"), start, end, 0)
  return this
}
