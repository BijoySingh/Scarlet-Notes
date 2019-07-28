package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import com.facebook.litho.ComponentContext
import com.github.ajalt.reprint.core.Reprint
import com.github.bijoysingh.starter.util.TextUtils
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.main.sheets.EnterPincodeBottomSheet
import com.maubis.scarlet.base.main.sheets.EnterPincodeBottomSheet.Companion.openCreateSheet
import com.maubis.scarlet.base.main.sheets.EnterPincodeBottomSheet.Companion.openVerifySheet
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.ui.ThemedActivity

class SecurityOptionsBottomSheet : LithoOptionBottomSheet() {
  override fun title(): Int = R.string.security_option_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val options = ArrayList<LithoOptionsItem>()
    options.add(LithoOptionsItem(
        title = R.string.security_option_set_pin_code,
        subtitle = R.string.security_option_set_pin_code_subtitle,
        icon = R.drawable.ic_option_security,
        listener = {
          val currentPinCode = ApplicationBase.instance.store().get(KEY_SECURITY_CODE, "")
          val hasPinCode = !TextUtils.isNullOrEmpty(currentPinCode)
          if (hasPinCode) {
            openResetPasswordDialog(dialog)
          } else {
            openCreatePasswordDialog(dialog)
          }
        },
        isSelectable = true,
        selected = !TextUtils.isNullOrEmpty(ApplicationBase.instance.store().get(KEY_SECURITY_CODE, ""))
    ))

    val hasFingerprint = Reprint.hasFingerprintRegistered()
    options.add(LithoOptionsItem(
        title = R.string.security_option_fingerprint_enabled,
        subtitle = R.string.security_option_fingerprint_enabled_subtitle,
        icon = R.drawable.ic_option_fingerprint,
        listener = {
          val currentPinCode = ApplicationBase.instance.store().get(KEY_SECURITY_CODE, "")
          val hasPinCode = !TextUtils.isNullOrEmpty(currentPinCode)
          if (hasPinCode) {
            openVerifyPasswordDialog(
                object : EnterPincodeBottomSheet.PincodeSuccessOnlyListener {
                  override fun onSuccess() {
                    ApplicationBase.instance.store().put(KEY_FINGERPRINT_ENABLED, false)
                    reset(componentContext.androidContext, dialog)
                  }
                }
            )
          } else {
            ApplicationBase.instance.store().put(KEY_FINGERPRINT_ENABLED, false)
            reset(componentContext.androidContext, dialog)
          }
        },
        visible = ApplicationBase.instance.store().get(KEY_FINGERPRINT_ENABLED, true) && hasFingerprint,
        isSelectable = true,
        selected = true
    ))
    options.add(LithoOptionsItem(
        title = R.string.security_option_fingerprint_disabled,
        subtitle = R.string.security_option_fingerprint_disabled_subtitle,
        icon = R.drawable.ic_option_fingerprint,
        listener = {
          val currentPinCode = ApplicationBase.instance.store().get(KEY_SECURITY_CODE, "")
          val hasPinCode = !TextUtils.isNullOrEmpty(currentPinCode)
          if (hasPinCode) {
            openVerifyPasswordDialog(
                object : EnterPincodeBottomSheet.PincodeSuccessOnlyListener {
                  override fun onSuccess() {
                    ApplicationBase.instance.store().put(KEY_FINGERPRINT_ENABLED, true)
                    reset(componentContext.androidContext, dialog)
                  }
                }
            )
          } else {
            ApplicationBase.instance.store().put(KEY_FINGERPRINT_ENABLED, true)
            reset(componentContext.androidContext, dialog)
          }
        },
        visible = !ApplicationBase.instance.store().get(KEY_FINGERPRINT_ENABLED, true) && hasFingerprint
    ))
    return options
  }

  fun openCreatePasswordDialog(dialog: Dialog) {
    val activity = context as ThemedActivity
    openCreateSheet(
        activity,
        object : EnterPincodeBottomSheet.PincodeSuccessOnlyListener {
          override fun onSuccess() {
            reset(dialog.context, dialog)
          }
        })
  }

  fun openResetPasswordDialog(dialog: Dialog) {
    val activity = context as ThemedActivity
    openVerifySheet(
        activity,
        object : EnterPincodeBottomSheet.PincodeSuccessListener {
          override fun onFailure() {
            openResetPasswordDialog(dialog)
          }

          override fun onSuccess() {
            openCreatePasswordDialog(dialog)
          }
        })
  }

  fun openVerifyPasswordDialog(listener: EnterPincodeBottomSheet.PincodeSuccessOnlyListener) {
    val activity = context as ThemedActivity
    openVerifySheet(
        activity,
        object : EnterPincodeBottomSheet.PincodeSuccessListener {
          override fun onFailure() {

          }

          override fun onSuccess() {
            listener.onSuccess()
          }
        })
  }

  companion object {

    const val KEY_SECURITY_CODE = "KEY_SECURITY_CODE"
    const val KEY_FINGERPRINT_ENABLED = "KEY_FINGERPRINT_ENABLED"

    fun openSheet(activity: MainActivity) {
      val sheet = SecurityOptionsBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }

    fun hasPinCodeEnabled(): Boolean {
      val currentPinCode = ApplicationBase.instance.store().get(KEY_SECURITY_CODE, "")
      return !TextUtils.isNullOrEmpty(currentPinCode)
    }
  }
}