package com.maubis.scarlet.base.support.specs

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.widget.Card
import com.facebook.litho.widget.SolidColor
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.R
import com.maubis.scarlet.base.config.ApplicationBase.Companion.sAppTheme
import com.maubis.scarlet.base.support.ui.ThemeColorType

object EmptySpec {
  fun create(context: ComponentContext): SolidColor.Builder {
    return SolidColor.create(context)
      .color(Color.TRANSPARENT)
  }
}

fun Drawable.color(tint: Int): Drawable {
  val drawableCopy = mutate().constantState?.newDrawable()
  drawableCopy?.colorFilter = PorterDuffColorFilter(tint, PorterDuff.Mode.MULTIPLY)
  return drawableCopy ?: this
}

fun separatorSpec(context: ComponentContext): Component.Builder<*> {
  return SolidColor.create(context)
    .alignSelf(YogaAlign.CENTER)
    .colorRes(R.color.material_grey_200)
    .heightDip(1f)
    .widthDip(164f)
    .marginDip(YogaEdge.HORIZONTAL, 32f)
    .marginDip(YogaEdge.TOP, 16f)
    .marginDip(YogaEdge.BOTTOM, 16f)
}

data class ToolbarColorConfig(
  var toolbarBackgroundColor: Int = sAppTheme.get(ThemeColorType.TOOLBAR_BACKGROUND),
  var toolbarIconColor: Int = sAppTheme.get(ThemeColorType.TOOLBAR_ICON))

fun bottomBarRoundIcon(context: ComponentContext, colorConfig: ToolbarColorConfig): RoundIcon.Builder {
  return RoundIcon.create(context)
    .bgColor(colorConfig.toolbarIconColor)
    .iconColor(colorConfig.toolbarIconColor)
    .iconSizeRes(R.dimen.toolbar_round_icon_size)
    .iconPaddingRes(R.dimen.toolbar_round_icon_padding)
    .iconMarginVerticalRes(R.dimen.toolbar_round_icon_margin_vertical)
    .iconMarginHorizontalRes(R.dimen.toolbar_round_icon_margin_horizontal)
    .bgAlpha(15)
}

fun bottomBarCard(context: ComponentContext, child: Component, colorConfig: ToolbarColorConfig): Column.Builder {
  return Column.create(context)
    .widthPercent(100f)
    .paddingDip(YogaEdge.ALL, 0f)
    .backgroundColor(Color.TRANSPARENT)
    .child(
      Card.create(context)
        .widthPercent(100f)
        .backgroundColor(Color.TRANSPARENT)
        .clippingColor(Color.TRANSPARENT)
        .cardBackgroundColor(colorConfig.toolbarBackgroundColor)
        .cornerRadiusDip(0f)
        .elevationDip(0f)
        .content(child))
}