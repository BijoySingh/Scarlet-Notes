package com.bijoysingh.quicknote.utils

import android.content.Context
import android.support.v4.content.ContextCompat
import com.bijoysingh.quicknote.R
import com.github.bijoysingh.starter.prefs.DataStore

const val KEY_APP_THEME = "KEY_APP_THEME"

// Old Theme Key, remove in future once theme is properly handled
const val KEY_NIGHT_THEME: String = "KEY_NIGHT_THEME"

class ThemeManager(context: Context) {

  var theme: Theme

  init {
    theme = getThemeFromDataStore(context)
  }

  fun isNightTheme() = theme.isNightTheme

  fun notifyUpdate(context: Context) {
    theme = getThemeFromDataStore(context)
  }

  fun getThemedColor(context: Context, lightColor: Int, darkColor: Int): Int {
    return ContextCompat.getColor(context, if (isNightTheme()) darkColor else lightColor)
  }

  private fun getThemeFromDataStore(context: Context): Theme {
    val dataStore = DataStore.get(context)
    val theme = dataStore.get(KEY_APP_THEME, Theme.LIGHT.name)
    return Theme.valueOf(theme)
  }

  companion object {
    var themeManager: ThemeManager? = null

    fun get(context: Context): ThemeManager {
      if (themeManager === null) {
        themeManager = ThemeManager(context)
      }
      return themeManager!!
    }
  }
}

// NOTE: These names cannot be changed
enum class Theme(
    val isNightTheme: Boolean) {
  LIGHT(false),
  DARK(true),
}
