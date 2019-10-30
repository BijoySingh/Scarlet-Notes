package com.maubis.scarlet.base.support.ui

import android.graphics.*
import android.graphics.drawable.Drawable
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme

class CircleDrawable(color: Int, showBorder: Boolean = true) : Drawable() {
  private val paint: Paint
  private val borderPaint: Paint
  private var radius = 0

  init {
    this.paint = Paint(Paint.ANTI_ALIAS_FLAG)
    this.paint.color = color

    val isNightTheme = sAppTheme.isNightTheme()
    this.borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    this.borderPaint.color = when {
      !showBorder -> Color.TRANSPARENT
      isNightTheme -> Color.LTGRAY
      else -> Color.GRAY
    }
  }

  override fun draw(canvas: Canvas) {
    val bounds = bounds
    canvas.drawCircle(bounds.centerX().toFloat(), bounds.centerY().toFloat(), radius.toFloat(), borderPaint)
    canvas.drawCircle(bounds.centerX().toFloat(), bounds.centerY().toFloat(), radius.toFloat() - 2, paint)
  }

  override fun setAlpha(alpha: Int) {
    paint.alpha = alpha
  }

  override fun setColorFilter(cf: ColorFilter?) {
    paint.colorFilter = cf
  }

  override fun getOpacity(): Int {
    return PixelFormat.TRANSLUCENT
  }

  override fun onBoundsChange(bounds: Rect) {
    super.onBoundsChange(bounds)
    radius = Math.min(bounds.width(), bounds.height()) / 2
  }
}