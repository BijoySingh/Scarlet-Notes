package com.bijoysingh.quicknote.utils;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class CircleDrawable extends Drawable {
  private final Paint mPaint;
  private int mRadius = 0;

  public CircleDrawable(final int color) {
    this.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    this.mPaint.setColor(color);
  }

  @Override
  public void draw(final Canvas canvas) {
    final Rect bounds = getBounds();
    canvas.drawCircle(bounds.centerX(), bounds.centerY(), mRadius, mPaint);
  }

  @Override
  public void setAlpha(final int alpha) {
    mPaint.setAlpha(alpha);
  }

  @Override
  public void setColorFilter(final ColorFilter cf) {
    mPaint.setColorFilter(cf);
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  protected void onBoundsChange(final Rect bounds) {
    super.onBoundsChange(bounds);
    mRadius = Math.min(bounds.width(), bounds.height()) / 2;
  }
}