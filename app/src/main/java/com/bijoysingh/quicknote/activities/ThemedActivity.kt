package com.bijoysingh.quicknote.activities

import android.app.Activity
import android.content.Context
import android.os.Build
import android.support.annotation.IdRes
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.bijoysingh.quicknote.MaterialNotes.Companion.appTheme
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.bijoysingh.quicknote.utils.ThemeManager

abstract class ThemedActivity : AppCompatActivity() {

  abstract fun notifyThemeChange()

  fun setSystemTheme() {
    setStatusBarColor(getStatusBarColor())
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
      flags = when (appTheme().isNightTheme()) {
        true -> flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        false -> flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
      }
      view.systemUiVisibility = flags
    }
  }

  fun getThemeColor(): Int = appTheme().get(ThemeColorType.BACKGROUND)

  fun getStatusBarColor(): Int = appTheme().get(ThemeColorType.STATUS_BAR)

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