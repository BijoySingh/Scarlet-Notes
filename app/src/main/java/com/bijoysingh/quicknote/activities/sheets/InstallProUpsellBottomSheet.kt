package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.github.bijoysingh.starter.util.IntentUtils


class InstallProUpsellBottomSheet : ThemedBottomSheetFragment() {
  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val whyInstallPro = dialog.findViewById<TextView>(R.id.why_install_pro)
    val whyInstallProDetails = dialog.findViewById<TextView>(R.id.why_install_pro_details)
    val installPro = dialog.findViewById<TextView>(R.id.install_pro_app)

    val textColor = theme().get(themedContext(), ThemeColorType.TERTIARY_TEXT)
    whyInstallProDetails.setTextColor(textColor)

    val titleTextColor = theme().get(themedContext(), ThemeColorType.SECTION_HEADER)
    whyInstallPro.setTextColor(titleTextColor)

    installPro.setTextColor(theme().get(themedContext(), ThemeColorType.ACCENT_TEXT))
    installPro.setOnClickListener {
      IntentUtils.openAppPlayStore(context, "com.bijoysingh.quicknote.pro")
      dismiss()
    }

    val optionsTitle = dialog.findViewById<TextView>(R.id.options_title)
    optionsTitle.setTextColor(theme().get(themedContext(), ThemeColorType.SECONDARY_TEXT))
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_install_pro_upsell

  companion object {
    fun openSheet(activity: ThemedActivity) {
      val sheet = InstallProUpsellBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}