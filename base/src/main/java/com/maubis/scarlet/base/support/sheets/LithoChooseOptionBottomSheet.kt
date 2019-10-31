package com.maubis.scarlet.base.support.sheets

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import com.facebook.litho.ClickEvent
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.Row
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.OnEvent
import com.facebook.litho.annotations.Prop
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.support.specs.RoundIcon
import com.maubis.scarlet.base.support.ui.ThemeColorType

class LithoChooseOptionsItem(
  val title: Int,
  val selected: Boolean = false,
  val listener: () -> Unit)

@LayoutSpec
object ChooseOptionItemLayoutSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop option: LithoChooseOptionsItem): Component {
    val titleColor = sAppTheme.get(ThemeColorType.SECONDARY_TEXT)
    val selectedColor = context.getColor(R.color.colorAccent)

    val row = Row.create(context)
      .widthPercent(100f)
      .alignItems(YogaAlign.CENTER)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .paddingDip(YogaEdge.VERTICAL, 12f)
      .child(
        Text.create(context)
          .textRes(option.title)
          .textSizeRes(R.dimen.font_size_normal)
          .typeface(CoreConfig.FONT_MONSERRAT)
          .textStyle(Typeface.BOLD)
          .textColor(titleColor)
          .flexGrow(1f))
      .child(RoundIcon.create(context)
               .iconRes(R.drawable.ic_done_white_48dp)
               .bgColor(if (option.selected) selectedColor else titleColor)
               .bgAlpha(if (option.selected) 200 else 25)
               .iconAlpha(if (option.selected) 1f else 0.6f)
               .iconColor(if (option.selected) Color.WHITE else titleColor)
               .iconSizeRes(R.dimen.toolbar_round_small_icon_size)
               .iconPaddingRes(R.dimen.toolbar_round_small_icon_padding)
               .onClick { }
               .isClickDisabled(true)
               .marginDip(YogaEdge.START, 12f))
    row.clickHandler(ChooseOptionItemLayout.onItemClick(context))
    return row.build()
  }

  @OnEvent(ClickEvent::class)
  fun onItemClick(context: ComponentContext, @Prop onClick: () -> Unit) {
    onClick()
  }
}

abstract class LithoChooseOptionBottomSheet : LithoBottomSheet() {

  abstract fun title(): Int
  abstract fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoChooseOptionsItem>

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val column = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .child(getLithoBottomSheetTitle(componentContext).textRes(title()))
    getOptions(componentContext, dialog).forEach {
      column.child(ChooseOptionItemLayout.create(componentContext)
                     .option(it)
                     .onClick {
                       it.listener()
                       reset(componentContext.androidContext, dialog)
                     })
    }
    return column.build()
  }
}
