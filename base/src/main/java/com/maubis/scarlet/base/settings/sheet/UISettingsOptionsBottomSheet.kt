package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import com.facebook.litho.ComponentContext
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.MainActivityActions
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppPreferences
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.main.sheets.InstallProUpsellBottomSheet
import com.maubis.scarlet.base.performAction
import com.maubis.scarlet.base.settings.sheet.SortingOptionsBottomSheet.Companion.getSortingState
import com.maubis.scarlet.base.settings.sheet.SortingOptionsBottomSheet.Companion.getSortingTechniqueLabel
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.utils.FlavorUtils

class UISettingsOptionsBottomSheet : LithoOptionBottomSheet() {
  override fun title(): Int = R.string.home_option_ui_experience

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val activity = componentContext.androidContext as MainActivity
    val options = ArrayList<LithoOptionsItem>()
    options.add(LithoOptionsItem(
      title = R.string.home_option_theme_color,
      subtitle = R.string.home_option_theme_color_subtitle,
      icon = if (sAppTheme.isNightTheme()) R.drawable.night_mode_white_48dp else R.drawable.ic_action_day_mode,
      listener = {
        activity.performAction(MainActivityActions.COLOR_PICKER)
      }
    ))
    val isTablet = resources.getBoolean(R.bool.is_tablet)
    options.add(
      LithoOptionsItem(
        title = R.string.home_option_enable_list_view,
        subtitle = R.string.home_option_enable_list_view_subtitle,
        icon = R.drawable.ic_action_list,
        listener = {
          useGridView = false
          activity.notifyAdapterExtraChanged()
          reset(activity, dialog)
        },
        visible = !isTablet && useGridView
      ))
    options.add(
      LithoOptionsItem(
        title = R.string.home_option_enable_grid_view,
        subtitle = R.string.home_option_enable_grid_view_subtitle,
        icon = R.drawable.ic_action_grid,
        listener = {
          useGridView = true
          activity.notifyAdapterExtraChanged()
          reset(activity, dialog)
        },
        visible = !isTablet && !useGridView
      ))
    options.add(LithoOptionsItem(
      title = R.string.home_option_order_notes,
      subtitle = getSortingTechniqueLabel(getSortingState()),
      icon = R.drawable.ic_sort,
      listener = {
        SortingOptionsBottomSheet.openSheet(activity, { activity.setupData() })
        reset(activity, dialog)
      }
    ))
    options.add(
      LithoOptionsItem(
        title = R.string.note_option_font_size,
        subtitle = 0,
        content = activity.getString(R.string.note_option_font_size_subtitle, sEditorTextSize),
        icon = R.drawable.ic_title_white_48dp,
        listener = {
          when {
            FlavorUtils.isLite() -> openSheet(activity, InstallProUpsellBottomSheet())
            else -> openSheet(activity, FontSizeBottomSheet())
          }
          reset(activity, dialog)
        },
        actionIcon = if (FlavorUtils.isLite()) R.drawable.ic_rating else 0
      ))
    options.add(LithoOptionsItem(
      title = R.string.note_option_number_lines,
      subtitle = 0,
      content = activity.getString(R.string.note_option_number_lines_subtitle, sNoteItemLineCount),
      icon = R.drawable.ic_action_list,
      listener = {
        openSheet(activity, LineCountBottomSheet())
      }
    ))
    options.add(
      LithoOptionsItem(
        title = R.string.ui_options_note_background_color,
        subtitle = when (useNoteColorAsBackground) {
          true -> R.string.ui_options_note_background_color_settings_note
          false -> R.string.ui_options_note_background_color_settings_theme
        },
        icon = R.drawable.ic_action_color,
        listener = {
          if (FlavorUtils.isLite()) {
            openSheet(activity, InstallProUpsellBottomSheet())
            return@LithoOptionsItem
          }

          useNoteColorAsBackground = !useNoteColorAsBackground
          reset(activity, dialog)
        },
        actionIcon = if (FlavorUtils.isLite()) R.drawable.ic_rating else 0
      ))
    options.add(
      LithoOptionsItem(
        title = R.string.markdown_sheet_home_markdown_support,
        subtitle = R.string.markdown_sheet_home_markdown_support_subtitle,
        icon = R.drawable.ic_markdown_logo,
        listener = {
          sMarkdownEnabledHome = !sMarkdownEnabledHome
          reset(activity, dialog)
        },
        isSelectable = true,
        selected = sMarkdownEnabledHome
      ))
    return options
  }

  companion object {

    const val KEY_LIST_VIEW = "KEY_LIST_VIEW"
    const val KEY_NOTE_VIEWER_BG_COLOR = "KEY_NOTE_VIEWER_BG_COLOR"
    const val KEY_MARKDOWN_HOME_ENABLED = "KEY_MARKDOWN_HOME_ENABLED"

    fun openSheet(activity: MainActivity) {
      val sheet = UISettingsOptionsBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }

    var useGridView: Boolean
      get() = sAppPreferences.get(KEY_LIST_VIEW, true)
      set(isGrid) = sAppPreferences.put(KEY_LIST_VIEW, isGrid)

    var useNoteColorAsBackground: Boolean
      get() = sAppPreferences.get(KEY_NOTE_VIEWER_BG_COLOR, false)
      set(value) = sAppPreferences.put(KEY_NOTE_VIEWER_BG_COLOR, value)

    var sMarkdownEnabledHome: Boolean
      get() = sAppPreferences.get(KEY_MARKDOWN_HOME_ENABLED, true)
      set(value) = sAppPreferences.put(KEY_MARKDOWN_HOME_ENABLED, value)
  }
}