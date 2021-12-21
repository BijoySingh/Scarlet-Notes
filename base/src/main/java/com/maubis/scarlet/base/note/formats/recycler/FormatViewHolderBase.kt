package com.maubis.scarlet.base.note.formats.recycler

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.github.bijoysingh.starter.recyclerview.RecyclerViewHolder
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTypeface
import com.maubis.scarlet.base.core.format.Format
import com.maubis.scarlet.base.core.format.FormatType
import com.maubis.scarlet.base.note.creation.activity.INTENT_KEY_NOTE_ID
import com.maubis.scarlet.base.note.creation.activity.ViewAdvancedNoteActivity
import com.maubis.scarlet.base.note.creation.sheet.sNoteDefaultColor
import com.maubis.scarlet.base.settings.sheet.STORE_KEY_TEXT_SIZE
import com.maubis.scarlet.base.settings.sheet.SettingsOptionsBottomSheet
import com.maubis.scarlet.base.settings.sheet.TEXT_SIZE_DEFAULT
import com.maubis.scarlet.base.settings.sheet.sUIUseNoteColorAsBackground
import com.maubis.scarlet.base.support.ui.ColorUtil
import com.maubis.scarlet.base.support.ui.Theme
import com.maubis.scarlet.base.support.ui.ThemeColorType

const val KEY_EDITABLE = "KEY_EDITABLE"
const val KEY_NOTE_COLOR = "KEY_NOTE_COLOR"

data class FormatViewHolderConfig(
  val editable: Boolean,
  val isMarkdownEnabled: Boolean,
  val fontSize: Float,
  val backgroundColor: Int,
  val secondaryTextColor: Int,
  val tertiaryTextColor: Int,
  val iconColor: Int,
  val hintTextColor: Int,
  val accentColor: Int,
  val noteUUID: String,
  val typeface: Typeface,
  val typefaceStyle: Int)

abstract class FormatViewHolderBase(context: Context, view: View) : RecyclerViewHolder<Format>(context, view) {

  protected val activity: ViewAdvancedNoteActivity = context as ViewAdvancedNoteActivity

  override fun populate(data: Format, extra: Bundle?) {
    val noteColor: Int = extra?.getInt(KEY_NOTE_COLOR) ?: sNoteDefaultColor
    val secondaryTextColor: Int
    val tertiaryTextColor: Int
    val iconColor: Int
    val hintTextColor: Int
    val isLightBackground = ColorUtil.isLightColored(noteColor)
    val linkColor: Int
    when {
      !sUIUseNoteColorAsBackground -> {
        secondaryTextColor = sAppTheme.get(ThemeColorType.SECONDARY_TEXT)
        tertiaryTextColor = sAppTheme.get(ThemeColorType.TERTIARY_TEXT)
        iconColor = sAppTheme.get(ThemeColorType.TOOLBAR_ICON)
        hintTextColor = sAppTheme.get(ThemeColorType.HINT_TEXT)
        linkColor = sAppTheme.get(ThemeColorType.ACCENT_TEXT)
      }
      isLightBackground -> {
        secondaryTextColor = sAppTheme.get(context, Theme.LIGHT, ThemeColorType.SECONDARY_TEXT)
        tertiaryTextColor = sAppTheme.get(context, Theme.LIGHT, ThemeColorType.TERTIARY_TEXT)
        iconColor = sAppTheme.get(context, Theme.LIGHT, ThemeColorType.TOOLBAR_ICON)
        hintTextColor = sAppTheme.get(context, Theme.LIGHT, ThemeColorType.HINT_TEXT)
        linkColor = ContextCompat.getColor(context, R.color.colorAccentYellowLight)
      }
      else -> {
        secondaryTextColor = sAppTheme.get(context, Theme.DARK, ThemeColorType.SECONDARY_TEXT)
        tertiaryTextColor = sAppTheme.get(context, Theme.DARK, ThemeColorType.TERTIARY_TEXT)
        iconColor = sAppTheme.get(context, Theme.DARK, ThemeColorType.TOOLBAR_ICON)
        hintTextColor = sAppTheme.get(context, Theme.DARK, ThemeColorType.HINT_TEXT)
        linkColor = ContextCompat.getColor(context, R.color.colorAccentYellowDark)
      }
    }
    val
      config = FormatViewHolderConfig(
      editable = !(extra != null
        && extra.containsKey(KEY_EDITABLE)
        && !extra.getBoolean(KEY_EDITABLE)),
      isMarkdownEnabled = (extra == null
        || extra.getBoolean(SettingsOptionsBottomSheet.KEY_MARKDOWN_ENABLED, true)
        || data.forcedMarkdown) && (data.formatType != FormatType.CODE),
      fontSize = {
        val fontSize = extra?.getInt(STORE_KEY_TEXT_SIZE, TEXT_SIZE_DEFAULT)
          ?: TEXT_SIZE_DEFAULT
        when (data.formatType) {
          FormatType.HEADING -> fontSize.toFloat() + 4
          FormatType.SUB_HEADING -> fontSize.toFloat() + 2
          else -> fontSize.toFloat()
        }
      }(),
      backgroundColor = when (data.formatType) {
        FormatType.CODE, FormatType.IMAGE -> sAppTheme.get(context, R.color.code_light, R.color.code_dark)
        else -> ContextCompat.getColor(context, R.color.transparent)
      },
      secondaryTextColor = secondaryTextColor,
      tertiaryTextColor = tertiaryTextColor,
      iconColor = iconColor,
      hintTextColor = hintTextColor,
      accentColor = linkColor,
      noteUUID = extra?.getString(INTENT_KEY_NOTE_ID) ?: "default",
      typeface = when (data.formatType) {
        FormatType.HEADING -> sAppTypeface.subHeading()
        FormatType.SUB_HEADING -> sAppTypeface.title()
        FormatType.HEADING_3 -> sAppTypeface.title()
        FormatType.CODE -> sAppTypeface.code()
        else -> sAppTypeface.text()
      },
      typefaceStyle = when (data.formatType) {
        FormatType.HEADING, FormatType.SUB_HEADING, FormatType.HEADING_3 -> Typeface.BOLD
        else -> Typeface.NORMAL
      })

    populate(data, config)

  }

  abstract fun populate(data: Format, config: FormatViewHolderConfig)
}
