package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.activities.ThemedActivity
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

    val textColor = getColor(R.color.dark_tertiary_text, R.color.light_tertiary_text)
    whyInstallProDetails.setTextColor(textColor)

    val titleTextColor = getColor(R.color.material_blue_grey_500, R.color.material_blue_grey_200)
    whyInstallPro.setTextColor(titleTextColor)

    installPro.setTextColor(getColor(R.color.material_blue_600, R.color.material_blue_200))
    installPro.setOnClickListener {
      IntentUtils.openAppPlayStore(context, "com.bijoysingh.quicknote.pro")
      dismiss()
    }

    val optionsTitle = dialog.findViewById<TextView>(R.id.options_title)
    optionsTitle.setTextColor(getColor(R.color.dark_tertiary_text, R.color.light_secondary_text))
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_install_pro_upsell

  companion object {
    fun openSheet(activity: ThemedActivity) {
      val sheet = InstallProUpsellBottomSheet()
      sheet.isNightMode = activity.isNightMode
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}