package com.maubis.scarlet.base.main.sheets

import android.app.Dialog
import android.content.DialogInterface
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.github.ajalt.reprint.core.AuthenticationFailureReason
import com.github.ajalt.reprint.core.AuthenticationListener
import com.github.ajalt.reprint.core.Reprint
import com.github.bijoysingh.starter.util.LocaleManager
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.main.sheets.NoPincodeBottomSheet.Companion.ignoreNoPinSheet
import com.maubis.scarlet.base.settings.sheet.SecurityOptionsBottomSheet
import com.maubis.scarlet.base.settings.sheet.SecurityOptionsBottomSheet.Companion.hasPinCodeEnabled
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.ui.ThemedBottomSheetFragment


class EnterPincodeBottomSheet : ThemedBottomSheetFragment() {

  var listener: PincodeListener? = null

  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    if (listener == null) {
      dismiss()
    }

    val title = dialog.findViewById<TextView>(R.id.options_title)
    val action = dialog.findViewById<TextView>(R.id.action_button)
    val enterPin = dialog.findViewById<EditText>(R.id.enter_pin)
    val pinLength = dialog.findViewById<TextView>(R.id.pin_length)
    val fingerprint = dialog.findViewById<ImageView>(R.id.fingerprint)
    val removeBtn = dialog.findViewById<TextView>(R.id.action_remove_button)

    title.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))
    enterPin.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))

    val hintColor = CoreConfig.instance.themeController().get(ThemeColorType.HINT_TEXT)
    enterPin.setHintTextColor(hintColor)
    pinLength.setTextColor(hintColor)

    title.setText(listener!!.getTitle())
    action.setText(listener!!.getActionTitle())
    fingerprint.visibility = if (listener!!.isFingerprintEnabled()) View.VISIBLE else View.INVISIBLE
    enterPin.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(p0: Editable?) {
        // Ignore
      }

      override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        // Ignore
      }

      override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        if (p0 == null) {
          return
        }
        if (p0.length > 4) {
          enterPin.setText(p0.substring(0, 4))
          return
        }
        val text = LocaleManager.toString(p0.length) + " / " + LocaleManager.toString(PIN_LENGTH)
        pinLength.text = text
      }
    })
    removeBtn.setOnClickListener {
      listener!!.onRemoveButtonClick()
      dismiss()
    }
    removeBtn.visibility = if (listener!!.isRemoveButtonEnabled()) View.VISIBLE else View.GONE

    enterPin.setOnEditorActionListener { _, actionId, event ->
      if (event == null) {
        if (actionId != EditorInfo.IME_ACTION_DONE && actionId != EditorInfo.IME_ACTION_NEXT) {
          return@setOnEditorActionListener false
        }
      } else if (actionId == EditorInfo.IME_NULL || actionId == KeyEvent.KEYCODE_ENTER) {
        if (event.getAction() != KeyEvent.ACTION_DOWN) {
          return@setOnEditorActionListener true
        }
      } else {
        return@setOnEditorActionListener false
      }

      if (enterPin.length() != PIN_LENGTH) {
        return@setOnEditorActionListener false
      }

      listener!!.onPasswordRequested(enterPin.text.toString())
      dismiss()
      return@setOnEditorActionListener true
    }

    action.setOnClickListener {
      val pinCode = enterPin.text.toString()
      if (enterPin.length() != PIN_LENGTH) {
        return@setOnClickListener
      }

      listener!!.onPasswordRequested(pinCode)
      dismiss()
    }

    if (listener!!.isFingerprintEnabled()) {
      Reprint.authenticate(object : AuthenticationListener {
        override fun onSuccess(moduleTag: Int) {
          listener!!.onSuccess()
          dismiss()
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
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  override fun onDismiss(dialog: DialogInterface?) {
    super.onDismiss(dialog)
    Reprint.cancelAuthentication()
  }

  override fun onCancel(dialog: DialogInterface?) {
    super.onCancel(dialog)
    Reprint.cancelAuthentication()
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_pin_code

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.enter_code_card)

  companion object {

    const val PIN_LENGTH = 4

    fun openSheet(activity: ThemedActivity, listener: PincodeListener) {
      val sheet = EnterPincodeBottomSheet()

      sheet.listener = listener
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }

    fun openCreateSheet(
        activity: ThemedActivity,
        listener: PincodeSuccessOnlyListener) {
      openSheet(activity, object : PincodeListener {
        override fun getTitle(): Int = R.string.security_sheet_enter_new_pin_title

        override fun getActionTitle(): Int = R.string.security_sheet_button_set

        override fun isFingerprintEnabled(): Boolean = false

        override fun isRemoveButtonEnabled(): Boolean = true

        override fun onRemoveButtonClick() {
          CoreConfig.instance.store().put(SecurityOptionsBottomSheet.KEY_SECURITY_CODE, "")
          CoreConfig.instance.store().put(NoPincodeBottomSheet.KEY_NO_PIN_ASK, false)
          listener.onSuccess()

          if (activity is MainActivity)
            activity.setupData()
        }

        override fun onPasswordRequested(password: String) {
          CoreConfig.instance.store().put(SecurityOptionsBottomSheet.KEY_SECURITY_CODE, password)
          listener.onSuccess()
        }

        override fun onSuccess() {
        }
      })
    }

    fun openVerifySheet(
        activity: ThemedActivity,
        listener: PincodeSuccessListener) {
      openUnlockSheetBase(
          activity,
          listener,
          R.string.security_sheet_enter_current_pin_title,
          R.string.security_sheet_button_verify
      )
    }

    fun openUnlockSheet(
        activity: ThemedActivity,
        listener: PincodeSuccessOnlyListener) {
      if (!hasPinCodeEnabled()) {
        if (ignoreNoPinSheet()) {
          listener.onSuccess()
          return
        }
        NoPincodeBottomSheet.openSheet(activity, listener)
        return
      }

      openUnlockSheetBase(
          activity,
          listener,
          R.string.security_sheet_enter_pin_to_unlock_title,
          R.string.security_sheet_button_unlock
      )
    }

    private fun openUnlockSheetBase(
        activity: ThemedActivity,
        listener: PincodeSuccessOnlyListener,
        title: Int,
        actionTitle: Int) {
      openSheet(activity, object : PincodeListener {
        override fun getTitle(): Int = title

        override fun getActionTitle(): Int = actionTitle

        override fun isFingerprintEnabled(): Boolean {
          return Reprint.hasFingerprintRegistered() &&
              CoreConfig.instance.store().get(SecurityOptionsBottomSheet.KEY_FINGERPRINT_ENABLED, true)
        }

        override fun onPasswordRequested(password: String) {
          val currentPassword = CoreConfig.instance.store().get(SecurityOptionsBottomSheet.KEY_SECURITY_CODE, "")
          if (currentPassword != "" && currentPassword == password) {
            listener.onSuccess()
          } else if (listener is PincodeSuccessListener) {
            listener.onFailure()
          }
        }

        override fun isRemoveButtonEnabled(): Boolean = false

        override fun onRemoveButtonClick() {
        }

        override fun onSuccess() {
          listener.onSuccess()
        }
      })
    }
  }

  interface PincodeSuccessOnlyListener {
    fun onSuccess()
  }

  interface PincodeSuccessListener : PincodeSuccessOnlyListener {

    fun onFailure()
  }

  interface PincodeListener : PincodeSuccessOnlyListener {
    fun getTitle(): Int

    fun getActionTitle(): Int

    fun isFingerprintEnabled(): Boolean

    fun isRemoveButtonEnabled(): Boolean

    fun onPasswordRequested(password: String): Unit

    fun onRemoveButtonClick(): Unit
  }
}