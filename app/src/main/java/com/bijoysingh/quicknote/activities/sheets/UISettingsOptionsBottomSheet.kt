package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.view.View
import com.bijoysingh.quicknote.MaterialNotes.Companion.appTheme
import com.bijoysingh.quicknote.MaterialNotes.Companion.userPreferences
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.activities.sheets.LineCountBottomSheet.Companion.getDefaultLineCount
import com.bijoysingh.quicknote.activities.sheets.SortingOptionsBottomSheet.Companion.getSortingState
import com.bijoysingh.quicknote.activities.sheets.TextSizeBottomSheet.Companion.getDefaultTextSize
import com.bijoysingh.quicknote.items.OptionsItem
import com.bijoysingh.quicknote.utils.Flavor
import com.bijoysingh.quicknote.utils.KEY_APP_THEME
import com.bijoysingh.quicknote.utils.Theme
import com.bijoysingh.quicknote.utils.getAppFlavor

class UISettingsOptionsBottomSheet : OptionItemBottomSheetBase() {

  override fun setupViewWithDialog(dialog: Dialog) {
    setOptions(dialog, getOptions())
  }

  private fun getOptions(): List<OptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.home_option_enable_night_mode,
        subtitle = R.string.home_option_enable_night_mode_subtitle,
        icon = R.drawable.night_mode_white_48dp,
        listener = View.OnClickListener {
          userPreferences().put(KEY_APP_THEME, Theme.DARK.name)
          appTheme().notifyUpdate(activity)
          activity.notifyThemeChange()
          dismiss()
        },
        visible = !isNightMode()
    ))
    options.add(OptionsItem(
        title = R.string.home_option_enable_day_mode,
        subtitle = R.string.home_option_enable_day_mode_subtitle,
        icon = R.drawable.ic_action_day_mode,
        listener = View.OnClickListener {
          userPreferences().put(KEY_APP_THEME, Theme.LIGHT.name)
          appTheme().notifyUpdate(activity)
          activity.notifyThemeChange()
          dismiss()
        },
        visible = isNightMode()
    ))
    val isTablet = resources.getBoolean(R.bool.is_tablet)
    options.add(OptionsItem(
        title = R.string.home_option_enable_list_view,
        subtitle = R.string.home_option_enable_list_view_subtitle,
        icon = R.drawable.ic_action_list,
        listener = View.OnClickListener {
          setGridView(false)
          activity.notifyAdapterExtraChanged()
          dismiss()
        },
        visible = !isTablet && isGridView()
    ))
    options.add(OptionsItem(
        title = R.string.home_option_enable_grid_view,
        subtitle = R.string.home_option_enable_grid_view_subtitle,
        icon = R.drawable.ic_action_grid,
        listener = View.OnClickListener {
          setGridView(true)
          activity.notifyAdapterExtraChanged()
          dismiss()
        },
        visible = !isTablet && !isGridView()
    ))
    options.add(OptionsItem(
        title = R.string.home_option_order_notes,
        subtitle = getSortingState().label,
        icon = R.drawable.ic_sort,
        listener = View.OnClickListener {
          SortingOptionsBottomSheet.openSheet(activity, { activity.setupData() })
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.note_option_font_size,
        subtitle = 0,
        content = activity.getString(R.string.note_option_font_size_subtitle, getDefaultTextSize()),
        icon = R.drawable.ic_title_white_48dp,
        listener = View.OnClickListener {
          if (getAppFlavor() == Flavor.PRO) {
            TextSizeBottomSheet.openSheet(activity)
          }
          dismiss()
        },
        visible = getAppFlavor() != Flavor.NONE,
        actionIcon = if (getAppFlavor() == Flavor.PRO) 0 else R.drawable.ic_rating
    ))
    options.add(OptionsItem(
        title = R.string.note_option_number_lines,
        subtitle = 0,
        content = activity.getString(R.string.note_option_number_lines_subtitle, getDefaultLineCount()),
        icon = R.drawable.ic_action_list,
        listener = View.OnClickListener {
          LineCountBottomSheet.openSheet(activity)
          dismiss()
        }
    ))
    return options
  }

  companion object {

    const val KEY_LIST_VIEW = "KEY_LIST_VIEW"

    fun openSheet(activity: MainActivity) {
      val sheet = UISettingsOptionsBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }

    fun isGridView(): Boolean = userPreferences().get(KEY_LIST_VIEW, false)

    fun setGridView(isGrid: Boolean) = userPreferences().put(KEY_LIST_VIEW, isGrid)
  }
}