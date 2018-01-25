package com.bijoysingh.quicknote.activities.sheets

import android.app.Dialog
import android.util.TypedValue
import android.widget.TextView
import com.bijoysingh.quicknote.R
import com.bijoysingh.quicknote.activities.MainActivity
import com.bijoysingh.quicknote.activities.ThemedActivity
import com.github.bijoysingh.starter.prefs.DataStore

class TextSizeBottomSheet : CounterBottomSheetBase() {
  override fun getMinCountLimit(): Int = TEXT_SIZE_MIN

  override fun getMaxCountLimit(): Int = TEXT_SIZE_MAX

  override fun getDefaultCount(dataStore: DataStore): Int = getDefaultTextSize(dataStore)

  override fun onCountChange(
      dialog: Dialog,
      activity: ThemedActivity,
      dataStore: DataStore,
      count: Int) {
    dataStore.put(KEY_TEXT_SIZE, count)
    updateExample(dialog.findViewById<TextView>(R.id.options_example), count)
  }

  fun updateExample(example: TextView, fontSize: Int) {
    example.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
  }

  override fun setupFurther(dialog: Dialog, dataStore: DataStore) {
    val example = dialog.findViewById<TextView>(R.id.options_example)
    updateExample(example, getDefaultTextSize(dataStore))
    example.setBackgroundColor(getColor(R.color.material_grey_200, R.color.material_grey_700))
    example.setTextColor(getColor(R.color.dark_secondary_text, R.color.light_secondary_text))
  }

  override fun getLayout(): Int = R.layout.bottom_sheet_text_size

  companion object {
    const val KEY_TEXT_SIZE = "KEY_TEXT_SIZE"
    const val TEXT_SIZE_DEFAULT = 16
    const val TEXT_SIZE_MIN = 12
    const val TEXT_SIZE_MAX = 24

    fun openSheet(activity: MainActivity) {
      val sheet = TextSizeBottomSheet()
      sheet.show(activity.supportFragmentManager, sheet.tag)
    }

    fun getDefaultTextSize(dataStore: DataStore): Int = dataStore.get(KEY_TEXT_SIZE, TEXT_SIZE_DEFAULT)
  }
}