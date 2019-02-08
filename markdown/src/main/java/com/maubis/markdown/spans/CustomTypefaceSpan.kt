package com.maubis.markdown.spans

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.TypefaceSpan

class CustomTypefaceSpan(val tface: Typeface) : TypefaceSpan("san-serif"), ICustomSpan {

  override fun updateDrawState(paint: TextPaint) {
    applyTypeFace(paint, tface)
  }

  override fun updateMeasureState(paint: TextPaint) {
    applyTypeFace(paint, tface)
  }

  private fun applyTypeFace(paint: Paint, typeface: Typeface) {
    val oldStyle = paint.typeface?.style ?: 0
    val isFake = oldStyle and tface.style.inv()
    if (isFake and Typeface.BOLD != 0) {
      paint.isFakeBoldText = true
    }

    if (isFake and Typeface.ITALIC != 0) {
      paint.textSkewX = -0.25f
    }

    paint.typeface = tface
  }
}