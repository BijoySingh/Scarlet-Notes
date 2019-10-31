package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppPreferences
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.specs.BottomSheetBar
import com.maubis.scarlet.base.support.specs.CounterChooser
import com.maubis.scarlet.base.support.ui.ThemeColorType

const val STORE_KEY_TEXT_SIZE = "KEY_TEXT_SIZE"
const val TEXT_SIZE_DEFAULT = 16
const val TEXT_SIZE_MIN = 12
const val TEXT_SIZE_MAX = 24

var sEditorTextSize: Int
  get() = sAppPreferences.get(STORE_KEY_TEXT_SIZE, TEXT_SIZE_DEFAULT)
  set(value) = sAppPreferences.put(STORE_KEY_TEXT_SIZE, value)

class FontSizeBottomSheet : LithoBottomSheet() {

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val activity = context as MainActivity
    val component = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .child(
        getLithoBottomSheetTitle(componentContext)
          .textRes(R.string.note_option_font_size)
          .marginDip(YogaEdge.HORIZONTAL, 0f))
      .child(
        Text.create(componentContext)
          .textSizeDip(sEditorTextSize.toFloat())
          .marginDip(YogaEdge.BOTTOM, 16f)
          .textRes(R.string.note_option_font_size_example)
          .textColor(sAppTheme.get(ThemeColorType.TERTIARY_TEXT)))
      .child(CounterChooser.create(componentContext)
               .value(sEditorTextSize)
               .minValue(TEXT_SIZE_MIN)
               .maxValue(TEXT_SIZE_MAX)
               .onValueChange { value ->
                 sEditorTextSize = value
                 reset(activity, dialog)
               }
               .paddingDip(YogaEdge.VERTICAL, 16f))
      .child(BottomSheetBar.create(componentContext)
               .primaryActionRes(R.string.import_export_layout_exporting_done)
               .onPrimaryClick {
                 dismiss()
               }.paddingDip(YogaEdge.VERTICAL, 8f))
    return component.build()
  }
}