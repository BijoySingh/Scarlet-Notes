package com.maubis.scarlet.base.support.specs

import android.graphics.Color
import android.graphics.drawable.Drawable
import com.facebook.litho.ClickEvent
import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.annotations.*
import com.facebook.litho.widget.Image
import com.facebook.yoga.YogaAlign
import com.facebook.yoga.YogaEdge
import com.maubis.scarlet.base.support.ui.LithoCircleDrawable

@LayoutSpec
object RoundIconSpec {
  @OnCreateLayout
  fun onCreate(
      context: ComponentContext,
      @Prop(resType = ResType.DRAWABLE) icon: Drawable,
      @Prop(resType = ResType.COLOR) iconColor: Int,
      @Prop(resType = ResType.COLOR) bgColor: Int,
      @Prop(resType = ResType.DIMEN_SIZE) iconSize: Int,
      @Prop(resType = ResType.DIMEN_SIZE, optional = true) iconPadding: Int?,
      @Prop(resType = ResType.DIMEN_OFFSET, optional = true) iconMarginVertical: Int?,
      @Prop(resType = ResType.DIMEN_OFFSET, optional = true) iconMarginHorizontal: Int?,
      @Prop(optional = true) bgAlpha: Int?,
      @Prop(optional = true) isClickDisabled: Boolean?,
      @Prop(optional = true) showBorder: Boolean?): Component {
    val image = Image.create(context)
        .heightPx(iconSize)
        .widthPx(iconSize)
        .paddingPx(YogaEdge.ALL, iconPadding ?: 0)
        .marginPx(YogaEdge.VERTICAL, iconMarginVertical ?: 0)
        .marginPx(YogaEdge.HORIZONTAL, iconMarginHorizontal ?: 0)
        .drawable(icon.color(iconColor))
        .background(LithoCircleDrawable(bgColor, bgAlpha ?: 255, showBorder ?: false))
    if (isClickDisabled === null || !isClickDisabled) {
      image.clickHandler(RoundIcon.onClickEvent(context))
    }
    return Column.create(context)
        .alignItems(YogaAlign.CENTER)
        .child(image)
        .build()
  }

  @OnEvent(ClickEvent::class)
  fun onClickEvent(context: ComponentContext, @Prop(optional = true) onClick: () -> Unit) {
    onClick()
  }
}
