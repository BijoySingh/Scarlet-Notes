package com.maubis.scarlet.base.main.sheets

import android.app.Dialog
import android.view.View
import com.github.bijoysingh.starter.util.IntentUtils
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.ui.ThemedBottomSheetFragment


class InstallProUpsellBottomSheet : ThemedBottomSheetFragment() {
  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val installPro = dialog.findViewById<View>(R.id.install_pro_app)
    installPro.setOnClickListener {
      IntentUtils.openAppPlayStore(context, "com.bijoysingh.quicknote.pro")
      dismiss()
    }
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_install_pro_upsell

  companion object {
    fun openSheet(activity: ThemedActivity) {
      val sheet = InstallProUpsellBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}