package com.bijoysingh.quicknote.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.bijoysingh.quicknote.MaterialNotes.Companion.userPreferences
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.sheets.AboutSettingsOptionsBottomSheet
import com.bijoysingh.quicknote.utils.bind
import com.bijoysingh.quicknote.utils.isLoggedIn
import com.bijoysingh.quicknote.utils.visibility
import com.github.bijoysingh.starter.util.IntentUtils
import com.github.bijoysingh.starter.util.ToastHelper

class DataPolicyActivity : ThemedActivity() {

  val pageTitle = R.id.page_title
  val headings = intArrayOf(R.id.heading_1, R.id.heading_2, R.id.heading_3, R.id.heading_4)
  val texts = intArrayOf(R.id.text_introduction, R.id.text_1, R.id.text_2, R.id.text_3)

  val acceptCheckBox: CheckBox by bind(R.id.accept_policy)
  val refuseBtn: TextView by bind(R.id.btn_refuse)
  val doneBtn: TextView by bind(R.id.btn_done)
  val pageContainer: View by bind(R.id.scroll_bg)
  val privacyPolicy: View by bind(R.id.heading_4)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_data_policy)

    notifyThemeChange()
    acceptCheckBox.setOnCheckedChangeListener { button, checked ->
      val textColor = if (checked) R.color.light_primary_text else R.color.dark_tertiary_text
      doneBtn.setTextColor(ContextCompat.getColor(this, textColor))

      val backgroundColor = if (checked) R.color.colorAccent else R.color.transparent
      doneBtn.setBackgroundColor(ContextCompat.getColor(this, backgroundColor))
    }

    doneBtn.setOnClickListener {
      if (acceptCheckBox.isChecked) {
        userPreferences().put(DATA_POLICY_ACCEPTED, true)
        finish()
        return@setOnClickListener
      }

      ToastHelper.show(this, "Please consent to the data policy")
    }

    refuseBtn.setOnClickListener {

    }
    refuseBtn.visibility = visibility(isLoggedIn())

    privacyPolicy.setOnClickListener {
      startActivity(Intent(
          Intent.ACTION_VIEW,
          Uri.parse(AboutSettingsOptionsBottomSheet.PRIVACY_POLICY_LINK)))
    }
  }

  override fun notifyThemeChange() {
    pageContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
    findViewById<TextView>(pageTitle).setTextColor(ContextCompat.getColor(this, R.color.dark_primary_text))
    for (heading in headings) {
      findViewById<TextView>(heading).setTextColor(ContextCompat.getColor(this, R.color.dark_secondary_text))
    }
    for (text in texts) {
      findViewById<TextView>(text).setTextColor(ContextCompat.getColor(this, R.color.dark_tertiary_text))
    }
  }


  override fun onBackPressed() {

  }

  companion object {

    const val DATA_POLICY_ACCEPTED = "DATA_POLICY_ACCEPTED"

    fun openIfNeeded(activity: AppCompatActivity) {
      if (!userPreferences().get(DATA_POLICY_ACCEPTED, false)) {
        IntentUtils.startActivity(activity, DataPolicyActivity::class.java)
      }
    }
  }
}
