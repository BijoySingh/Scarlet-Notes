package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.content.DialogInterface
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.github.ajalt.reprint.core.AuthenticationFailureReason
import com.github.ajalt.reprint.core.AuthenticationListener
import com.github.ajalt.reprint.core.Reprint
import com.github.bijoysingh.starter.util.LocaleManager


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

    title.setTextColor(getColor(R.color.dark_secondary_text, R.color.light_secondary_text))
    action.setTextColor(getColor(R.color.colorAccent, R.color.colorAccentDark))
    enterPin.setTextColor(getColor(R.color.dark_secondary_text, R.color.light_secondary_text))
    enterPin.setHintTextColor(getColor(R.color.dark_hint_text, R.color.light_hint_text))
    pinLength.setTextColor(getColor(R.color.dark_hint_text, R.color.light_hint_text))
    fingerprint.setColorFilter(getColor(R.color.dark_hint_text, R.color.light_hint_text))

    title.setText(listener!!.getTitle())
    action.setText(listener!!.getActionTitle())
    fingerprint.visibility = if (listener!!.isFingerprintEnabled()) View.VISIBLE else View.INVISIBLE
    enterPin.addTextChangedListener(object: TextWatcher {
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
    action.setOnClickListener {
      val pinCode = enterPin.text.toString()
      if (enterPin.length() != PIN_LENGTH) {
        return@setOnClickListener
      }

      listener!!.onPasswordRequested(pinCode)
    }

    if (listener!!.isFingerprintEnabled()) {
      Reprint.authenticate(object: AuthenticationListener {
        override fun onSuccess(moduleTag: Int) {
          listener!!.onFingerprintEntered()
        }

        override fun onFailure(failureReason: AuthenticationFailureReason?, fatal: Boolean, errorMessage: CharSequence?, moduleTag: Int, errorCode: Int) {
          // Ignore
        }
      })
    }
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

  companion object {

    const val PIN_LENGTH = 4

    fun openSheet(activity: MainActivity, listener: PincodeListener) {
      val sheet = EnterPincodeBottomSheet()
      sheet.isNightMode = activity.isNightMode
      sheet.listener = listener
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }
  }

  interface PincodeListener {
    fun getTitle(): Int

    fun getActionTitle(): Int

    fun isFingerprintEnabled(): Boolean

    fun onPasswordRequested(password: String): Unit

    fun onFingerprintEntered(): Unit
  }
}