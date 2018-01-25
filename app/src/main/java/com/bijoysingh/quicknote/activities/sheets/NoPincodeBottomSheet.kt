package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.bijoysingh.quicknote.activities.sheets.EnterPincodeBottomSheet.Companion.openCreateSheet
import com.bijoysingh.quicknote.activities.sheets.SecurityOptionsBottomSheet.Companion.hasPinCodeEnabled
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

    sheetTitle.setTextColor(getColor(R.color.dark_tertiary_text, R.color.light_secondary_text))
    sheetDescription.setTextColor(getColor(R.color.dark_tertiary_text, R.color.light_tertiary_text))
    notNow.setTextColor(getColor(R.color.dark_hint_text, R.color.light_tertiary_text))
    neverAsk.setTextColor(getColor(R.color.dark_hint_text, R.color.light_tertiary_text))
    setUp.setTextColor(getColor(R.color.colorAccent, R.color.colorAccentDark))

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