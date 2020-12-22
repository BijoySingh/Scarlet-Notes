package com.maubis.markdown.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.Layout
import android.text.TextPaint
import android.text.style.LeadingMarginSpan
import android.text.style.MetricAffectingSpan
import com.maubis.markdown.MarkdownConfig.Companion.config

class CodeSegmentSpan : MetricAffectingSpan(), LeadingMarginSpan, ICustomSpan {
  private val rect = Rect()
  private val paint = Paint()

  override fun updateMeasureState(paint: TextPaint) {
    setTextColor(paint)
  }

  override fun updateDrawState(paint: TextPaint) {
    setTextColor(paint)
  }

  private fun setTextColor(paint: TextPaint) {
    paint.color = paint.color
    paint.alpha = 225
  }

  override fun getLeadingMargin(first: Boolean): Int {
    return config.spanConfig.codeBlockLeadingMargin
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
    paint.style = Paint.Style.FILL
    paint.color = config.spanConfig.codeBackgroundColor

    val leftPosition = when {
      dir > 0 -> x
      else -> x - canvas.width
    }
    val rightPosition = when {
      dir > 0 -> canvas.width
      else -> x
    }

    rect.set(leftPosition, top, rightPosition, bottom)
    canvas.drawRect(rect, paint)
  }
}
