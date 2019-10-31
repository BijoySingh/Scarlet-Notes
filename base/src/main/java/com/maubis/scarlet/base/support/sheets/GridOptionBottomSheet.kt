package com.maubis.scarlet.base.support.sheets

import android.app.Dialog
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.support.specs.GridSectionItem
import com.maubis.scarlet.base.support.specs.GridSectionView

abstract class GridOptionBottomSheet : LithoBottomSheet() {

  abstract fun title(): Int
  abstract fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<GridSectionItem>

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val column = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .child(getLithoBottomSheetTitle(componentContext).textRes(title()))

    val options = getOptions(componentContext, dialog)
    var index = 0
    options.forEach {
      index++
      column.child(
        GridSectionView.create(componentContext)
          .marginDip(YogaEdge.HORIZONTAL, 12f)
          .marginDip(YogaEdge.VERTICAL, 8f)
          .iconSizeRes(R.dimen.primary_round_icon_size)
          .showSeparator(index != options.size)
          .section(it))
    }

    return column.build()
  }
}
