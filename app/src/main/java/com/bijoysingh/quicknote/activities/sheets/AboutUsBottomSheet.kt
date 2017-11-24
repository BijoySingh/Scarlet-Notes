package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.view.View
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.github.bijoysingh.starter.fragments.SimpleBottomSheetFragment


class AboutUsBottomSheet : SimpleBottomSheetFragment() {

  override fun setupView(dialog: Dialog?) {
    if (dialog == null) {
      return
    }

    val aboutUs = dialog.findViewById<TextView>(R.id.about_us)
    val aboutApp = dialog.findViewById<TextView>(R.id.about_app)
    val openSource = dialog.findViewById<TextView>(R.id.open_source)
    val appVersion = dialog.findViewById<TextView>(R.id.app_version)

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
        val creatorName = getString(R.string.maubis_apps)

        val aboutUsDetails = getString(R.string.about_page_about_us_details, appName)
        aboutUs.text = aboutUsDetails

        val aboutAppDetails = getString(R.string.about_page_description, appName)
        aboutApp.text = aboutAppDetails

        val openSourceDetails = getString(R.string.about_page_description_os, appName, creatorName)
        openSource.text = openSourceDetails

        appVersion.text = result
      }
    })
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_about_page

  companion object {
    fun openSheet(activity: MainActivity) {
      val sheet = AboutUsBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}