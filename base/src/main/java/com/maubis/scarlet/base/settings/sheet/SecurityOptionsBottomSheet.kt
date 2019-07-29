package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import com.facebook.litho.ComponentContext
import com.github.ajalt.reprint.core.Reprint
import com.github.bijoysingh.starter.util.TextUtils
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.security.sheets.openCreateSheet
import com.maubis.scarlet.base.security.sheets.openVerifySheet
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.ui.ThemedActivity

const val KEY_SECURITY_CODE = "KEY_SECURITY_CODE"
const val KEY_FINGERPRINT_ENABLED = "KEY_FINGERPRINT_ENABLED"
const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"

var sSecurityCode: String
  get() = ApplicationBase.instance.store().get(KEY_SECURITY_CODE, "")
  set(value) = ApplicationBase.instance.store().put(KEY_SECURITY_CODE, value)
var sSecurityFingerprintEnabled: Boolean
  get() = ApplicationBase.instance.store().get(KEY_FINGERPRINT_ENABLED, true)
  set(value) = ApplicationBase.instance.store().put(KEY_FINGERPRINT_ENABLED, value)
var sSecurityAppLockEnabled: Boolean
  get() = ApplicationBase.instance.store().get(KEY_APP_LOCK_ENABLED, false)
  set(value) = ApplicationBase.instance.store().put(KEY_APP_LOCK_ENABLED, value)

class SecurityOptionsBottomSheet : LithoOptionBottomSheet() {
  override fun title(): Int = R.string.security_option_title

  override fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem> {
    val activity = context as ThemedActivity
    val options = ArrayList<LithoOptionsItem>()
    options.add(LithoOptionsItem(
        title = R.string.security_option_set_pin_code,
        subtitle = R.string.security_option_set_pin_code_subtitle,
        icon = R.drawable.ic_option_security,
        listener = {
          when {
            hasPinCodeEnabled() -> openResetPasswordDialog(dialog)
            else -> openCreatePasswordDialog(dialog)
          }
        },
        isSelectable = true,
        selected = hasPinCodeEnabled()
    ))

    options.add(LithoOptionsItem(
        title = R.string.security_option_lock_app,
        subtitle = R.string.security_option_lock_app_details,
        icon = R.drawable.ic_option_security,
        listener = {
          when {
            hasPinCodeEnabled() -> openVerifySheet(
                activity = activity,
                onVerifySuccess = {
                  sSecurityAppLockEnabled = !sSecurityAppLockEnabled
                  reset(componentContext.androidContext, dialog)
                }
            )
            else -> openCreatePasswordDialog(dialog)
          }
        },
        isSelectable = true,
        selected = sSecurityAppLockEnabled
    ))


    val hasFingerprint = Reprint.hasFingerprintRegistered()
    options.add(LithoOptionsItem(
        title = R.string.security_option_fingerprint_enabled,
        subtitle = R.string.security_option_fingerprint_enabled_subtitle,
        icon = R.drawable.ic_option_fingerprint,
        listener = {
          when {
            hasPinCodeEnabled() -> openVerifySheet(
                activity = activity,
                onVerifySuccess = {
                  sSecurityFingerprintEnabled = false
                  reset(componentContext.androidContext, dialog)
                }
            )
            else -> {
              sSecurityFingerprintEnabled = false
              reset(componentContext.androidContext, dialog)
            }
          }
        },
        visible = sSecurityFingerprintEnabled && hasFingerprint,
        isSelectable = true,
        selected = true
    ))
    options.add(LithoOptionsItem(
        title = R.string.security_option_fingerprint_disabled,
        subtitle = R.string.security_option_fingerprint_disabled_subtitle,
        icon = R.drawable.ic_option_fingerprint,
        listener = {
          when {
            hasPinCodeEnabled() -> openVerifySheet(
                activity = activity,
                onVerifySuccess = {
                  sSecurityFingerprintEnabled = true
                  reset(componentContext.androidContext, dialog)
                }
            )
            else -> {
              sSecurityFingerprintEnabled = true
              reset(componentContext.androidContext, dialog)
            }
          }
        },
        visible = !sSecurityFingerprintEnabled && hasFingerprint
    ))
    return options
  }

  fun openCreatePasswordDialog(dialog: Dialog) {
    val activity = context as ThemedActivity
    openCreateSheet(
        activity = activity,
        onCreateSuccess = { reset(dialog.context, dialog) })
  }

  fun openResetPasswordDialog(dialog: Dialog) {
    val activity = context as ThemedActivity
    openVerifySheet(
        activity,
        onVerifySuccess = {
          openCreatePasswordDialog(dialog)
        },
        onVerifyFailure = {
          openResetPasswordDialog(dialog)
        })
  }

  companion object {
    fun hasPinCodeEnabled(): Boolean {
      return !TextUtils.isNullOrEmpty(sSecurityCode)
    }
  }
}