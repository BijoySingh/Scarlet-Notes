package com.bijoysingh.quicknote.activities

import android.os.Build
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import com.bijoysingh.quicknote.R

abstract class ThemedActivity : AppCompatActivity() {

  var isNightMode = false

  abstract fun notifyNightModeChange()

  fun toggleNightMode() {
    requestSetNightMode(!isNightMode)
  }

  fun requestSetNightMode(nightMode: Boolean) {
    isNightMode = nightMode
    notifyNightModeChange()
  }

  fun setSystemTheme() {
    val backgroundColor = getThemeColor()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      window.statusBarColor = backgroundColor
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      val view = window.decorView
      var flags = view.systemUiVisibility
      if (isNightMode) flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
      else flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
      view.systemUiVisibility = flags
    }
  }

  fun getThemeColor(): Int {
    return getColor(R.color.white, R.color.material_grey_800)
  }

  fun getColor(lightColorRes: Int, darkColorRes: Int): Int {
    return ContextCompat.getColor(
        this,
        when (isNightMode) {
          true -> darkColorRes
          else -> lightColorRes
        })
  }

  companion object {
    val KEY_NIGHT_THEME: String = "KEY_NIGHT_THEME"

    fun getKey() = KEY_NIGHT_THEME
  }
}