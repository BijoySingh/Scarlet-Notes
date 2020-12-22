package com.maubis.scarlet.base.settings.sheet

import android.app.Dialog
import android.graphics.Color
import com.facebook.litho.ClickEvent
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.Row
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.OnEvent
import com.facebook.litho.annotations.Prop
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.support.sheets.LithoBottomSheet
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetTitle
import com.maubis.scarlet.base.support.specs.BottomSheetBar
import com.maubis.scarlet.base.support.specs.EmptySpec
import com.maubis.scarlet.base.support.specs.RoundIcon
import com.maubis.scarlet.base.support.specs.separatorSpec
import com.maubis.scarlet.base.support.ui.ColorUtil

class ColorPickerDefaultController(
  val title: Int = R.string.note_option_default_color,
  var selectedColor: Int = Color.WHITE,
  val colors: List<IntArray> = listOf(intArrayOf(Color.WHITE)),
  val onColorSelected: (Int) -> Unit = {},
  val columns: Int = 6)

@LayoutSpec
object ColorPickerItemSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop color: Int,
    @Prop isSelected: Boolean): Component {
    val row = Row.create(context)
      .alignItems(YogaAlign.CENTER)
      .child(RoundIcon.create(context)
               .iconRes(
                 when {
                   isSelected -> R.drawable.ic_done_white_48dp
                   color == Color.TRANSPARENT -> R.drawable.icon_no_color
                   else -> R.drawable.ic_empty
                 })
               .bgColor(color)
               .showBorder(true)
               .iconColorRes(if (ColorUtil.isLightColored(color)) R.color.dark_tertiary_text else R.color.light_secondary_text)
               .iconSizeRes(R.dimen.toolbar_round_icon_size)
               .iconPaddingRes(R.dimen.toolbar_round_icon_padding)
               .onClick { }
               .isClickDisabled(true))
    row.clickHandler(ColorPickerItem.onItemClick(context))
    return row.build()
  }

  @OnEvent(ClickEvent::class)
  fun onItemClick(context: ComponentContext, @Prop color: Int, @Prop onColorSelected: (Int) -> Unit) {
    onColorSelected(color)
  }
}

class ColorPickerBottomSheet : LithoBottomSheet() {

  var config: ColorPickerDefaultController = ColorPickerDefaultController()

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val column = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .child(
        getLithoBottomSheetTitle(componentContext)
          .textRes(config.title)
          .marginDip(YogaEdge.HORIZONTAL, 0f))

    config.colors.forEachIndexed { colorArrayIndex, colorArray ->
      var flex: Row.Builder? = null
      colorArray.forEachIndexed { index, color ->
        if (index % config.columns == 0) {
          column.child(flex)
          flex = Row.create(componentContext)
            .widthPercent(100f)
            .alignItems(YogaAlign.CENTER)
            .paddingDip(YogaEdge.VERTICAL, 8f)
        }

        flex?.child(
          ColorPickerItem.create(componentContext)
            .color(color)
            .isSelected(color == config.selectedColor)
            .onColorSelected { selectedColor ->
              config.selectedColor = selectedColor
              config.onColorSelected(selectedColor)
              reset(componentContext.androidContext, dialog)
            }
            .flexGrow(1f))
      }
      column.child(flex)

      if (colorArrayIndex != config.colors.size - 1) {
        column.child(separatorSpec(componentContext).alpha(0.5f))
      } else {
        column.child(EmptySpec.create(componentContext).widthPercent(100f).heightDip(24f))
      }
    }
    column.child(BottomSheetBar.create(componentContext)
                   .primaryActionRes(R.string.import_export_layout_exporting_done)
                   .onPrimaryClick {
                     dismiss()
                   }.paddingDip(YogaEdge.VERTICAL, 8f))
    return column.build()
  }
}
