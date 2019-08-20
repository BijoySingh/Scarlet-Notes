package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import com.facebook.litho.ComponentContext
import com.github.ajalt.reprint.core.Reprint
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.ApplicationBase.Companion.instance
import com.maubis.scarlet.base.main.sheets.InstallProUpsellBottomSheet
import com.maubis.scarlet.base.security.controller.PinLockController.isPinCodeEnabled
import com.maubis.scarlet.base.security.sheets.openCreateSheet
import com.maubis.scarlet.base.security.sheets.openVerifySheet
import com.maubis.scarlet.base.support.sheets.LithoOptionBottomSheet
import com.maubis.scarlet.base.support.sheets.LithoOptionsItem
import com.maubis.scarlet.base.support.sheets.openSheet
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.utils.Flavor

const val KEY_SECURITY_CODE = "KEY_SECURITY_CODE"
const val KEY_FINGERPRINT_ENABLED = "KEY_FINGERPRINT_ENABLED"
const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
const val KEY_ASK_PIN_ALWAYS = "ask_pin_always"

var sSecurityCode: String
  get() = ApplicationBase.instance.store().get(KEY_SECURITY_CODE, "")
  set(value) = ApplicationBase.instance.store().put(KEY_SECURITY_CODE, value)
var sSecurityFingerprintEnabled: Boolean
  get() = ApplicationBase.instance.store().get(KEY_FINGERPRINT_ENABLED, true)
  set(value) = ApplicationBase.instance.store().put(KEY_FINGERPRINT_ENABLED, value)
var sSecurityAppLockEnabled: Boolean
  get() = ApplicationBase.instance.store().get(KEY_APP_LOCK_ENABLED, false)
  set(value) = ApplicationBase.instance.store().put(KEY_APP_LOCK_ENABLED, value)
var sSecurityAskPinAlways: Boolean
  get() = ApplicationBase.instance.store().get(KEY_ASK_PIN_ALWAYS, true)
  set(value) = ApplicationBase.instance.store().put(KEY_ASK_PIN_ALWAYS, value)

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
            isPinCodeEnabled() -> openResetPasswordDialog(dialog)
            else -> openCreatePasswordDialog(dialog)
          }
        },
        isSelectable = true,
        selected = isPinCodeEnabled()
    ))

    val isLite = instance.appFlavor() == Flavor.LITE
    options.add(LithoOptionsItem(
        title = R.string.security_option_lock_app,
        subtitle = R.string.security_option_lock_app_details,
        icon = R.drawable.ic_apps_white_48dp,
        listener = {
          if (isLite && !sSecurityAppLockEnabled) {
            openSheet(activity, InstallProUpsellBottomSheet())
            return@LithoOptionsItem
          }

          when {
            isPinCodeEnabled() -> openVerifySheet(
                activity = activity,
                onVerifySuccess = {
                  sSecurityAppLockEnabled = !sSecurityAppLockEnabled
                  reset(componentContext.androidContext, dialog)
                }
            )
            else -> openCreatePasswordDialog(dialog)
          }
        },
        actionIcon = when {
          sSecurityAppLockEnabled -> R.drawable.ic_done_white_48dp
          isLite -> R.drawable.ic_rating
          else -> 0
        }
    ))

    options.add(LithoOptionsItem(
        title = R.string.security_option_ask_pin_always,
        subtitle = R.string.security_option_ask_pin_always_details,
        icon = R.drawable.ic_action_grid,
        listener = {
          if (isLite) {
            openSheet(activity, InstallProUpsellBottomSheet())
            return@LithoOptionsItem
          }

          when {
            isPinCodeEnabled() -> openVerifySheet(
                activity = activity,
                onVerifySuccess = {
                  sSecurityAskPinAlways = !sSecurityAskPinAlways
                  reset(componentContext.androidContext, dialog)
                }
            )
            else -> openCreatePasswordDialog(dialog)
          }
        },
        isSelectable = !isLite,
        selected = sSecurityAskPinAlways,
        actionIcon = when {
          isLite -> R.drawable.ic_rating
          sSecurityAskPinAlways -> R.drawable.ic_done_white_48dp
          else -> 0
        }
    ))

    val hasFingerprint = Reprint.hasFingerprintRegistered()
    options.add(LithoOptionsItem(
        title = R.string.security_option_fingerprint_enabled,
        subtitle = R.string.security_option_fingerprint_enabled_subtitle,
        icon = R.drawable.ic_option_fingerprint,
        listener = {
          when {
            isPinCodeEnabled() -> openVerifySheet(
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
            isPinCodeEnabled() -> openVerifySheet(
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
}