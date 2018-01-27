package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.activities.sheets.EnterPincodeBottomSheet.Companion.openCreateSheet
import com.bijoysingh.quicknote.activities.sheets.SecurityOptionsBottomSheet.Companion.hasPinCodeEnabled
import com.bijoysingh.quicknote.utils.ThemeColorType
import com.github.bijoysingh.starter.prefs.DataStore


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

    sheetTitle.setTextColor(theme().get(themedContext(), ThemeColorType.SECONDARY_TEXT))
    sheetDescription.setTextColor(theme().get(themedContext(), ThemeColorType.TERTIARY_TEXT))
    notNow.setTextColor(theme().get(themedContext(), ThemeColorType.DISABLED_TEXT))
    neverAsk.setTextColor(theme().get(themedContext(), ThemeColorType.DISABLED_TEXT))
    setUp.setTextColor(theme().get(themedContext(), ThemeColorType.ACCENT_TEXT))

    notNow.setOnClickListener {
      listener?.onSuccess()
      dismiss()
    }

    setUp.setOnClickListener {
      openCreateSheet(
          activity,
          listener ?: getEmptySuccessListener(),
          DataStore.get(context))
      dismiss()
    }

    neverAsk.setOnClickListener {
      DataStore.get(context).put(KEY_NO_PIN_ASK, true)
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

    fun ignoreNoPinSheet(dataStore: DataStore): Boolean {
      return dataStore.get(KEY_NO_PIN_ASK, false)
    }

    fun maybeOpenSheet(activity: ThemedActivity) {
      val dataStore = DataStore.get(activity)
      if (hasPinCodeEnabled(dataStore) || ignoreNoPinSheet(dataStore)) {
        return
      }
      openSheet(activity, getEmptySuccessListener())
    }
  }
}