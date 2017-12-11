package com.bijoysingh.quicknote.utils

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable

class CircleDrawable(color: Int) : Drawable() {
  private val paint: Paint
  private var radius = 0

  init {
    this.paint = Paint(Paint.ANTI_ALIAS_FLAG)
    this.paint.color = color
  }

  override fun draw(canvas: Canvas) {
    val bounds = bounds
    canvas.drawCircle(bounds.centerX().toFloat(), bounds.centerY().toFloat(), radius.toFloat(), paint)
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