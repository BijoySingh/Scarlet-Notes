package com.bijoysingh.quicknote.utils

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.v4.content.ContextCompat
import com.bijoysingh.quicknote.MaterialNotes.Companion.userPreferences
import com.bijoysingh.quicknote.R

const val KEY_APP_THEME = "KEY_APP_THEME"

// Old Theme Key, remove in future once theme is properly handled
const val KEY_NIGHT_THEME: String = "KEY_NIGHT_THEME"

class ThemeManager(context: Context) {

  var theme: Theme
  var map = HashMap<ThemeColorType, Int>()

  init {
    theme = getThemeFromStore()
    notifyUpdate(context)
  }

  fun isNightTheme() = theme.isNightTheme

  fun notifyUpdate(context: Context) {
    theme = getThemeFromStore()
    for (colorType in ThemeColorType.values()) {
      map[colorType] = load(context, colorType)
    }
  }

  fun load(context: Context, type: ThemeColorType): Int {
    val colorResource = when (type) {
      ThemeColorType.BACKGROUND -> theme.background
      ThemeColorType.STATUS_BAR -> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) theme.background
        else theme.statusBarColorFallback ?: theme.background
      }
      ThemeColorType.PRIMARY_TEXT -> theme.primaryText
      ThemeColorType.SECONDARY_TEXT -> theme.secondaryText
      ThemeColorType.TERTIARY_TEXT -> theme.tertiaryText
      ThemeColorType.HINT_TEXT -> theme.hintText
      ThemeColorType.DISABLED_TEXT -> theme.disabledText
      ThemeColorType.ACCENT_TEXT -> theme.accentText
      ThemeColorType.SECTION_HEADER -> theme.sectionHeader
      ThemeColorType.TOOLBAR_BACKGROUND -> theme.toolbarBackground
      ThemeColorType.TOOLBAR_ICON -> theme.toolbarIcon
    }
    return ContextCompat.getColor(context, colorResource)
  }

  fun get(type: ThemeColorType): Int = map[type] ?: Color.WHITE

  fun getThemedColor(context: Context, lightColor: Int, darkColor: Int): Int {
    return ContextCompat.getColor(context, if (isNightTheme()) darkColor else lightColor)
  }

  private fun getThemeFromStore(): Theme {
    val theme = userPreferences().get(KEY_APP_THEME, Theme.DARK.name)
    return Theme.valueOf(theme)
  }
}

// NOTE: These names cannot be changed
enum class Theme(
    val isNightTheme: Boolean,
    val background: Int,
    val primaryText: Int,
    val secondaryText: Int,
    val tertiaryText: Int,
    val hintText: Int,
    val disabledText: Int,
    val accentText: Int,
    val sectionHeader: Int,
    val toolbarBackground: Int,
    val toolbarIcon: Int,
    val statusBarColorFallback: Int? = null) {
  LIGHT(
      isNightTheme = false,
      background = R.color.white,
      primaryText = R.color.dark_primary_text,
      secondaryText = R.color.dark_secondary_text,
      tertiaryText = R.color.dark_tertiary_text,
      hintText = R.color.dark_hint_text,
      disabledText = R.color.material_grey_600,
      accentText = R.color.colorAccent,
      sectionHeader = R.color.material_blue_grey_500,
      toolbarBackground = R.color.material_grey_50,
      toolbarIcon = R.color.material_blue_grey_700,
      statusBarColorFallback = R.color.material_grey_500),
  DARK(
      isNightTheme = true,
      background = R.color.material_grey_800,
      primaryText = R.color.light_primary_text,
      secondaryText = R.color.light_primary_text,
      tertiaryText = R.color.light_secondary_text,
      hintText = R.color.light_hint_text,
      disabledText = R.color.material_grey_200,
      accentText = R.color.colorAccentDark,
      sectionHeader = R.color.material_blue_grey_200,
      toolbarBackground = R.color.material_grey_850,
      toolbarIcon = R.color.white),
}

enum class ThemeColorType {
  BACKGROUND,
  STATUS_BAR,
  PRIMARY_TEXT,
  SECONDARY_TEXT,
  TERTIARY_TEXT,
  HINT_TEXT,
  DISABLED_TEXT,
  ACCENT_TEXT,
  SECTION_HEADER,
  TOOLBAR_BACKGROUND,
  TOOLBAR_ICON,
}