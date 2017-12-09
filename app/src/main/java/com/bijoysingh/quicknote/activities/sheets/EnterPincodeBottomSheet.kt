package com.bijoysingh.quicknote.activities.sheets

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
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.github.ajalt.reprint.core.AuthenticationFailureReason
import com.github.ajalt.reprint.core.AuthenticationListener
import com.github.ajalt.reprint.core.Reprint
import com.github.bijoysingh.starter.prefs.DataStore
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
    val removeBtn = dialog.findViewById<TextView>(R.id.action_remove_button)

    title.setTextColor(getColor(R.color.dark_secondary_text, R.color.light_secondary_text))
    action.setTextColor(getColor(R.color.colorAccent, R.color.colorAccentDark))
    enterPin.setTextColor(getColor(R.color.dark_secondary_text, R.color.light_secondary_text))
    enterPin.setHintTextColor(getColor(R.color.dark_hint_text, R.color.light_hint_text))
    pinLength.setTextColor(getColor(R.color.dark_hint_text, R.color.light_hint_text))
    fingerprint.setColorFilter(getColor(R.color.dark_hint_text, R.color.light_hint_text))

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
    removeBtn.visibility = if (listener!!.isRemoveButtonEnabled()) View.VISIBLE else View.INVISIBLE

    enterPin.setOnEditorActionListener { view, actionId, event ->
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

    fun openSheet(activity: ThemedActivity, listener: PincodeListener) {
      val sheet = EnterPincodeBottomSheet()
      sheet.isNightMode = activity.isNightMode
      sheet.listener = listener
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }

    fun openCreateSheet(
        activity: ThemedActivity,
        listener: PincodeSuccessOnlyListener,
        dataStore: DataStore) {
      openSheet(activity, object : PincodeListener {
        override fun getTitle(): Int = R.string.security_sheet_enter_new_pin_title

        override fun getActionTitle(): Int = R.string.security_sheet_button_set

        override fun isFingerprintEnabled(): Boolean = false

        override fun isRemoveButtonEnabled(): Boolean = true

        override fun onRemoveButtonClick() {
          dataStore.put(SecurityOptionsBottomSheet.KEY_SECURITY_CODE, "")
          listener.onSuccess()
        }

        override fun onPasswordRequested(password: String) {
          dataStore.put(SecurityOptionsBottomSheet.KEY_SECURITY_CODE, password)
          listener.onSuccess()
        }

        override fun onSuccess() {
        }
      })
    }

    fun openVerifySheet(
        activity: ThemedActivity,
        listener: PincodeSuccessListener,
        dataStore: DataStore) {
      openUnlockSheetBase(
          activity,
          listener,
          dataStore,
          R.string.security_sheet_enter_current_pin_title,
          R.string.security_sheet_button_verify
      )
    }

    fun openUnlockSheet(
        activity: ThemedActivity,
        listener: PincodeSuccessOnlyListener,
        dataStore: DataStore) {
      openUnlockSheetBase(
          activity,
          listener,
          dataStore,
          R.string.security_sheet_enter_pin_to_unlock_title,
          R.string.security_sheet_button_unlock
      )
    }

    private fun openUnlockSheetBase(
        activity: ThemedActivity,
        listener: PincodeSuccessOnlyListener,
        dataStore: DataStore,
        title: Int,
        actionTitle: Int) {
      openSheet(activity, object : PincodeListener {
        override fun getTitle(): Int = title

        override fun getActionTitle(): Int = actionTitle

        override fun isFingerprintEnabled(): Boolean {
          return Reprint.hasFingerprintRegistered() &&
              dataStore.get(SecurityOptionsBottomSheet.KEY_FINGERPRINT_ENABLED, true)
        }

        override fun onPasswordRequested(password: String) {
          val currentPassword = dataStore.get(SecurityOptionsBottomSheet.KEY_SECURITY_CODE, password)
          if (currentPassword == password) {
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