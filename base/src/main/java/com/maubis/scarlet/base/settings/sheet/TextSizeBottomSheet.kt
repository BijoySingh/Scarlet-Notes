package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import android.util.TypedValue
import android.widget.TextView
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.support.sheets.CounterBottomSheetBase
import com.maubis.scarlet.base.support.ui.ThemeColorType
import com.maubis.scarlet.base.support.ui.ThemedActivity

class TextSizeBottomSheet : CounterBottomSheetBase() {
  override fun getMinCountLimit(): Int = TEXT_SIZE_MIN

  override fun getMaxCountLimit(): Int = TEXT_SIZE_MAX

  override fun getDefaultCount(): Int = getDefaultTextSize()

  override fun onCountChange(
      dialog: Dialog,
      activity: ThemedActivity,
      count: Int) {
    CoreConfig.instance.store().put(KEY_TEXT_SIZE, count)
    updateExample(dialog.findViewById<TextView>(R.id.options_example), count)
  }

  fun updateExample(example: TextView, fontSize: Int) {
    example.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
  }

  override fun setupFurther(dialog: Dialog) {
    val example = dialog.findViewById<TextView>(R.id.options_example)
    updateExample(example, getDefaultTextSize())
    example.setBackgroundColor(CoreConfig.instance.themeController().get(themedContext(), R.color.material_grey_200, R.color.material_grey_700))
    example.setTextColor(CoreConfig.instance.themeController().get(ThemeColorType.SECONDARY_TEXT))
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

    fun getDefaultTextSize(): Int = CoreConfig.instance.store().get(KEY_TEXT_SIZE, TEXT_SIZE_DEFAULT)
  }
}