package com.bijoysingh.quicknote.firebase.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.github.bijoysingh.starter.util.IntentUtils
import com.github.bijoysingh.starter.util.ToastHelper
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.settings.sheet.AboutSettingsOptionsBottomSheet
import com.maubis.scarlet.base.support.bind
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.ui.visibility

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
    acceptCheckBox.setOnCheckedChangeListener { _, checked ->
      val textColor = if (checked) R.color.light_primary_text else R.color.dark_tertiary_text
      doneBtn.setTextColor(ContextCompat.getColor(this, textColor))

      val backgroundColor = if (checked) R.color.colorAccent else R.color.transparent
      doneBtn.setBackgroundColor(ContextCompat.getColor(this, backgroundColor))
    }

    doneBtn.setOnClickListener {
      if (acceptCheckBox.isChecked) {
        acceptThePolicy()
        if (startState == "" && !CoreConfig.instance.authenticator().isLoggedIn()) {
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
    refuseBtn.visibility = visibility(CoreConfig.instance.authenticator().isLoggedIn())

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
    if (startState == KEY_DATA_POLICY_REQUEST_LOGGED_IN && !CoreConfig.instance.authenticator().isLoggedIn()) {
      finish()
    }
  }

  companion object {

    const val DATA_POLICY_VERSION = 4
    const val DATA_POLICY_ACCEPTED = "DATA_POLICY_ACCEPTED_VERSION"

    fun hasAcceptedThePolicy() = CoreConfig.instance.store().get(DATA_POLICY_ACCEPTED, 0) == DATA_POLICY_VERSION

    fun acceptThePolicy() = CoreConfig.instance.store().put(DATA_POLICY_ACCEPTED, DATA_POLICY_VERSION)

    fun openIfNeeded(activity: AppCompatActivity) {
      if (!hasAcceptedThePolicy() && CoreConfig.instance.authenticator().isLoggedIn()) {
        val intent = Intent(activity, DataPolicyActivity::class.java)
        intent.putExtra(KEY_DATA_POLICY_REQUEST, KEY_DATA_POLICY_REQUEST_LOGGED_IN)
        activity.startActivity(intent)
      }
    }
  }
}
