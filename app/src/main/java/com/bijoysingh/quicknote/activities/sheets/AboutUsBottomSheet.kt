package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.github.bijoysingh.starter.util.IntentUtils


class AboutUsBottomSheet : ThemedBottomSheetFragment() {
  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val aboutUs = dialog.findViewById<TextView>(R.id.about_us)
    val aboutApp = dialog.findViewById<TextView>(R.id.about_app)
    val appVersion = dialog.findViewById<TextView>(R.id.app_version)
    val rateUs = dialog.findViewById<TextView>(R.id.rate_us)

    val activity = themedActivity()
    MultiAsyncTask.execute(activity, object : MultiAsyncTask.Task<String> {
      override fun run(): String {
        try {
          val pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0)
          return pInfo.versionName
        } catch (e: Exception) {

        }
        return ""
      }

      override fun handle(result: String) {
        val appName = getString(R.string.app_name)
        val aboutUsDetails = getString(R.string.about_page_about_us_details, appName)
        aboutUs.text = aboutUsDetails

        val aboutAppDetails = getString(R.string.about_page_description, appName)
        aboutApp.text = aboutAppDetails

        appVersion.text = result

        rateUs.setOnClickListener {
          IntentUtils.openAppPlayStore(activity)
          dismiss()
        }
      }
    })

    val textColor = getColor(R.color.dark_tertiary_text, R.color.light_tertiary_text)
    aboutUs.setTextColor(textColor)
    aboutApp.setTextColor(textColor)
    appVersion.setTextColor(textColor)

    val aboutUsTitle = dialog.findViewById<TextView>(R.id.about_us_title)
    val aboutAppTitle = dialog.findViewById<TextView>(R.id.about_app_title)
    val appVersionTitle = dialog.findViewById<TextView>(R.id.app_version_title)
    val titleTextColor = getColor(R.color.material_blue_grey_500, R.color.material_blue_grey_200)
    aboutUsTitle.setTextColor(titleTextColor)
    aboutAppTitle.setTextColor(titleTextColor)
    appVersionTitle.setTextColor(titleTextColor)

    rateUs.setTextColor(getColor(R.color.material_blue_600, R.color.material_blue_200))

    val optionsTitle = dialog.findViewById<TextView>(R.id.options_title)
    optionsTitle.setTextColor(getColor(R.color.dark_tertiary_text, R.color.light_secondary_text))
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_about_page

  companion object {
    fun openSheet(activity: MainActivity) {
      val sheet = AboutUsBottomSheet()

      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}