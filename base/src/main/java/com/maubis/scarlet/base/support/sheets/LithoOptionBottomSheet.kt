package com.maubis.scarlet.base.support.sheets

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface.BOLD
import com.facebook.litho.*
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.OnEvent
import com.facebook.litho.annotations.Prop
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.config.CoreConfig.Companion.FONT_MONSERRAT
import com.maubis.scarlet.base.config.CoreConfig.Companion.FONT_OPEN_SANS
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
  fun onCreate(context: ComponentContext,
               @Prop option: LithoOptionsItem): Component {
    val theme = CoreConfig.instance.themeController()
    val titleColor = theme.get(ThemeColorType.SECONDARY_TEXT)
    val subtitleColor = theme.get(ThemeColorType.HINT_TEXT)
    val selectedColor = theme.get(ThemeColorType.ACCENT_TEXT)

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
        .child(Column.create(context)
            .flexGrow(1f)
            .child(
                Text.create(context)
                    .textRes(option.title)
                    .textSizeRes(R.dimen.font_size_normal)
                    .typeface(FONT_MONSERRAT)
                    .textStyle(BOLD)
                    .textColor(titleColor))
            .child(
                Text.create(context)
                    .text(subtitle)
                    .textSizeRes(R.dimen.font_size_small)
                    .typeface(FONT_OPEN_SANS)
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
    }

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
              reset(componentContext.androidContext, dialog)
            })
      }
    }
    return column.build()
  }
}
