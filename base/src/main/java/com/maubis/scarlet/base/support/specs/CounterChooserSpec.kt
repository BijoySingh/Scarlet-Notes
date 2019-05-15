package com.maubis.scarlet.base.support.specs

import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.Row
import com.facebook.litho.annotations.LayoutSpec
import com.facebook.litho.annotations.OnCreateLayout
import com.facebook.litho.annotations.Prop
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.support.ui.ThemeColorType

@LayoutSpec
object CounterChooserSpec {
  @OnCreateLayout
  fun onCreate(
      context: ComponentContext,
      @Prop value: Int,
      @Prop minValue: Int,
      @Prop maxValue: Int,
      @Prop onValueChange: (Int) -> Unit): Component {
    val row = Row.create(context)
        .alignItems(YogaAlign.CENTER)
        .child(EmptySpec.create(context).flexGrow(1f))
        .child(bottomBarRoundIcon(context, ToolbarColorConfig())
            .iconRes(R.drawable.icon_less_counter)
            .onClick { onValueChange(Math.max(value - 1, minValue)) })
        .child(Text.create(context)
            .text(value.toString())
            .typeface(CoreConfig.FONT_MONSERRAT)
            .textSizeRes(R.dimen.font_size_xxxlarge)
            .paddingDip(YogaEdge.HORIZONTAL, 12f)
            .textColor(ApplicationBase.instance.themeController().get(ThemeColorType.TERTIARY_TEXT)))
        .child(bottomBarRoundIcon(context, ToolbarColorConfig())
            .iconRes(R.drawable.icon_more_counter)
            .onClick { onValueChange(Math.min(value + 1, maxValue)) })
        .child(EmptySpec.create(context).flexGrow(1f))
    return row.build()
  }
}

