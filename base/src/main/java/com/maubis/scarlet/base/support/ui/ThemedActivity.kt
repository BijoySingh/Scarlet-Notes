package com.maubis.scarlet.base.support.ui

import android.content.Context
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.maubis.scarlet.base.config.CoreConfig

abstract class ThemedActivity : AppCompatActivity() {

  abstract fun notifyThemeChange()

  fun setSystemTheme(color: Int = getStatusBarColor()) {
    setStatusBarColor(color)
    setStatusBarTextColor()
  }

  fun setStatusBarColor(color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      window.statusBarColor = color
    }
  }

  fun setStatusBarTextColor() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      val view = window.decorView
      var flags = view.systemUiVisibility
      flags = when (CoreConfig.instance.themeController().isNightTheme()) {
        true -> flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        false -> flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
      }
      view.systemUiVisibility = flags
    }
  }

  fun getThemeColor(): Int = CoreConfig.instance.themeController().get(ThemeColorType.BACKGROUND)

  fun getStatusBarColor(): Int = CoreConfig.instance.themeController().get(ThemeColorType.STATUS_BAR)

  fun tryClosingTheKeyboard() {
    try {
      val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
    } catch (exception: Exception) {
      // Do nothing
    }
  }

  fun tryOpeningTheKeyboard() {
    try {
      val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    } catch (exception: Exception) {
      // Do nothing
    }
  }
}