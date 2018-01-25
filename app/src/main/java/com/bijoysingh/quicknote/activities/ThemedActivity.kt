package com.bijoysingh.quicknote.activities

import android.content.Context
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.bijoysingh.quicknote.R
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
      flags = when (ThemeManager.get(this).isNightTheme()) {
        true -> flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        false -> flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
      }
      view.systemUiVisibility = flags
    }
  }

  fun getThemeColor(): Int {
    return getColor(R.color.white, R.color.material_grey_800)
  }

  fun getStatusBarColor(): Int {
    val lightColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) R.color.white else R.color.material_grey_500
    return getColor(lightColor, R.color.material_grey_800)
  }

  fun getColor(lightColorRes: Int, darkColorRes: Int): Int {
    return ThemeManager.get(this).getThemedColor(this, lightColorRes, darkColorRes)
  }

  fun tryClosingTheKeyboard() {
    try {
      val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
    } catch (exception: Exception) {
      // Do nothing
    }
  }
}