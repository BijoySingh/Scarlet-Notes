package com.maubis.scarlet.base.export.sheet

import android.app.Dialog
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.export.support.PermissionUtils
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedBottomSheetFragment

class PermissionBottomSheet : ThemedBottomSheetFragment() {
  override fun getBackgroundView(): Int = R.id.container_layout

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val title = dialog.findViewById<TextView>(R.id.permissions_title)
    title.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))

    val details = dialog.findViewById<TextView>(R.id.permissions_details)
    details.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT))

    val allowButton = dialog.findViewById<TextView>(R.id.give_permissions);
    allowButton.setOnClickListener {
      val manager = PermissionUtils().getStoragePermissionManager(themedActivity() as AppCompatActivity)
      manager.requestPermissions()
      dismiss()
    }

    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_give_permission

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.permission_card)

  companion object {
    fun openSheet(activity: AppCompatActivity) {
      val sheet = PermissionBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}