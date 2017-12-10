package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.view.View
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.activities.external.ImportNoteFromFileActivity
import com.bijoysingh.quicknote.activities.external.getStoragePermissionManager
import com.bijoysingh.quicknote.items.OptionsItem
import com.github.bijoysingh.starter.prefs.DataStore
import com.github.bijoysingh.starter.util.IntentUtils

class SettingsOptionsBottomSheet : OptionItemBottomSheetBase() {

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
          activity.requestSetNightMode(false)
          dismiss()
        },
        visible = isNightMode
    ))
    val isMarkdownEnabled = dataStore.get(KEY_MARKDOWN_ENABLED, true)
    options.add(OptionsItem(
        title = R.string.home_option_markdown_support,
        subtitle = R.string.home_option_markdown_support_subtitle,
        icon = R.drawable.ic_action_markdown,
        listener = View.OnClickListener {
          dataStore.put(KEY_MARKDOWN_ENABLED, !isMarkdownEnabled)
          reset(dialog)
        },
        enabled = isMarkdownEnabled
    ))
    options.add(OptionsItem(
        title = R.string.home_option_enable_list_view,
        subtitle = R.string.home_option_enable_list_view_subtitle,
        icon = R.drawable.ic_action_list,
        listener = View.OnClickListener {
          activity.setLayoutMode(false)
          dismiss()
        },
        visible = dataStore.get(KEY_LIST_VIEW, false)
    ))
    options.add(OptionsItem(
        title = R.string.home_option_enable_grid_view,
        subtitle = R.string.home_option_enable_grid_view_subtitle,
        icon = R.drawable.ic_action_grid,
        listener = View.OnClickListener {
          activity.setLayoutMode(true)
          dismiss()
        },
        visible = !dataStore.get(KEY_LIST_VIEW, false)
    ))
    options.add(OptionsItem(
        title = R.string.home_option_export,
        subtitle = R.string.home_option_export_subtitle,
        icon = R.drawable.ic_export,
        listener = View.OnClickListener {
          val manager = getStoragePermissionManager(activity)
          if (manager.hasAllPermissions()) {
            openExportSheet()
            dismiss()
          } else {
            PermissionBottomSheet.openSheet(activity)
          }
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_import,
        subtitle = R.string.home_option_import_subtitle,
        icon = R.drawable.ic_import,
        listener = View.OnClickListener {
          val manager = getStoragePermissionManager(activity)
          if (manager.hasAllPermissions()) {
            IntentUtils.startActivity(activity, ImportNoteFromFileActivity::class.java)
            dismiss()
          } else {
            PermissionBottomSheet.openSheet(activity)
          }
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_security,
        subtitle = R.string.home_option_security_subtitle,
        icon = R.drawable.ic_option_security,
        listener = View.OnClickListener {
          SecurityOptionsBottomSheet.openSheet(activity)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_about_page,
        subtitle = R.string.home_option_about_page_subtitle,
        icon = R.drawable.ic_info,
        listener = View.OnClickListener {
          AboutUsBottomSheet.openSheet(activity)
          dismiss()
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_rate_and_review,
        subtitle = R.string.home_option_rate_and_review_subtitle,
        icon = R.drawable.ic_rating,
        listener = View.OnClickListener {
          IntentUtils.openAppPlayStore(activity)
          dismiss()
        }
    ))
    return options
  }

  private fun openExportSheet() {
    val activity = context as MainActivity
    val dataStore = DataStore.get(context)
    if (!SecurityOptionsBottomSheet.hasPinCodeEnabled(dataStore)) {
      ExportNotesBottomSheet.openSheet(activity)
      return
    }
    EnterPincodeBottomSheet.openUnlockSheet(
        context as ThemedActivity,
        object : EnterPincodeBottomSheet.PincodeSuccessListener {
          override fun onFailure() {
            openExportSheet()
          }

          override fun onSuccess() {
            ExportNotesBottomSheet.openSheet(activity)
          }
        },
        dataStore)
  }

  override fun getLayout(): Int = R.layout.layout_options_sheet

  companion object {

    const val KEY_LIST_VIEW = "KEY_LIST_VIEW"
    const val KEY_MARKDOWN_ENABLED = "KEY_MARKDOWN_ENABLED"

    fun openSheet(activity: MainActivity) {
      val sheet = SettingsOptionsBottomSheet()
      sheet.isNightMode = activity.isNightMode
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}