package com.maubis.scarlet.base.support.ui

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.support.v4.content.ContextCompat
import com.github.bijoysingh.starter.util.DimensionManager
import com.maubis.markdown.MarkdownConfig
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppPreferences
import com.maubis.scarlet.base.support.utils.OsVersionUtils
import com.maubis.scarlet.base.support.utils.throwOrReturn
import java.lang.ref.WeakReference

var sThemeLabel: String
  get() = sAppPreferences.get("KEY_APP_THEME", Theme.DARK.name)
  set(value) = sAppPreferences.put("KEY_APP_THEME", value)

var sThemeIsAutomatic: Boolean
  get() = sAppPreferences.get("automatic_theme", false)
  set(value) = sAppPreferences.put("automatic_theme", value)

var sThemeDarkenNoteColor: Boolean
  get() = sAppPreferences.get("darken_note_color", false)
  set(value) = sAppPreferences.put("darken_note_color", value)


fun setThemeFromSystem(context: Context) {
  val configuration = context.resources.configuration
  val systemBasedTheme = when (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
    Configuration.UI_MODE_NIGHT_NO -> Theme.LIGHT.name
    Configuration.UI_MODE_NIGHT_YES -> Theme.VERY_DARK.name
    else -> Theme.VERY_DARK.name
  }
  if (systemBasedTheme === sThemeLabel) {
    return
  }
  sThemeLabel = systemBasedTheme
}

// Old Theme Key, remove in future once theme is properly handled
const val KEY_NIGHT_THEME: String = "KEY_NIGHT_THEME"

class ThemeManager : IThemeManager {
  lateinit var theme: Theme

  var listeners = HashSet<WeakReference<IThemeChangeListener>>()
  var map = HashMap<ThemeColorType, Int>()
  override fun setup(context: Context) {
    theme = getThemeFromStore()
    notifyChange(context)
  }

  override fun get(): Theme {
    return theme
  }

  override fun register(listener: IThemeChangeListener) {
    listeners.add(WeakReference(listener))
  }

  override fun isNightTheme() = theme.isNightTheme

  override fun get(type: ThemeColorType): Int = map[type] ?: Color.WHITE

  override fun get(context: Context, lightColor: Int, darkColor: Int): Int {
    return ContextCompat.getColor(context, if (isNightTheme()) darkColor else lightColor)
  }

  override fun get(context: Context, theme: Theme, type: ThemeColorType): Int {
    return load(context, theme, type)
  }

  override fun notifyChange(context: Context) {
    theme = getThemeFromStore()
    for (colorType in ThemeColorType.values()) {
      map[colorType] = load(context, colorType)
    }

    if (map[ThemeColorType.TOOLBAR_BACKGROUND] == map[ThemeColorType.BACKGROUND]) {
      map[ThemeColorType.TOOLBAR_BACKGROUND] = ColorUtil.darkOrDarkerColor(
        map[ThemeColorType.TOOLBAR_BACKGROUND]
          ?: 0)
    }

    setMarkdownConfig(context)
    for (reference in listeners) {
      val listener = reference.get()
      if (listener !== null) {
        listener.onChange(theme)
      }
    }
  }

  private fun setMarkdownConfig(context: Context) {
    MarkdownConfig.config.spanConfig.codeTextColor = get(ThemeColorType.SECONDARY_TEXT)
    MarkdownConfig.config.spanConfig.codeBackgroundColor = get(context, R.color.code_light, R.color.code_dark)
    MarkdownConfig.config.spanConfig.codeBlockLeadingMargin = DimensionManager.dpToPixels(context, 8)
    MarkdownConfig.config.spanConfig.quoteColor = MarkdownConfig.config.spanConfig.codeBackgroundColor
    MarkdownConfig.config.spanConfig.separatorColor = MarkdownConfig.config.spanConfig.codeBackgroundColor
    MarkdownConfig.config.spanConfig.quoteWidth = DimensionManager.dpToPixels(context, 4)
    MarkdownConfig.config.spanConfig.separatorWidth = DimensionManager.dpToPixels(context, 2)
    MarkdownConfig.config.spanConfig.quoteBlockLeadingMargin = DimensionManager.dpToPixels(context, 8)
  }

  private fun load(context: Context, type: ThemeColorType): Int {
    return load(context, theme, type)
  }

  private fun load(context: Context, theme: Theme, type: ThemeColorType): Int {
    val colorResource = when (type) {
      ThemeColorType.BACKGROUND -> theme.background
      ThemeColorType.STATUS_BAR -> {
        if (OsVersionUtils.canSetStatusBarTheme()) theme.background
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

  companion object {
    fun getThemeByBackgroundColor(context: Context, color: Int): Theme {
      for (theme in Theme.values()) {
        if (color == ContextCompat.getColor(context, theme.background)) {
          return theme
        }
      }
      return Theme.DARK
    }

    fun getThemeFromStore(): Theme {
      return try {
        Theme.valueOf(sThemeLabel)
      } catch (exception: Exception) {
        throwOrReturn(exception, Theme.DARK)
      }
    }
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
    sectionHeader = R.color.material_blue_grey_600,
    toolbarBackground = R.color.material_grey_50,
    toolbarIcon = R.color.dark_secondary_text,
    statusBarColorFallback = R.color.material_grey_500),
  OFF_WHITE(
    isNightTheme = false,
    background = R.color.bg_off_white,
    primaryText = R.color.dark_primary_text,
    secondaryText = R.color.dark_secondary_text,
    tertiaryText = R.color.dark_tertiary_text,
    hintText = R.color.dark_hint_text,
    disabledText = R.color.material_grey_600,
    accentText = R.color.colorAccent,
    sectionHeader = R.color.material_blue_grey_700,
    toolbarBackground = R.color.bg_off_white_dark,
    toolbarIcon = R.color.dark_secondary_text,
    statusBarColorFallback = R.color.bg_off_white_darkest),
  PEACH(
    isNightTheme = false,
    background = R.color.bg_peach,
    primaryText = R.color.dark_primary_text,
    secondaryText = R.color.dark_secondary_text,
    tertiaryText = R.color.dark_tertiary_text,
    hintText = R.color.dark_hint_text,
    disabledText = R.color.material_grey_600,
    accentText = R.color.colorAccent,
    sectionHeader = R.color.material_blue_grey_700,
    toolbarBackground = R.color.bg_peach_dark,
    toolbarIcon = R.color.dark_secondary_text,
    statusBarColorFallback = R.color.bg_peach_darkest),
  ROSE(
    isNightTheme = false,
    background = R.color.app_theme_rose,
    primaryText = R.color.dark_primary_text,
    secondaryText = R.color.dark_secondary_text,
    tertiaryText = R.color.dark_tertiary_text,
    hintText = R.color.dark_hint_text,
    disabledText = R.color.material_grey_600,
    accentText = R.color.colorAccent,
    sectionHeader = R.color.material_blue_grey_700,
    toolbarBackground = R.color.app_theme_rose_dark,
    toolbarIcon = R.color.dark_secondary_text,
    statusBarColorFallback = R.color.app_theme_rose_dark),
  TEAL(
    isNightTheme = true,
    background = R.color.app_theme_oceanic,
    primaryText = R.color.light_primary_text,
    secondaryText = R.color.light_primary_text,
    tertiaryText = R.color.light_secondary_text,
    hintText = R.color.light_hint_text,
    disabledText = R.color.material_grey_200,
    accentText = R.color.material_pink_accent_100,
    sectionHeader = R.color.material_blue_grey_200,
    toolbarBackground = R.color.app_theme_oceanic,
    toolbarIcon = R.color.white),
  VIOLET(
    isNightTheme = true,
    background = R.color.app_theme_violet,
    primaryText = R.color.light_primary_text,
    secondaryText = R.color.light_primary_text,
    tertiaryText = R.color.light_secondary_text,
    hintText = R.color.light_hint_text,
    disabledText = R.color.material_grey_200,
    accentText = R.color.material_pink_accent_100,
    sectionHeader = R.color.material_blue_grey_200,
    toolbarBackground = R.color.app_theme_violet,
    toolbarIcon = R.color.white),
  HONEYSUCKLE(
    isNightTheme = true,
    background = R.color.app_theme_honeysuckle,
    primaryText = R.color.light_primary_text,
    secondaryText = R.color.light_primary_text,
    tertiaryText = R.color.light_secondary_text,
    hintText = R.color.light_hint_text,
    disabledText = R.color.material_grey_200,
    accentText = R.color.material_yellow_accent_100,
    sectionHeader = R.color.material_blue_grey_200,
    toolbarBackground = R.color.app_theme_honeysuckle,
    toolbarIcon = R.color.white),
  BROWN(
    isNightTheme = true,
    background = R.color.material_brown_800,
    primaryText = R.color.light_primary_text,
    secondaryText = R.color.light_primary_text,
    tertiaryText = R.color.light_secondary_text,
    hintText = R.color.light_hint_text,
    disabledText = R.color.material_grey_200,
    accentText = R.color.material_pink_accent_100,
    sectionHeader = R.color.material_blue_grey_200,
    toolbarBackground = R.color.material_brown_900,
    toolbarIcon = R.color.white),
  BLUE_GRAY(
    isNightTheme = true,
    background = R.color.material_blue_grey_900,
    primaryText = R.color.light_primary_text,
    secondaryText = R.color.light_primary_text,
    tertiaryText = R.color.light_secondary_text,
    hintText = R.color.light_hint_text,
    disabledText = R.color.material_grey_200,
    accentText = R.color.material_pink_accent_100,
    sectionHeader = R.color.material_blue_grey_200,
    toolbarBackground = R.color.material_blue_grey_900,
    toolbarIcon = R.color.white),
  DARK(
    isNightTheme = true,
    background = R.color.material_grey_850,
    primaryText = R.color.light_primary_text,
    secondaryText = R.color.light_primary_text,
    tertiaryText = R.color.light_secondary_text,
    hintText = R.color.light_hint_text,
    disabledText = R.color.material_grey_200,
    accentText = R.color.material_pink_accent_100,
    sectionHeader = R.color.material_blue_grey_200,
    toolbarBackground = R.color.material_grey_900,
    toolbarIcon = R.color.white),
  VERY_DARK(
    isNightTheme = true,
    background = R.color.material_grey_900,
    primaryText = R.color.light_primary_text,
    secondaryText = R.color.light_primary_text,
    tertiaryText = R.color.light_secondary_text,
    hintText = R.color.light_hint_text,
    disabledText = R.color.material_grey_200,
    accentText = R.color.material_pink_accent_100,
    sectionHeader = R.color.material_blue_grey_200,
    toolbarBackground = R.color.material_grey_900,
    toolbarIcon = R.color.white),
  BLACK(
    isNightTheme = true,
    background = R.color.black,
    primaryText = R.color.light_primary_text,
    secondaryText = R.color.light_primary_text,
    tertiaryText = R.color.light_secondary_text,
    hintText = R.color.light_hint_text,
    disabledText = R.color.material_grey_200,
    accentText = R.color.material_pink_accent_100,
    sectionHeader = R.color.material_blue_grey_200,
    toolbarBackground = R.color.black,
    toolbarIcon = R.color.white),
}