package com.bijoysingh.quicknote.activities

import android.content.Context
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.bijoysingh.quicknote.utils.ThemeManager

abstract class ThemedActivity : AppCompatActivity() {

  abstract fun notifyThemeChange()

  fun setSystemTheme() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      window.statusBarColor = getStatusBarColor()
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      val view = window.decorView
      var flags = view.systemUiVisibility
      flags = when (getAppTheme().isNightTheme()) {
        true -> flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        false -> flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
      }
      view.systemUiVisibility = flags
    }
  }

  fun getAppTheme(): ThemeManager = ThemeManager.get(this)

  fun getThemeColor(): Int = getAppTheme().get(this, ThemeColorType.BACKGROUND)

  fun getStatusBarColor(): Int = getAppTheme().get(this, ThemeColorType.STATUS_BAR)

  fun getColor(lightColorRes: Int, darkColorRes: Int): Int =
      getAppTheme().getThemedColor(this, lightColorRes, darkColorRes)

  fun tryClosingTheKeyboard() {
    try {
      val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
    } catch (exception: Exception) {
      // Do nothing
    }
  }
}