package com.bijoysingh.quicknote.export.sheet

import android.app.Dialog
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.bijoysingh.quicknote.MaterialNotes.Companion.appTheme
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.sheets.ThemedBottomSheetFragment
import com.bijoysingh.quicknote.export.support.PermissionUtils
import com.bijoysingh.quicknote.utils.ThemeColorType

class PermissionBottomSheet : ThemedBottomSheetFragment() {
  override fun getBackgroundView(): Int = R.id.container_layout

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val title = dialog.findViewById<TextView>(R.id.permissions_title)
    title.setTextColor(appTheme().get(ThemeColorType.SECONDARY_TEXT))

    val details = dialog.findViewById<TextView>(R.id.permissions_details)
    details.setTextColor(appTheme().get(ThemeColorType.TERTIARY_TEXT))

    val allowButton = dialog.findViewById<TextView>(R.id.give_permissions);
    allowButton.setTextColor(appTheme().get(ThemeColorType.ACCENT_TEXT))
    allowButton.setOnClickListener {
      val manager = PermissionUtils().getStoragePermissionManager(themedActivity() as AppCompatActivity)
      manager.requestPermissions()
      dismiss()
    }
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_give_permission

  companion object {
    fun openSheet(activity: AppCompatActivity) {
      val sheet = PermissionBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}