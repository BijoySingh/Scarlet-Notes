package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import com.bijoysingh.quicknote.MaterialNotes.Companion.userPreferences
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.activities.ThemedActivity


class LineCountBottomSheet : CounterBottomSheetBase() {
  override fun getMinCountLimit(): Int = LINE_COUNT_MIN

  override fun getMaxCountLimit(): Int = LINE_COUNT_MAX

  override fun getDefaultCount(): Int = getDefaultLineCount()


  override fun onCountChange(dialog: Dialog, activity: ThemedActivity, count: Int) {
    userPreferences().put(KEY_LINE_COUNT, count)
    (activity as MainActivity).notifyAdapterExtraChanged()
  }

  companion object {
    const val KEY_LINE_COUNT = "KEY_LINE_COUNT"
    const val LINE_COUNT_DEFAULT = 7
    const val LINE_COUNT_MIN = 2
    const val LINE_COUNT_MAX = 15

    fun openSheet(activity: MainActivity) {
      val sheet = LineCountBottomSheet()

      sheet.show(activity.supportFragmentManager, sheet.tag)
    }

    fun getDefaultLineCount(): Int = userPreferences().get(KEY_LINE_COUNT, LINE_COUNT_DEFAULT)
  }
}