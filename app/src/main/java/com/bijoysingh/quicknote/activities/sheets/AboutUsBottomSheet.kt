package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.github.bijoysingh.starter.async.MultiAsyncTask
import com.github.bijoysingh.starter.fragments.SimpleBottomSheetFragment
import com.github.bijoysingh.starter.util.IntentUtils


class AboutUsBottomSheet : SimpleBottomSheetFragment() {

  override fun setupView(dialog: Dialog?) {
    if (dialog == null) {
      return
    }

    val aboutUs = dialog.findViewById<TextView>(R.id.about_us)
    val aboutApp = dialog.findViewById<TextView>(R.id.about_app)
    val openSource = dialog.findViewById<TextView>(R.id.open_source)
    val appVersion = dialog.findViewById<TextView>(R.id.app_version)

    val contribute = dialog.findViewById<View>(R.id.contribute)
    val rateUs = dialog.findViewById<View>(R.id.rate_us)

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

        contribute.setOnClickListener {
          context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)))
          dismiss()
        }

        rateUs.setOnClickListener {
          IntentUtils.openAppPlayStore(activity)
          dismiss()
        }
      }
    })
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_about_page

  companion object {

    val GITHUB_URL = "https://github.com/BijoySingh/Material-Notes-Android-App"

    fun openSheet(activity: MainActivity) {
      val sheet = AboutUsBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }
}