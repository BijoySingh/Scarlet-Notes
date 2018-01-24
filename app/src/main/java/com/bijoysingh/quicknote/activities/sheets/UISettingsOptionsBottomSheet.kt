package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.view.View
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.activities.sheets.LineCountBottomSheet.Companion.getDefaultLineCount
import com.bijoysingh.quicknote.activities.sheets.SortingOptionsBottomSheet.Companion.getSortingState
import com.bijoysingh.quicknote.activities.sheets.TextSizeBottomSheet.Companion.getDefaultTextSize
import com.bijoysingh.quicknote.items.OptionsItem
import com.bijoysingh.quicknote.utils.*
import com.github.bijoysingh.starter.prefs.DataStore
import com.github.bijoysingh.starter.util.LocaleManager

class UISettingsOptionsBottomSheet : OptionItemBottomSheetBase() {

  override fun setupViewWithDialog(dialog: Dialog) {
    setOptions(dialog, getOptions())
  }

  private fun getOptions(): List<OptionsItem> {
    val activity = context as MainActivity
    val dataStore = DataStore.get(context)
    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.home_option_enable_night_mode,
        subtitle = R.string.home_option_enable_night_mode_subtitle,
        icon = R.drawable.night_mode_white_48dp,
        listener = View.OnClickListener {
          dataStore.put(KEY_APP_THEME, Theme.DARK.name)
          ThemeManager.get(activity).notifyUpdate(activity)
          activity.requestSetNightMode(true)
          dismiss()
        },
        visible = !isNightMode
    ))
    options.add(OptionsItem(
        title = R.string.home_option_enable_day_mode,
        subtitle = R.string.home_option_enable_day_mode_subtitle,
        icon = R.drawable.ic_action_day_mode,
        listener = View.OnClickListener {
          dataStore.put(KEY_APP_THEME, Theme.LIGHT.name)
          ThemeManager.get(activity).notifyUpdate(activity)
          activity.requestSetNightMode(false)
          dismiss()
        },
        visible = isNightMode
    ))
    val isTablet = resources.getBoolean(R.bool.is_tablet)
    options.add(OptionsItem(
        title = R.string.home_option_enable_list_view,
        subtitle = R.string.home_option_enable_list_view_subtitle,
        icon = R.drawable.ic_action_list,
        listener = View.OnClickListener {
          activity.setLayoutMode(false)
          dismiss()
        },
        visible = !isTablet && dataStore.get(KEY_LIST_VIEW, false)
    ))
    options.add(OptionsItem(
        title = R.string.home_option_enable_grid_view,
        subtitle = R.string.home_option_enable_grid_view_subtitle,
        icon = R.drawable.ic_action_grid,
        listener = View.OnClickListener {
          activity.setLayoutMode(true)
          dismiss()
        },
        visible = !isTablet && !dataStore.get(KEY_LIST_VIEW, false)
    ))
    options.add(OptionsItem(
        title = R.string.home_option_order_notes,
        subtitle = getSortingState(dataStore).label,
        icon = R.drawable.ic_sort,
        listener = View.OnClickListener {
          SortingOptionsBottomSheet.openSheet(activity, { activity.setupData() })
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.note_option_font_size,
        subtitle = 0,
        content = activity.getString(R.string.note_option_font_size_subtitle, getDefaultTextSize(dataStore)),
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
        content = activity.getString(R.string.note_option_number_lines_subtitle, getDefaultLineCount(dataStore)),
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
      sheet.isNightMode = activity.isNightMode
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}