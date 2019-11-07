package com.maubis.scarlet.base.security.controller

import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sBiometricManager
import com.maubis.scarlet.base.settings.sheet.sSecurityBiometricEnabled

fun deviceHasBiometricEnabled(): Boolean {
  return sBiometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
}

fun isBiometricEnabled() = sSecurityBiometricEnabled && deviceHasBiometricEnabled()

fun showBiometricPrompt(
  activity: AppCompatActivity,
  fragment: Fragment? = null,
  onSuccess: () -> Unit = {},
  onFailure: () -> Unit = {},
  title: Int = R.string.biometric_prompt_unlock_app,
  subtitle: Int = R.string.biometric_prompt_unlock_app_details) {
  val executor = ContextCompat.getMainExecutor(activity)

  val callback = object : BiometricPrompt.AuthenticationCallback() {
    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
      super.onAuthenticationError(errorCode, errString)
      onFailure()
    }

    override fun onAuthenticationFailed() {
      super.onAuthenticationFailed()
      onFailure()
    }

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
      super.onAuthenticationSucceeded(result)
      onSuccess()
    }
  }

  val prompt = when {
    fragment !== null -> BiometricPrompt(fragment, executor, callback)
    else -> BiometricPrompt(activity, executor, callback)
  }
  val promptInfo = BiometricPrompt.PromptInfo.Builder()
    .setTitle(activity.getString(title))
    .setDescription(activity.getString(subtitle))
    .setDeviceCredentialAllowed(false)
    .setNegativeButtonText(activity.getString(R.string.delete_sheet_delete_trash_no))
    .build()
  prompt.authenticate(promptInfo)
}