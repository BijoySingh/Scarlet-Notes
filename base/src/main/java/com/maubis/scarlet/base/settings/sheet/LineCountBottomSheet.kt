package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.MainActivity
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.specs.BottomSheetBar
import com.maubis.scarlet.base.support.specs.CounterChooser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

const val STORE_KEY_LINE_COUNT = "KEY_LINE_COUNT"
const val LINE_COUNT_DEFAULT = 7
const val LINE_COUNT_MIN = 2
const val LINE_COUNT_MAX = 15

var sNoteItemLineCount: Int
  get() = ApplicationBase.instance.store().get(STORE_KEY_LINE_COUNT, LINE_COUNT_DEFAULT)
  set(value) = ApplicationBase.instance.store().put(STORE_KEY_LINE_COUNT, value)

class LineCountBottomSheet : LithoBottomSheet() {

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val activity = context as MainActivity
    val component = Column.create(componentContext)
        .widthPercent(100f)
        .paddingDip(YogaEdge.VERTICAL, 8f)
        .paddingDip(YogaEdge.HORIZONTAL, 20f)
        .child(getLithoBottomSheetTitle(componentContext)
            .textRes(R.string.note_option_number_lines)
            .marginDip(YogaEdge.HORIZONTAL, 0f))
        .child(CounterChooser.create(componentContext)
            .value(sNoteItemLineCount)
            .minValue(LINE_COUNT_MIN)
            .maxValue(LINE_COUNT_MAX)
            .onValueChange { value ->
              sNoteItemLineCount = value
              GlobalScope.launch(Dispatchers.Main) { reset(activity, dialog) }
              GlobalScope.launch(Dispatchers.Main) { activity.notifyAdapterExtraChanged() }
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