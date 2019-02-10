package com.maubis.scarlet.base.support.ui

import android.graphics.Color
import android.support.v4.graphics.ColorUtils

object ColorUtil {

  fun isLightColored(color: Int): Boolean {
    return ColorUtils.calculateLuminance(color) > 0.4
  }

  fun darkerColor(color: Int): Int {
    return luminantColor(color, 0.2f)
  }

  fun darkerOrSlightlyDarkerColor(color: Int): Int {
    val hsl = floatArrayOf(0.0f, 0.0f, 0.0f)
    ColorUtils.RGBToHSL(Color.red(color), Color.green(color), Color.blue(color), hsl)

    val luminance = hsl[2]
    if (luminance > 0.2) {
      return luminantColor(color, 0.2f)
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
