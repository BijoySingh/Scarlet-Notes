package com.maubis.scarlet.base.main.sheets

import android.app.Dialog
import android.widget.TextView
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.main.sheets.EnterPincodeBottomSheet.Companion.openCreateSheet
import com.maubis.scarlet.base.settings.sheet.SecurityOptionsBottomSheet.Companion.hasPinCodeEnabled
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity
import com.maubis.scarlet.base.support.ui.ThemedBottomSheetFragment


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

    sheetTitle.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))
    sheetDescription.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT))

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
      CoreConfig.instance.store().put(KEY_NO_PIN_ASK, true)
      listener?.onSuccess()
      dismiss()
    }
    makeBackgroundTransparent(dialog, R.id.root_layout)
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_no_pincode

  override fun getBackgroundCardViewIds(): Array<Int> = arrayOf(R.id.no_pin_card)

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
      return CoreConfig.instance.store().get(KEY_NO_PIN_ASK, false)
    }

    fun maybeOpenSheet(activity: ThemedActivity) {
      if (hasPinCodeEnabled() || ignoreNoPinSheet()) {
        return
      }
      openSheet(activity, getEmptySuccessListener())
    }
  }
}