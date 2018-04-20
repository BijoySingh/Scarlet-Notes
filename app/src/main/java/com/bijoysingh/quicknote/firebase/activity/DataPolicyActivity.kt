package com.bijoysingh.quicknote.firebase.activity

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
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.activities.sheets.AboutSettingsOptionsBottomSheet
import com.bijoysingh.quicknote.utils.bind
import com.bijoysingh.quicknote.utils.isLoggedIn
import com.bijoysingh.quicknote.utils.visibility
import com.github.bijoysingh.starter.util.IntentUtils
import com.github.bijoysingh.starter.util.ToastHelper

const val KEY_DATA_POLICY_REQUEST = "KEY_DATA_POLICY_REQUEST"
const val KEY_DATA_POLICY_REQUEST_LOGGED_IN = "LOGGED_IN"

class DataPolicyActivity : ThemedActivity() {

  var startState: String = ""

  val acceptCheckBox: CheckBox by bind(R.id.accept_policy)
  val refuseBtn: TextView by bind(R.id.btn_refuse)
  val doneBtn: TextView by bind(R.id.btn_done)
  val privacyPolicy: View by bind(R.id.heading_4)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_data_policy)

    startState = intent.getStringExtra(KEY_DATA_POLICY_REQUEST) ?: ""

    notifyThemeChange()
    acceptCheckBox.setOnCheckedChangeListener { button, checked ->
      val textColor = if (checked) R.color.light_primary_text else R.color.dark_tertiary_text
      doneBtn.setTextColor(ContextCompat.getColor(this, textColor))

      val backgroundColor = if (checked) R.color.colorAccent else R.color.transparent
      doneBtn.setBackgroundColor(ContextCompat.getColor(this, backgroundColor))
    }

    doneBtn.setOnClickListener {
      if (acceptCheckBox.isChecked) {
        acceptThePolicy()
        if (startState == "" && !isLoggedIn()) {
          IntentUtils.startActivity(this, LoginActivity::class.java)
        }

        finish()
        return@setOnClickListener
      }

      ToastHelper.show(this, "Please consent to the data policy")
    }

    refuseBtn.setOnClickListener {
      IntentUtils.startActivity(this, ForgetMeActivity::class.java)
    }
    refuseBtn.visibility = visibility(isLoggedIn())

    privacyPolicy.setOnClickListener {
      startActivity(Intent(
          Intent.ACTION_VIEW,
          Uri.parse(AboutSettingsOptionsBottomSheet.PRIVACY_POLICY_LINK)))
    }
  }

  override fun notifyThemeChange() {

  }

  override fun onResume() {
    super.onResume()
    if (startState == KEY_DATA_POLICY_REQUEST_LOGGED_IN && !isLoggedIn()) {
      finish()
    }
  }

  companion object {

    const val DATA_POLICY_VERSION = 2
    const val DATA_POLICY_ACCEPTED = "DATA_POLICY_ACCEPTED_VERSION"

    fun hasAcceptedThePolicy() = userPreferences().get(DATA_POLICY_ACCEPTED, 0) == DATA_POLICY_VERSION

    fun acceptThePolicy() = userPreferences().put(DATA_POLICY_ACCEPTED, DATA_POLICY_VERSION)

    fun openIfNeeded(activity: AppCompatActivity) {
      if (!hasAcceptedThePolicy() && isLoggedIn()) {
        val intent = Intent(activity, DataPolicyActivity::class.java)
        intent.putExtra(KEY_DATA_POLICY_REQUEST, KEY_DATA_POLICY_REQUEST_LOGGED_IN)
        activity.startActivity(intent)
      }
    }
  }
}
