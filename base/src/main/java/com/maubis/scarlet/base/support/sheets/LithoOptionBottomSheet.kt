package com.maubis.scarlet.base.support.sheets

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface.BOLD
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
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTypeface
import com.maubis.scarlet.base.support.specs.RoundIcon
import com.maubis.scarlet.base.support.ui.ThemeColorType

class LithoOptionsItem(
  val title: Int,
  val subtitle: Int,
  val content: String = "",
  val icon: Int,
  val isSelectable: Boolean = false,
  val selected: Boolean = false,
  val actionIcon: Int = 0,
  val visible: Boolean = true,
  val listener: () -> Unit)

@LayoutSpec
object OptionItemLayoutSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop option: LithoOptionsItem): Component {
    val titleColor = sAppTheme.get(ThemeColorType.SECONDARY_TEXT)
    val subtitleColor = sAppTheme.get(ThemeColorType.HINT_TEXT)
    val selectedColor = sAppTheme.get(ThemeColorType.ACCENT_TEXT)

    val subtitle = when (option.subtitle) {
      0 -> option.content
      else -> context.getString(option.subtitle)
    }

    val row = Row.create(context)
      .widthPercent(100f)
      .alignItems(YogaAlign.CENTER)
      .paddingDip(YogaEdge.HORIZONTAL, 20f)
      .paddingDip(YogaEdge.VERTICAL, 12f)
      .child(
        RoundIcon.create(context)
          .iconRes(option.icon)
          .bgColor(titleColor)
          .iconColor(titleColor)
          .iconSizeRes(R.dimen.toolbar_round_icon_size)
          .iconPaddingRes(R.dimen.toolbar_round_icon_padding)
          .bgAlpha(15)
          .onClick { }
          .isClickDisabled(true)
          .marginDip(YogaEdge.END, 16f))
      .child(
        Column.create(context)
          .flexGrow(1f)
          .child(
            Text.create(context)
              .textRes(option.title)
              .textSizeRes(R.dimen.font_size_normal)
              .typeface(sAppTypeface.title())
              .textStyle(BOLD)
              .textColor(titleColor))
          .child(
            Text.create(context)
              .text(subtitle)
              .textSizeRes(R.dimen.font_size_small)
              .typeface(sAppTypeface.title())
              .textColor(subtitleColor)))

    if (option.isSelectable) {
      row.child(RoundIcon.create(context)
                  .iconRes(if (option.actionIcon == 0) R.drawable.ic_done_white_48dp else option.actionIcon)
                  .bgColor(if (option.selected) selectedColor else titleColor)
                  .bgAlpha(if (option.selected) 200 else 25)
                  .iconAlpha(if (option.selected) 1f else 0.6f)
                  .iconColor(if (option.selected) Color.WHITE else titleColor)
                  .iconSizeRes(R.dimen.toolbar_round_small_icon_size)
                  .iconPaddingRes(R.dimen.toolbar_round_small_icon_padding)
                  .onClick { }
                  .isClickDisabled(true)
                  .marginDip(YogaEdge.START, 12f))
    } else if (!option.isSelectable && option.actionIcon != 0) {
      row.child(RoundIcon.create(context)
                  .iconRes(option.actionIcon)
                  .bgColor(titleColor)
                  .bgAlpha(25)
                  .iconAlpha(0.9f)
                  .iconColor(titleColor)
                  .iconSizeRes(R.dimen.toolbar_round_small_icon_size)
                  .iconPaddingRes(R.dimen.toolbar_round_small_icon_padding)
                  .onClick { }
                  .isClickDisabled(true)
                  .marginDip(YogaEdge.START, 12f))
    }

    row.clickHandler(OptionItemLayout.onItemClick(context))
    return row.build()
  }

  @OnEvent(ClickEvent::class)
  fun onItemClick(context: ComponentContext, @Prop onClick: () -> Unit) {
    onClick()
  }
}

class LithoLabelOptionsItem(
  val title: Int,
  val icon: Int,
  val visible: Boolean = true,
  val listener: () -> Unit)

@LayoutSpec
object OptionLabelItemLayoutSpec {
  @OnCreateLayout
  fun onCreate(
    context: ComponentContext,
    @Prop option: LithoLabelOptionsItem): Component {
    val titleColor = sAppTheme.get(ThemeColorType.SECONDARY_TEXT)

    val row = Column.create(context)
      .widthPercent(100f)
      .alignItems(YogaAlign.CENTER)
      .paddingDip(YogaEdge.VERTICAL, 16f)
      .child(
        RoundIcon.create(context)
          .iconRes(option.icon)
          .bgColor(titleColor)
          .iconColor(titleColor)
          .iconSizeRes(R.dimen.toolbar_round_icon_size)
          .iconPaddingRes(R.dimen.toolbar_round_icon_padding)
          .bgAlpha(15)
          .onClick { }
          .isClickDisabled(true)
          .marginDip(YogaEdge.BOTTOM, 4f))
      .child(
        Text.create(context)
          .textRes(option.title)
          .textSizeRes(R.dimen.font_size_normal)
          .typeface(sAppTypeface.title())
          .textStyle(BOLD)
          .textColor(titleColor))
    row.clickHandler(OptionItemLayout.onItemClick(context))
    return row.build()
  }

  @OnEvent(ClickEvent::class)
  fun onItemClick(context: ComponentContext, @Prop onClick: () -> Unit) {
    onClick()
  }
}

abstract class LithoOptionBottomSheet : LithoBottomSheet() {

  abstract fun title(): Int
  abstract fun getOptions(componentContext: ComponentContext, dialog: Dialog): List<LithoOptionsItem>

  override fun getComponent(componentContext: ComponentContext, dialog: Dialog): Component {
    val column = Column.create(componentContext)
      .widthPercent(100f)
      .paddingDip(YogaEdge.VERTICAL, 8f)
      .child(getLithoBottomSheetTitle(componentContext).textRes(title()))
    getOptions(componentContext, dialog).forEach {
      if (it.visible) {
        column.child(OptionItemLayout.create(componentContext)
                       .option(it)
                       .onClick {
                         it.listener()
                       })
      }
    }
    return column.build()
  }
}
