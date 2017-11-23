package com.bijoysingh.quicknote.activities.sheets

import android.Manifest
import android.app.Dialog
import android.view.View
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.activities.external.getStoragePermissionManager
import com.github.bijoysingh.starter.fragments.SimpleBottomSheetFragment
import com.github.bijoysingh.starter.util.PermissionManager

class PermissionBottomSheet : SimpleBottomSheetFragment() {
  override fun setupView(dialog: Dialog?) {
    if (dialog == null) {
      return
    }

    val allowButton = dialog.findViewById<View>(R.id.give_permissions);
    allowButton.setOnClickListener {
      val manager = getStoragePermissionManager(context as MainActivity)
      manager.requestPermissions()
      dismiss()
    }
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_give_permission

  companion object {
    fun openSheet(activity: MainActivity) {
      val sheet = PermissionBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}