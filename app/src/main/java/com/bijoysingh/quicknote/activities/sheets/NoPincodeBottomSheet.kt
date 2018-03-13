package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.widget.TextView
import com.bijoysingh.quicknote.MaterialNotes.Companion.appTheme
import com.bijoysingh.quicknote.MaterialNotes.Companion.userPreferences
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.activities.sheets.EnterPincodeBottomSheet.Companion.openCreateSheet
import com.bijoysingh.quicknote.activities.sheets.SecurityOptionsBottomSheet.Companion.hasPinCodeEnabled
import com.bijoysingh.quicknote.utils.ThemeColorType


class NoPincodeBottomSheet : ThemedBottomSheetFragment() {

  var listener: EnterPincodeBottomSheet.PincodeSuccessOnlyListener? = null
  override fun getBackgroundView(): Int {
    return R.id.container_layout
  }

  override fun setupView(dialog: Dialog?) {
    super.setupView(dialog)
    if (dialog == null) {
      return
    }

    val activity = context as ThemedActivity

    val sheetTitle = dialog.findViewById<TextView>(R.id.no_pincode_title)
    val sheetDescription = dialog.findViewById<TextView>(R.id.no_pincode_description)
    val setUp = dialog.findViewById<TextView>(R.id.no_pincode_set_up)
    val notNow = dialog.findViewById<TextView>(R.id.no_pincode_not_now)
    val neverAsk = dialog.findViewById<TextView>(R.id.no_pincode_dont_ask)

    sheetTitle.setTextColor(appTheme().get(ThemeColorType.SECONDARY_TEXT))
    sheetDescription.setTextColor(appTheme().get(ThemeColorType.TERTIARY_TEXT))
    notNow.setTextColor(appTheme().get(ThemeColorType.DISABLED_TEXT))
    neverAsk.setTextColor(appTheme().get(ThemeColorType.DISABLED_TEXT))
    setUp.setTextColor(appTheme().get(ThemeColorType.ACCENT_TEXT))

    notNow.setOnClickListener {
      listener?.onSuccess()
      dismiss()
    }

    setUp.setOnClickListener {
      openCreateSheet(
          activity,
          listener ?: getEmptySuccessListener())
      dismiss()
    }

    neverAsk.setOnClickListener {
      userPreferences().put(KEY_NO_PIN_ASK, true)
      listener?.onSuccess()
      dismiss()
    }
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_no_pincode

  companion object {
    const val KEY_NO_PIN_ASK = "KEY_NO_PIN_ASK"

    fun openSheet(activity: ThemedActivity,
                  listener: EnterPincodeBottomSheet.PincodeSuccessOnlyListener) {
      val sheet = NoPincodeBottomSheet()

      sheet.listener = listener
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }

    fun getEmptySuccessListener(): EnterPincodeBottomSheet.PincodeSuccessOnlyListener {
      return object : EnterPincodeBottomSheet.PincodeSuccessOnlyListener {
        override fun onSuccess() {
          // Ignore this
        }
      }
    }

    fun ignoreNoPinSheet(): Boolean {
      return userPreferences().get(KEY_NO_PIN_ASK, false)
    }

    fun maybeOpenSheet(activity: ThemedActivity) {
      if (hasPinCodeEnabled() || ignoreNoPinSheet()) {
        return
      }
      openSheet(activity, getEmptySuccessListener())
    }
  }
}