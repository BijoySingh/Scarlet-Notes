package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.github.bijoysingh.starter.prefs.DataStore
import com.github.bijoysingh.starter.util.LocaleManager


class LineCountBottomSheet : CounterBottomSheetBase() {
  override fun getMinCountLimit(): Int = LINE_COUNT_MIN

  override fun getMaxCountLimit(): Int = LINE_COUNT_MAX

  override fun getDefaultCount(dataStore: DataStore): Int = getDefaultLineCount(dataStore)


  override fun onCountChange(dialog: Dialog, activity: ThemedActivity, dataStore: DataStore, count: Int) {
    dataStore.put(KEY_LINE_COUNT, count)
    (activity as MainActivity).notifyAdapterExtraChanged()
  }

  companion object {
    const val KEY_LINE_COUNT = "KEY_LINE_COUNT"
    const val LINE_COUNT_DEFAULT = 7
    const val LINE_COUNT_MIN = 2
    const val LINE_COUNT_MAX = 15

    fun openSheet(activity: MainActivity) {
      val sheet = LineCountBottomSheet()
      sheet.isNightMode = activity.isNightMode
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }

    fun getDefaultLineCount(dataStore: DataStore): Int = dataStore.get(KEY_LINE_COUNT, LINE_COUNT_DEFAULT)
  }
}