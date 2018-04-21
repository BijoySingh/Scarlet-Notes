package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import android.view.View
import com.github.ajalt.reprint.core.Reprint
import com.github.bijoysingh.starter.util.TextUtils
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.main.sheets.EnterPincodeBottomSheet
import com.maubis.scarlet.base.main.sheets.EnterPincodeBottomSheet.Companion.openCreateSheet
import com.maubis.scarlet.base.main.sheets.EnterPincodeBottomSheet.Companion.openVerifySheet
import com.maubis.scarlet.base.support.option.OptionsItem
import com.maubis.scarlet.base.support.sheets.OptionItemBottomSheetBase
import com.maubis.scarlet.base.support.ui.ThemedActivity

class SecurityOptionsBottomSheet : OptionItemBottomSheetBase() {
  override fun setupViewWithDialog(dialog: Dialog) {
    setOptions(dialog, getOptions())
    setOptionTitle(dialog, R.string.security_option_title)
  }

  private fun getOptions(): List<OptionsItem> {
    val options = ArrayList<OptionsItem>()
    options.add(OptionsItem(
        title = R.string.security_option_set_pin_code,
        subtitle = R.string.security_option_set_pin_code_subtitle,
        icon = R.drawable.ic_option_security,
        listener = View.OnClickListener {
          val currentPinCode = CoreConfig.instance.store().get(KEY_SECURITY_CODE, "")
          val hasPinCode = !TextUtils.isNullOrEmpty(currentPinCode)
          if (hasPinCode) {
            openResetPasswordDialog(dialog)
          } else {
            openCreatePasswordDialog(dialog)
          }
        },
        enabled = !TextUtils.isNullOrEmpty(CoreConfig.instance.store().get(KEY_SECURITY_CODE, ""))
    ))
    val hasFingerprint = Reprint.hasFingerprintRegistered()
    options.add(OptionsItem(
        title = R.string.security_option_fingerprint_enabled,
        subtitle = R.string.security_option_fingerprint_enabled_subtitle,
        icon = R.drawable.ic_option_fingerprint,
        listener = View.OnClickListener {
          val currentPinCode = CoreConfig.instance.store().get(KEY_SECURITY_CODE, "")
          val hasPinCode = !TextUtils.isNullOrEmpty(currentPinCode)
          if (hasPinCode) {
            openVerifyPasswordDialog(
                object : EnterPincodeBottomSheet.PincodeSuccessOnlyListener {
                  override fun onSuccess() {
                    CoreConfig.instance.store().put(KEY_FINGERPRINT_ENABLED, false)
                    reset(dialog)
                  }
                }
            )
          } else {
            CoreConfig.instance.store().put(KEY_FINGERPRINT_ENABLED, false)
            reset(dialog)
          }
        },
        visible = CoreConfig.instance.store().get(KEY_FINGERPRINT_ENABLED, true) && hasFingerprint,
        enabled = true
    ))
    options.add(OptionsItem(
        title = R.string.security_option_fingerprint_disabled,
        subtitle = R.string.security_option_fingerprint_disabled_subtitle,
        icon = R.drawable.ic_option_fingerprint,
        listener = View.OnClickListener {
          val currentPinCode = CoreConfig.instance.store().get(KEY_SECURITY_CODE, "")
          val hasPinCode = !TextUtils.isNullOrEmpty(currentPinCode)
          if (hasPinCode) {
            openVerifyPasswordDialog(
                object : EnterPincodeBottomSheet.PincodeSuccessOnlyListener {
                  override fun onSuccess() {
                    CoreConfig.instance.store().put(KEY_FINGERPRINT_ENABLED, true)
                    reset(dialog)
                  }
                }
            )
          } else {
            CoreConfig.instance.store().put(KEY_FINGERPRINT_ENABLED, true)
            reset(dialog)
          }
        },
        visible = !CoreConfig.instance.store().get(KEY_FINGERPRINT_ENABLED, true) && hasFingerprint
    ))
    return options
  }

  fun openCreatePasswordDialog(dialog: Dialog) {
    val activity = context as ThemedActivity
    openCreateSheet(
        activity,
        object : EnterPincodeBottomSheet.PincodeSuccessOnlyListener {
          override fun onSuccess() {
            reset(dialog)
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

  override fun getLayout(): Int = R.layout.layout_options_sheet

  companion object {

    const val KEY_SECURITY_CODE = "KEY_SECURITY_CODE"
    const val KEY_FINGERPRINT_ENABLED = "KEY_FINGERPRINT_ENABLED"

    fun openSheet(activity: MainActivity) {
      val sheet = SecurityOptionsBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }

    fun hasPinCodeEnabled(): Boolean {
      val currentPinCode = CoreConfig.instance.store().get(KEY_SECURITY_CODE, "")
      return !TextUtils.isNullOrEmpty(currentPinCode)
    }
  }
}