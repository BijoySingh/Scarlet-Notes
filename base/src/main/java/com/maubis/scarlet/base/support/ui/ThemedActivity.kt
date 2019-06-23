package com.maubis.scarlet.base.support.ui

import android.content.Context
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.maubis.scarlet.base.BuildConfig
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.settings.sheet.sInternalEnableFullScreen
import com.maubis.scarlet.base.support.utils.maybeThrow

abstract class ThemedActivity : AppCompatActivity() {

  abstract fun notifyThemeChange()

  fun setSystemTheme(color: Int = getStatusBarColor()) {
    setStatusBarColor(color)
    setStatusBarTextColor()
  }

  override fun onResume() {
    super.onResume()
    fullScreenView()
  }

  fun fullScreenView() {
    if (!sInternalEnableFullScreen) {
      return
    }

    window.decorView.systemUiVisibility = (
        View.SYSTEM_UI_FLAG_IMMERSIVE
        // Set the content to appear under the system bars so that the
        // content doesn't resize when the system bars hide and show.
        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        // Hide the nav bar and status bar
        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        or View.SYSTEM_UI_FLAG_FULLSCREEN)
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
      flags = when (ApplicationBase.instance.themeController().isNightTheme()) {
        true -> flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        false -> flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
      }
      view.systemUiVisibility = flags
    }
  }

  fun getThemeColor(): Int = ApplicationBase.instance.themeController().get(ThemeColorType.BACKGROUND)

  fun getStatusBarColor(): Int = ApplicationBase.instance.themeController().get(ThemeColorType.STATUS_BAR)

  fun tryClosingTheKeyboard() {
    try {
      val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
    } catch (exception: Exception) {
      maybeThrow(this, exception)
    }
  }

  fun tryOpeningTheKeyboard() {
    try {
      val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    } catch (exception: Exception) {
      maybeThrow(this, exception)
    }
  }
}