package com.maubis.scarlet.base.security.activity

import android.content.Context
import android.os.Bundle
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.github.ajalt.reprint.core.AuthenticationFailureReason
import com.github.ajalt.reprint.core.AuthenticationListener
import com.github.ajalt.reprint.core.Reprint
import com.maubis.scarlet.base.security.controller.PinLockController
import com.maubis.scarlet.base.settings.sheet.sSecurityCode
import com.maubis.scarlet.base.settings.sheet.sSecurityFingerprintEnabled
import com.maubis.scarlet.base.support.ui.ThemedActivity

class AppLockActivity : ThemedActivity() {
  lateinit var context: Context
  lateinit var component: Component
  lateinit var componentContext: ComponentContext

  private var passCodeEntered: String = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    context = this
    componentContext = ComponentContext(context)

    setView()
    notifyThemeChange()
  }

  private fun setView() {
    component = AppLockView.create(componentContext)
      .fingerprintEnabled(Reprint.hasFingerprintRegistered() && sSecurityFingerprintEnabled)
      .onTextChange { text ->
        passCodeEntered = text
      }
      .onClick {
        if (passCodeEntered.length == 4 && sSecurityCode == passCodeEntered) {
          PinLockController.notifyPinVerified()
          finish()
        }
      }
      .build()
    setContentView(LithoView.create(componentContext, component))
  }

  override fun onResume() {
    super.onResume()
    passCodeEntered = ""
    Reprint.authenticate(object : AuthenticationListener {
      override fun onSuccess(moduleTag: Int) {
        PinLockController.notifyPinVerified()
        finish()
      }

      override fun onFailure(
        failureReason: AuthenticationFailureReason?,
        fatal: Boolean,
        errorMessage: CharSequence?,
        moduleTag: Int,
        errorCode: Int) {
        // Ignore
      }
    })
  }

  override fun onPause() {
    super.onPause()
    Reprint.cancelAuthentication()
  }

  override fun notifyThemeChange() {
    setSystemTheme()
  }
}
