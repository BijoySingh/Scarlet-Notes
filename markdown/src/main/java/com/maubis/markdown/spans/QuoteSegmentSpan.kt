package com.maubis.markdown.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.Layout
import android.text.style.LeadingMarginSpan
import com.maubis.markdown.MarkdownConfig.Companion.config

class QuoteSegmentSpan : LeadingMarginSpan, ICustomSpan {

  private val rect = Rect()
  private val paint = Paint()

  override fun getLeadingMargin(first: Boolean): Int {
    return config.spanConfig.quoteBlockLeadingMargin
  }

  override fun drawLeadingMargin(
      canvas: Canvas,
      p: Paint,
      x: Int,
      dir: Int,
      top: Int,
      baseline: Int,
      bottom: Int,
      text: CharSequence,
      start: Int,
      end: Int,
      first: Boolean,
      layout: Layout) {
    paint.set(p)
    paint.style = Paint.Style.FILL
    paint.color = config.spanConfig.quoteColor
    paint.typeface = config.spanConfig.textTypeface

    val startPosition = x + dir * config.spanConfig.quoteWidth
    val endPosition = startPosition + dir * config.spanConfig.quoteWidth

    rect.set(Math.min(startPosition, endPosition), top, Math.max(startPosition, endPosition), bottom)
    canvas.drawRect(rect, paint)
  }
}
