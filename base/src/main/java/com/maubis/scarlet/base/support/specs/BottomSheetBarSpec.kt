package com.maubis.scarlet.base.support.specs

import com.facebook.litho.ClickEvent
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.Row
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.support.sheets.getLithoBottomSheetButton
import com.maubis.scarlet.base.support.ui.ThemeColorType

@LayoutSpec
object BottomSheetBarSpec {
  @OnCreateLayout
  fun onCreate(
      context: ComponentContext,
      @Prop(resType = ResType.STRING) primaryAction: String,
      @Prop(resType = ResType.STRING, optional = true) secondaryAction: String?): Component {
    val row = Row.create(context)

    if (secondaryAction !== null && secondaryAction.isNotBlank()) {
      row.child(Text.create(context)
          .typeface(CoreConfig.FONT_MONSERRAT)
          .textSizeRes(R.dimen.font_size_large)
          .paddingDip(YogaEdge.VERTICAL, 6f)
          .paddingDip(YogaEdge.HORIZONTAL, 16f)
          .textColor(CoreConfig.instance.themeController().get(ThemeColorType.TERTIARY_TEXT))
          .clickHandler(BottomSheetBar.onSecondaryClickEvent(context)))
    }

    row.child(EmptySpec.create(context).flexGrow(1f))
        .child(getLithoBottomSheetButton(context)
            .text(primaryAction)
            .clickHandler(BottomSheetBar.onPrimaryClickEvent(context)))
    return row.build()
  }

  @OnEvent(ClickEvent::class)
  fun onPrimaryClickEvent(context: ComponentContext, @Prop onPrimaryClick: () -> Unit) {
    onPrimaryClick()
  }

  @OnEvent(ClickEvent::class)
  fun onSecondaryClickEvent(context: ComponentContext, @Prop(optional = true) onSecondaryClick: () -> Unit) {
    onSecondaryClick()
  }
}

