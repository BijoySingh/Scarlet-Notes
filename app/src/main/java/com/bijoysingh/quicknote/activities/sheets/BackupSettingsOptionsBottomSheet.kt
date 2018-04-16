package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.view.View
import com.bijoysingh.quicknote.MaterialNotes.Companion.userPreferences
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.ForgetMeActivity
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.activities.external.ImportNoteFromFileActivity
import com.bijoysingh.quicknote.activities.external.KEY_AUTO_BACKUP_MODE
import com.bijoysingh.quicknote.activities.external.getStoragePermissionManager
import com.bijoysingh.quicknote.items.OptionsItem
import com.bijoysingh.quicknote.utils.Flavor
import com.bijoysingh.quicknote.utils.getAppFlavor
import com.bijoysingh.quicknote.utils.isLoggedIn
import com.github.bijoysingh.starter.util.IntentUtils

class BackupSettingsOptionsBottomSheet : OptionItemBottomSheetBase() {

  override fun setupViewWithDialog(dialog: Dialog) {
    setOptions(dialog, getOptions())
  }

  private fun getOptions(): List<OptionsItem> {
    val activity = context as MainActivity
    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.home_option_install_from_store,
        subtitle = R.string.home_option_install_from_store_subtitle,
        icon = R.drawable.ic_action_play,
        listener = View.OnClickListener {
          IntentUtils.openAppPlayStore(context)
          dismiss()
        },
        visible = getAppFlavor() == Flavor.NONE
    ))
    options.add(OptionsItem(
        title = R.string.home_option_export,
        subtitle = R.string.home_option_export_subtitle,
        icon = R.drawable.ic_export,
        listener = View.OnClickListener {
          val manager = getStoragePermissionManager(activity)
          val hasAllPermissions = manager.hasAllPermissions()
          when (hasAllPermissions) {
            true -> {
              openExportSheet()
              dismiss()
            }
            false -> {
              PermissionBottomSheet.openSheet(activity)
            }
          }
        }
    ))
    options.add(OptionsItem(
        title = R.string.home_option_import,
        subtitle = R.string.home_option_import_subtitle,
        icon = R.drawable.ic_import,
        listener = View.OnClickListener {
          val manager = getStoragePermissionManager(activity)
          val hasAllPermissions = manager.hasAllPermissions()
          when (hasAllPermissions) {
            true -> {
              IntentUtils.startActivity(activity, ImportNoteFromFileActivity::class.java)
              dismiss()
            }
            false -> {
              PermissionBottomSheet.openSheet(activity)
            }
          }
        }
    ))
    val autoBackupEnabled = userPreferences().get(KEY_AUTO_BACKUP_MODE, false)
    options.add(OptionsItem(
        title = R.string.home_option_auto_export,
        subtitle = R.string.home_option_auto_export_subtitle,
        icon = R.drawable.ic_time,
        listener = View.OnClickListener {
          val manager = getStoragePermissionManager(activity)
          val hasAllPermissions = manager.hasAllPermissions()
          when {
            autoBackupEnabled -> {
              userPreferences().put(KEY_AUTO_BACKUP_MODE, false)
              reset(dialog)
            }
            hasAllPermissions -> {
              userPreferences().put(KEY_AUTO_BACKUP_MODE, true)
              reset(dialog)
            }
            else -> PermissionBottomSheet.openSheet(activity)
          }
        },
        enabled = autoBackupEnabled
    ))
    return options
  }

  private fun openExportSheet() {
    val activity = context as MainActivity
    if (!SecurityOptionsBottomSheet.hasPinCodeEnabled()) {
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
        })
  }

  override fun getLayout(): Int = R.layout.layout_options_sheet

  companion object {
    fun openSheet(activity: MainActivity) {
      val sheet = BackupSettingsOptionsBottomSheet()

      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}