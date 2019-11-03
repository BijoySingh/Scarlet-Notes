package com.maubis.markdown.spans

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import com.maubis.markdown.MarkdownConfig.Companion.config

class CodeSpan : MetricAffectingSpan(), ICustomSpan {
  override fun updateMeasureState(paint: TextPaint) {
    setTextColor(paint)
  }

  override fun updateDrawState(paint: TextPaint) {
    setTextColor(paint)
    paint.bgColor = config.spanConfig.codeBackgroundColor
    paint.typeface = config.spanConfig.codeTypeface
    paint.textSize = paint.textSize * 0.87f
  }

  private fun setTextColor(paint: TextPaint) {
    paint.color = paint.color
    paint.alpha = 225
  }
}
