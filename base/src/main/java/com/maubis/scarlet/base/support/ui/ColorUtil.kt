package com.maubis.scarlet.base.support.ui

import android.graphics.Color
import androidx.core.graphics.ColorUtils

object ColorUtil {

  fun isLightColored(color: Int): Boolean {
    if (Color.alpha(color) < 100) {
      return true
    }
    return ColorUtils.calculateLuminance(color) > 0.4
  }

  fun darkOrDarkerColor(color: Int): Int {
    val hsl = floatArrayOf(0.0f, 0.0f, 0.0f)
    ColorUtils.RGBToHSL(Color.red(color), Color.green(color), Color.blue(color), hsl)

    val luminance = hsl[2]
    if (luminance > 0.25) {
      return luminantColor(color, 0.25f)
    }
    return luminantColor(color, luminance * 0.8f)
  }

  fun luminantColor(color: Int, luminance: Float): Int {
    val hsl = floatArrayOf(0.0f, 0.0f, 0.0f)
    ColorUtils.RGBToHSL(Color.red(color), Color.green(color), Color.blue(color), hsl)
    hsl[2] = luminance
    return ColorUtils.HSLToColor(hsl)
  }
}
