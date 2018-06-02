package com.maubis.scarlet.base.export.sheet

import android.app.Dialog
import android.view.View
import com.github.bijoysingh.starter.util.IntentUtils
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.export.activity.ImportNoteActivity
import com.maubis.scarlet.base.export.support.*
import com.maubis.scarlet.base.main.sheets.EnterPincodeBottomSheet
import com.maubis.scarlet.base.settings.sheet.SecurityOptionsBottomSheet
import com.maubis.scarlet.base.support.Flavor

import com.maubis.scarlet.base.support.option.OptionsItem
import com.maubis.scarlet.base.support.sheets.OptionItemBottomSheetBase
import com.maubis.scarlet.base.support.ui.ThemedActivity

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
        visible = CoreConfig.instance.appFlavor() == Flavor.NONE
    ))
    options.add(OptionsItem(
        title = R.string.home_option_export,
        subtitle = R.string.home_option_export_subtitle,
        icon = R.drawable.ic_export,
        listener = View.OnClickListener {
          val manager = PermissionUtils().getStoragePermissionManager(activity)
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
          val manager = PermissionUtils().getStoragePermissionManager(activity)
          val hasAllPermissions = manager.hasAllPermissions()
          when (hasAllPermissions) {
            true -> {
              IntentUtils.startActivity(activity, ImportNoteActivity::class.java)
              dismiss()
            }
            false -> {
              PermissionBottomSheet.openSheet(activity)
            }
          }
        }
    ))
    val exportAsMarkdown = CoreConfig.instance.store().get(KEY_BACKUP_MARKDOWN, false)
    options.add(OptionsItem(
        title = R.string.home_option_export_markdown,
        subtitle = R.string.home_option_export_markdown_subtitle,
        icon = R.drawable.ic_markdown_logo,
        listener = View.OnClickListener {
          CoreConfig.instance.store().put(KEY_BACKUP_MARKDOWN, !exportAsMarkdown)
          reset(dialog)
        },
        enabled = exportAsMarkdown
    ))
    val autoBackupEnabled = CoreConfig.instance.store().get(KEY_AUTO_BACKUP_MODE, false)
    options.add(OptionsItem(
        title = R.string.home_option_auto_export,
        subtitle = R.string.home_option_auto_export_subtitle,
        icon = R.drawable.ic_time,
        listener = View.OnClickListener {
          val manager = PermissionUtils().getStoragePermissionManager(activity)
          val hasAllPermissions = manager.hasAllPermissions()
          when {
            autoBackupEnabled -> {
              CoreConfig.instance.store().put(KEY_AUTO_BACKUP_MODE, false)
              reset(dialog)
            }
            hasAllPermissions -> {
              CoreConfig.instance.store().put(KEY_AUTO_BACKUP_MODE, true)
              reset(dialog)
            }
            else -> PermissionBottomSheet.openSheet(activity)
          }
        },
        enabled = autoBackupEnabled
    ))
    val backupLocation = CoreConfig.instance.store().get(KEY_BACKUP_LOCATION, "")
    options.add(OptionsItem(
        title = R.string.home_option_auto_export,
        subtitle = R.string.home_option_auto_export_subtitle,
        icon = R.drawable.ic_time,
        listener = View.OnClickListener {
          val manager = PermissionUtils().getStoragePermissionManager(activity)
          val hasAllPermissions = manager.hasAllPermissions()
          when {
            hasAllPermissions -> {
              // Open folder choosing dialog, once built
            }
            else -> PermissionBottomSheet.openSheet(activity)
          }
        },
        visible = false
    ))

    return options
  }

  private fun openExportSheet() {
    val activity = themedActivity() as MainActivity
    if (!SecurityOptionsBottomSheet.hasPinCodeEnabled()) {
      ExportNotesBottomSheet.openSheet(activity)
      return
    }
    EnterPincodeBottomSheet.openUnlockSheet(
        activity as ThemedActivity,
        object : EnterPincodeBottomSheet.PincodeSuccessListener {
          override fun onFailure() {
            openExportSheet()
          }

          override fun onSuccess() {
            ExportNotesBottomSheet.openSheet(activity)
          }
        })
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_options

  companion object {
    fun openSheet(activity: MainActivity) {
      val sheet = BackupSettingsOptionsBottomSheet()

      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}